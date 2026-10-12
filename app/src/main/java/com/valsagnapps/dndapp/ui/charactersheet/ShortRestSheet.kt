package com.valsagnapps.dndapp.ui.charactersheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.valsagnapps.dndapp.R
import com.valsagnapps.dndapp.data.RepositoryError
import com.valsagnapps.dndapp.domain.HitDice
import com.valsagnapps.dndapp.ui.common.errorMessage
import com.valsagnapps.dndapp.ui.theme.DnDAppTheme
import com.valsagnapps.dndapp.ui.theme.SheetType

/**
 * The short rest sheet, from the bottom, shaped as the HP sheet: a counter per die, the total of the dice typed
 * with the keypad (without the CON, which the server adds) and how the HP are left. Without dice it also rests:
 * it recovers the short rest resources.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ShortRestSheet(
    dialog: CombatDialog.ShortRest,
    isSaving: Boolean,
    error: RepositoryError?,
    actions: CombatActions,
    modifier: Modifier = Modifier,
) {
    ModalBottomSheet(
        onDismissRequest = actions.onDismiss,
        modifier = modifier,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        ShortRestContent(dialog, isSaving, error, actions)
    }
}

@Composable
private fun ShortRestContent(
    dialog: CombatDialog.ShortRest,
    isSaving: Boolean,
    error: RepositoryError?,
    actions: CombatActions,
) {
    // Scrolls: with a multiclass there's a counter per die, and the keypad is tall.
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(stringResource(R.string.short_rest), style = SheetType.characterName)
        dialog.hitDice.forEach { hitDice ->
            HitDieCounter(
                hitDice = hitDice,
                spent = dialog.spentOf(hitDice.die),
                enabled = dialog.canSpendDice,
                onChange = { actions.onInput(CombatInput.SpentHitDice(hitDice.die, it)) },
            )
        }
        RollDisplay(dialog)
        Keypad(onInput = actions.onInput)
        error?.let {
            Text(
                text = stringResource(R.string.edit_save_error, errorMessage(it)),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        RestButton(enabled = dialog.isValid && !isSaving, isSaving = isSaving, onClick = actions.onConfirm)
    }
}

/** "d10 · quedan 4" and − count +: from 0 to the ones left. */
@Composable
private fun HitDieCounter(hitDice: HitDice, spent: Int, enabled: Boolean, onChange: (Int) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = stringResource(R.string.hit_die_name, hitDice.die),
            style = MaterialTheme.typography.bodyLarge,
        )
        Text(
            text = " " + stringResource(R.string.hit_dice_left, hitDice.remaining),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        IconButton(onClick = { onChange(spent - 1) }, enabled = enabled && spent > 0) {
            Icon(
                painter = painterResource(R.drawable.ic_remove),
                contentDescription = stringResource(R.string.hit_die_less, hitDice.die),
            )
        }
        ValueChip(spent.toString())
        IconButton(onClick = { onChange(spent + 1) }, enabled = enabled && spent < hitDice.remaining) {
            Icon(
                painter = painterResource(R.drawable.ic_add),
                contentDescription = stringResource(R.string.hit_die_more, hitDice.die),
            )
        }
    }
}

/** The roll typed and, on the right, the heart with the HP after resting: green if they reach the max. */
@Composable
private fun RollDisplay(dialog: CombatDialog.ShortRest) {
    val full = dialog.hitPointsAfter == dialog.maxHitPoints
    val color = if (full) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
    val after = stringResource(R.string.hit_points_value, dialog.hitPointsAfter, dialog.maxHitPoints)
    val afterDescription = stringResource(R.string.hit_points_after)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = dialog.roll.ifEmpty { "0" },
            style = SheetType.hitPointsAmount,
            color = if (dialog.roll.isEmpty()) {
                MaterialTheme.colorScheme.onSurfaceVariant
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            modifier = Modifier.weight(1f),
        )
        Icon(painter = painterResource(R.drawable.ic_favorite), contentDescription = null, tint = color)
        Text(
            text = after,
            style = SheetType.hpValue,
            color = if (full) color else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.semantics { contentDescription = "$afterDescription: $after" },
        )
    }
}

@Composable
private fun RestButton(enabled: Boolean, isSaving: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
    ) {
        if (isSaving) {
            CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
        } else {
            Text(stringResource(R.string.rest))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ShortRestContentPreview() {
    DnDAppTheme {
        ShortRestContent(
            dialog = CombatDialog.ShortRest(
                hitDice = listOf(HitDice(die = 10, count = 5, used = 1), HitDice(die = 8, count = 3)),
                currentHitPoints = 20,
                maxHitPoints = 44,
                constitutionModifier = 2,
                canSpendDice = true,
                spent = mapOf(Pair(first = 10, second = 2)),
                roll = "13",
            ),
            isSaving = false,
            error = null,
            actions = CombatActions(),
        )
    }
}
