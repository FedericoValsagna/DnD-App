package com.valsagnapps.dndapp.ui.charactersheet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.valsagnapps.dndapp.data.CharacterRepository
import com.valsagnapps.dndapp.data.RepositoryError
import com.valsagnapps.dndapp.data.RepositoryResult
import com.valsagnapps.dndapp.domain.Character
import com.valsagnapps.dndapp.domain.CharacterClass
import com.valsagnapps.dndapp.domain.CharacterRules
import com.valsagnapps.dndapp.domain.ClassLevel
import com.valsagnapps.dndapp.domain.Proficiency
import com.valsagnapps.dndapp.domain.Skill
import com.valsagnapps.dndapp.ui.common.toNumericInput
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface CharacterSheetUiState {
    data object Loading : CharacterSheetUiState

    data class Content(
        val character: Character,
        /** Edit mode: only then can the class, the max HP and the skills be changed. */
        val isEditing: Boolean = false,
        /** A skill change is being sent; the skills can't be edited until it finishes. */
        val isSavingSkills: Boolean = false,
        /** The last skill change failed; the character keeps the values it had. */
        val skillsError: RepositoryError? = null,
        /** Value being edited in a dialog, or null if no dialog is open. */
        val edit: SheetEdit? = null,
        val isSavingEdit: Boolean = false,
        /** Saving the edit failed; the dialog stays open to retry. */
        val editError: RepositoryError? = null,
    ) : CharacterSheetUiState {
        /** The class can be edited only for single-class characters (multiclass comes later). */
        val canEditClass: Boolean get() = character.classes.size <= 1
    }

    data class Error(val error: RepositoryError) : CharacterSheetUiState
}

/** Values edited in the sheet's dialogs. Numbers are kept as text so the user can clear them while typing. */
sealed interface SheetEdit {
    val isValid: Boolean

    data class Class(val characterClass: CharacterClass?, val level: String) : SheetEdit {
        val isLevelValid: Boolean get() = level.toIntOrNull()?.let(CharacterRules::isValidLevel) == true
        override val isValid: Boolean get() = characterClass != null && isLevelValid
    }

    data class MaxHitPoints(val value: String) : SheetEdit {
        override val isValid: Boolean
            get() = value.toIntOrNull()?.let(CharacterRules::isValidMaxHitPoints) == true
    }
}

class CharacterSheetViewModel(private val characterId: String, private val repository: CharacterRepository) :
    ViewModel() {

    private val _uiState = MutableStateFlow<CharacterSheetUiState>(CharacterSheetUiState.Loading)
    val uiState: StateFlow<CharacterSheetUiState> = _uiState.asStateFlow()

    init {
        retry()
    }

    /** Enters or leaves edit mode. Leaving it closes the open dialog and clears the errors. */
    fun onToggleEditing() {
        val state = _uiState.value as? CharacterSheetUiState.Content ?: return
        if (state.isSavingEdit || state.isSavingSkills) return
        _uiState.value = state.copy(isEditing = !state.isEditing, edit = null, editError = null, skillsError = null)
    }

    /** Saves the change right away; the sheet shows what the server returns (with the new bonuses). */
    fun onSkillProficiencyChange(skill: Skill, proficiency: Proficiency) {
        val state = _uiState.value.editing() ?: return
        if (state.isSavingSkills) return
        val skills = state.character.skillProficiencies + (skill to proficiency)
        _uiState.value = state.copy(isSavingSkills = true, skillsError = null)
        viewModelScope.launch {
            val result = repository.updateSkills(characterId, skills)
            _uiState.update { current ->
                when (result) {
                    is RepositoryResult.Success -> current.withCharacter(result.value)
                    is RepositoryResult.Failure ->
                        (current as? CharacterSheetUiState.Content)
                            ?.copy(isSavingSkills = false, skillsError = result.error)
                            ?: current
                }
            }
        }
    }

    fun onEditClass() {
        val state = _uiState.value.editing() ?: return
        if (!state.canEditClass) return
        val current = state.character.classes.firstOrNull()
        val edit = SheetEdit.Class(current?.characterClass, state.character.level.toString())
        _uiState.value = state.copy(edit = edit, editError = null)
    }

    fun onEditMaxHitPoints() {
        val state = _uiState.value.editing() ?: return
        val edit = SheetEdit.MaxHitPoints(state.character.maxHitPoints?.toString().orEmpty())
        _uiState.value = state.copy(edit = edit, editError = null)
    }

    fun onEditClassChange(characterClass: CharacterClass) = updateEdit<SheetEdit.Class> {
        it.copy(characterClass = characterClass)
    }

    fun onEditLevelChange(level: String) =
        updateEdit<SheetEdit.Class> { it.copy(level = level.toNumericInput(CharacterRules.LEVEL_RANGE)) }

    fun onEditMaxHitPointsChange(value: String) = updateEdit<SheetEdit.MaxHitPoints> {
        it.copy(value = value.toNumericInput(CharacterRules.MAX_HIT_POINTS_RANGE))
    }

    fun onDismissEdit() {
        val state = _uiState.value as? CharacterSheetUiState.Content ?: return
        if (state.isSavingEdit) return
        _uiState.value = state.copy(edit = null, editError = null)
    }

    /** Sends the edit; on success the dialog closes and the sheet shows what the server returns. */
    fun onConfirmEdit() {
        val state = _uiState.value as? CharacterSheetUiState.Content ?: return
        val edit = state.edit?.takeIf { it.isValid && !state.isSavingEdit } ?: return
        _uiState.value = state.copy(isSavingEdit = true, editError = null)
        viewModelScope.launch {
            val result = when (edit) {
                is SheetEdit.Class -> repository.updateClasses(
                    characterId,
                    listOf(ClassLevel(checkNotNull(edit.characterClass), edit.level.toInt())),
                )
                is SheetEdit.MaxHitPoints -> repository.updateMaxHitPoints(characterId, edit.value.toInt())
            }
            _uiState.update { current ->
                when (result) {
                    is RepositoryResult.Success -> current.withCharacter(result.value)
                    is RepositoryResult.Failure ->
                        (current as? CharacterSheetUiState.Content)
                            ?.copy(isSavingEdit = false, editError = result.error)
                            ?: current
                }
            }
        }
    }

    private inline fun <reified E : SheetEdit> updateEdit(change: (E) -> E) {
        val state = _uiState.value as? CharacterSheetUiState.Content
        val edit = state?.edit as? E
        if (state == null || edit == null || state.isSavingEdit) return
        _uiState.value = state.copy(edit = change(edit))
    }

    /** Loads the character (also the first time). */
    fun retry() {
        _uiState.value = CharacterSheetUiState.Loading
        viewModelScope.launch {
            _uiState.value = when (val result = repository.get(characterId)) {
                is RepositoryResult.Success -> CharacterSheetUiState.Content(result.value)
                is RepositoryResult.Failure -> CharacterSheetUiState.Error(result.error)
            }
        }
    }
}

/** The content, only if it is in edit mode. */
private fun CharacterSheetUiState.editing(): CharacterSheetUiState.Content? =
    (this as? CharacterSheetUiState.Content)?.takeIf { it.isEditing }

/** The character the server returned after a save; stays in edit mode, without dialog or errors. */
private fun CharacterSheetUiState.withCharacter(character: Character): CharacterSheetUiState =
    (this as? CharacterSheetUiState.Content)?.copy(
        character = character,
        isSavingSkills = false,
        skillsError = null,
        edit = null,
        isSavingEdit = false,
        editError = null,
    ) ?: CharacterSheetUiState.Content(character)
