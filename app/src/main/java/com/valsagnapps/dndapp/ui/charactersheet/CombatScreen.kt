package com.valsagnapps.dndapp.ui.charactersheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.valsagnapps.dndapp.R
import com.valsagnapps.dndapp.domain.LifeStatus
import com.valsagnapps.dndapp.ui.common.errorMessage

/**
 * Combate: AC and Initiative on top (Spell DC and Spell Attack come with spellcasting). Below, the
 * actions by type with their uses, which scroll on their own; at 0 HP the death saves take their place,
 * with the stable or dead state. In edit mode AC can be tapped, and the hint goes at the foot, as in Personaje.
 */
@Composable
internal fun CombatScreen(
    state: CharacterSheetUiState.Content,
    editActions: SheetEditActions,
    combatActions: CombatActions,
    modifier: Modifier = Modifier,
) {
    val character = state.character
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCell(
                label = stringResource(R.string.armor_class),
                value = character.armorClass?.toString() ?: stringResource(R.string.missing_value),
                onClick = { editActions.onStartEdit(SheetEditTarget.ARMOR_CLASS) }
                    .takeIf { state.isEditing && character.armorClass != null },
                modifier = Modifier.weight(1f),
            )
            StatCell(
                label = stringResource(R.string.initiative_short),
                value = character.initiative?.let { stringResource(R.string.signed_value, it) }
                    ?: stringResource(R.string.missing_value),
                modifier = Modifier.weight(1f),
            )
        }
        if (character.lifeStatus == LifeStatus.CONSCIOUS) {
            CombatActionsList(
                character = character,
                enabled = !state.isSavingCombat,
                actions = combatActions,
                modifier = Modifier.weight(1f),
            )
        } else {
            SectionLabel(stringResource(R.string.death_saves), Modifier.padding(top = 8.dp))
            DeathSavesBox(character.deathSaves, enabled = !state.isSavingCombat, combatActions.onDeathSavesChange)
            LifeStatusBox(character.lifeStatus, combatActions.onStartRevive)
        }
        // A dialog shows its own error; this one is a death save's.
        state.combatError?.takeIf { state.combat == null }?.let {
            Text(
                text = stringResource(R.string.edit_save_error, errorMessage(it)),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        if (character.lifeStatus != LifeStatus.CONSCIOUS) {
            Spacer(Modifier.weight(1f))
        }
        if (state.isEditing) {
            CombatEditHint()
        }
    }
}

/** Stable or dead: what it means, and for the dead the way back. Nothing while dying. */
@Composable
private fun LifeStatusBox(status: LifeStatus, onRevive: () -> Unit, modifier: Modifier = Modifier) {
    val (title, message) = when (status) {
        LifeStatus.STABLE -> R.string.stable to R.string.stable_message
        LifeStatus.DEAD -> R.string.dead to R.string.dead_message
        LifeStatus.CONSCIOUS, LifeStatus.DYING -> return
    }
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(title), style = MaterialTheme.typography.titleMedium)
            Text(
                text = stringResource(message),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (status == LifeStatus.DEAD) {
                Button(onClick = onRevive) { Text(stringResource(R.string.revive)) }
            }
        }
    }
}

@Composable
private fun CombatEditHint(modifier: Modifier = Modifier) {
    StripBox(MaterialTheme.colorScheme.secondaryContainer, modifier) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(
                painter = painterResource(R.drawable.ic_edit),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = stringResource(R.string.combat_edit_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }
    }
}
