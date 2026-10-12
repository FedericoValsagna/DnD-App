package com.valsagnapps.dndapp.ui.charactersheet

import com.valsagnapps.dndapp.data.CombatRepository
import com.valsagnapps.dndapp.data.RepositoryError
import com.valsagnapps.dndapp.data.RepositoryResult
import com.valsagnapps.dndapp.domain.Character
import com.valsagnapps.dndapp.domain.ClassFeature
import com.valsagnapps.dndapp.domain.DeathSaves
import com.valsagnapps.dndapp.domain.HitPointsAction
import com.valsagnapps.dndapp.domain.LifeStatus
import com.valsagnapps.dndapp.domain.resourceOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * The sheet's combat actions (HP sheet, death saves, reviving), on the same state as the ViewModel. They
 * work in and out of edit mode: they're used at the table. Each change goes to the server, which applies the
 * rules, and the sheet shows the character it returns.
 */
class SheetCombat internal constructor(
    private val characterId: String,
    private val repository: CombatRepository,
    private val scope: CoroutineScope,
    private val state: MutableStateFlow<CharacterSheetUiState>,
) {
    /** Opens the HP sheet, unless the character is dead (healing doesn't apply) or the server sent no HP. */
    fun onOpenHitPoints() {
        val content = idle() ?: return
        val character = content.character
        if (character.currentHitPoints == null || character.lifeStatus == LifeStatus.DEAD) return
        state.value = content.copy(combat = CombatDialog.HitPoints(), combatError = null)
    }

    fun onStartRevive() {
        val content = idle() ?: return
        val max = content.character.maxHitPoints
        if (content.character.lifeStatus != LifeStatus.DEAD || max == null) return
        state.value = content.copy(combat = CombatDialog.Revive(max), combatError = null)
    }

    /** Opens the sheet of an action from Combate, with the uses of its resource as they are now. */
    fun onOpenFeature(feature: ClassFeature) {
        val content = idle() ?: return
        state.value = content.copy(
            combat = CombatDialog.Feature(feature, content.character.resourceOf(feature)),
            combatError = null,
        )
    }

    fun onInput(input: CombatInput) {
        val content = state.value as? CharacterSheetUiState.Content
        val combat = content?.combat
        if (combat == null || content.isSavingCombat) return
        state.value = content.copy(combat = combat.changedBy(input))
    }

    fun onDismiss() {
        val content = state.value as? CharacterSheetUiState.Content ?: return
        if (content.isSavingCombat) return
        state.value = content.copy(combat = null, combatError = null)
    }

    /** Sends the open dialog; on success it closes, and if it fails it stays open with the error. */
    fun onConfirm() {
        val content = state.value as? CharacterSheetUiState.Content ?: return
        val combat = content.combat?.takeIf { it.isValid && !content.isSavingCombat } ?: return
        send(content) {
            when (combat) {
                is CombatDialog.HitPoints -> applyHitPoints(combat)
                is CombatDialog.Revive -> repository.revive(characterId, combat.hitPoints.toInt())
                is CombatDialog.Feature -> repository.updateResourceUsed(
                    characterId,
                    checkNotNull(combat.resource).id,
                    checkNotNull(combat.newUsed),
                )
            }
        }
    }

    /** Saves the counters right away, as marking a slot. */
    fun onDeathSavesChange(deathSaves: DeathSaves) {
        val content = idle() ?: return
        if (content.character.currentHitPoints != 0) return
        send(content) { repository.updateDeathSaves(characterId, deathSaves) }
    }

    /**
     * Saves the used uses of [resource] right away, as tapping a point (in the row or in the action's sheet,
     * which closes when it's saved). Out of range does nothing: the server would refuse it.
     */
    fun onResourceUsedChange(resource: String, used: Int) {
        val content = idle() ?: return
        val max = content.character.resources.find { it.id == resource }?.max
        if (max != null && used in 0..max) {
            send(content) { repository.updateResourceUsed(characterId, resource, used) }
        }
    }

    private suspend fun applyHitPoints(dialog: CombatDialog.HitPoints): RepositoryResult<Character> {
        val amount = checkNotNull(dialog.amountValue)
        return when (dialog.action) {
            HitPointsAction.DAMAGE -> repository.takeDamage(characterId, amount, dialog.critical)
            HitPointsAction.HEALING -> repository.heal(characterId, amount)
            HitPointsAction.TEMPORARY -> repository.setTemporaryHitPoints(characterId, amount)
        }
    }

    private fun send(content: CharacterSheetUiState.Content, call: suspend () -> RepositoryResult<Character>) {
        state.value = content.copy(isSavingCombat = true, combatError = null)
        scope.launch {
            val result = call()
            state.update { current ->
                when (result) {
                    is RepositoryResult.Success -> current.withCharacter(result.value)
                    is RepositoryResult.Failure -> current.withCombatError(result.error)
                }
            }
        }
    }

    /** The content, if nothing is being saved and no edit dialog is open. */
    private fun idle(): CharacterSheetUiState.Content? = (state.value as? CharacterSheetUiState.Content)
        ?.takeIf { !it.isSavingCombat && !it.isSavingEdit && it.edit == null }
}

/** Saving a combat action failed: the dialog (if any) stays open with the error, to retry. */
private fun CharacterSheetUiState.withCombatError(error: RepositoryError): CharacterSheetUiState =
    (this as? CharacterSheetUiState.Content)?.copy(isSavingCombat = false, combatError = error) ?: this
