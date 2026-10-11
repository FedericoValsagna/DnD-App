package com.valsagnapps.dndapp.ui.charactersheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.valsagnapps.dndapp.R
import com.valsagnapps.dndapp.data.RepositoryError
import com.valsagnapps.dndapp.domain.Character
import com.valsagnapps.dndapp.domain.HitPointsAction
import com.valsagnapps.dndapp.domain.HitPointsPreview
import com.valsagnapps.dndapp.domain.previewHitPoints
import com.valsagnapps.dndapp.ui.common.errorMessage
import com.valsagnapps.dndapp.ui.theme.SheetType

private const val KEYPAD_COLUMNS = 3

/**
 * The HP sheet, from the bottom: what to do (damage, healing, temporary HP), a keypad with big keys
 * instead of the system keyboard, and how the HP are left before applying. Critical only shows at 0 HP,
 * where it counts.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HitPointsSheet(
    character: Character,
    dialog: CombatDialog.HitPoints,
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
        Column(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ActionSelector(dialog.action, onSelect = { actions.onInput(CombatInput.Action(it)) })
            AmountDisplay(character, dialog)
            if (dialog.action == HitPointsAction.DAMAGE && character.currentHitPoints == 0) {
                CriticalChip(dialog.critical, onChange = { actions.onInput(CombatInput.Critical(it)) })
            }
            Keypad(onInput = actions.onInput)
            error?.let {
                Text(
                    text = stringResource(R.string.edit_save_error, errorMessage(it)),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            ApplyButton(dialog, isSaving, actions.onConfirm)
        }
    }
}

@Composable
private fun ActionSelector(selected: HitPointsAction, onSelect: (HitPointsAction) -> Unit) {
    val labels = mapOf(
        HitPointsAction.DAMAGE to R.string.damage,
        HitPointsAction.HEALING to R.string.healing,
        HitPointsAction.TEMPORARY to R.string.temporary_hit_points,
    )
    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
        HitPointsAction.entries.forEachIndexed { index, action ->
            SegmentedButton(
                selected = action == selected,
                onClick = { onSelect(action) },
                shape = SegmentedButtonDefaults.itemShape(index, HitPointsAction.entries.size),
                label = { Text(stringResource(labels.getValue(action))) },
            )
        }
    }
}

/** The amount typed and, on the right, how the HP are left ("12/44 +5 → 9/44"). */
@Composable
private fun AmountDisplay(character: Character, dialog: CombatDialog.HitPoints) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = dialog.amount.ifEmpty { "0" },
            style = SheetType.hitPointsAmount,
            color = if (dialog.amount.isEmpty()) {
                MaterialTheme.colorScheme.onSurfaceVariant
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            modifier = Modifier.weight(1f),
        )
        Text(
            text = previewText(character, dialog),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun previewText(character: Character, dialog: CombatDialog.HitPoints): String {
    val now = hitPointsText(character.currentHitPoints ?: 0, character.maxHitPoints ?: 0, character.temporaryHitPoints)
    val preview = dialog.amountValue?.let { character.previewHitPoints(dialog.action, it, dialog.critical) }
        ?: return now
    val after = when (preview) {
        is HitPointsPreview.Points -> hitPointsText(preview.current, character.maxHitPoints ?: 0, preview.temporary)
        is HitPointsPreview.Failures -> pluralStringResource(R.plurals.preview_failures, preview.count, preview.count)
        HitPointsPreview.Dead -> stringResource(R.string.preview_dead)
    }
    return stringResource(R.string.preview_result, now, after)
}

/** "12/44", or "12/44 +5" with temporary HP. */
@Composable
internal fun hitPointsText(current: Int, max: Int, temporary: Int): String {
    val points = stringResource(R.string.hit_points_value, current, max)
    return if (temporary > 0) "$points ${stringResource(R.string.temporary_hit_points_value, temporary)}" else points
}

@Composable
private fun CriticalChip(critical: Boolean, onChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(
            selected = critical,
            onClick = { onChange(!critical) },
            label = { Text(stringResource(R.string.critical)) },
        )
        Text(
            text = stringResource(R.string.critical_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** 1 to 9, then Borrar, 0 and backspace. */
@Composable
private fun Keypad(onInput: (CombatInput) -> Unit) {
    val keys: List<Pair<CombatInput, @Composable () -> Unit>> =
        (1..9).map { digitKey(it) } +
            listOf(
                CombatInput.Clear to { KeyLabel(stringResource(R.string.keypad_clear), small = true) },
                digitKey(0),
                CombatInput.Backspace to {
                    Icon(
                        painter = painterResource(R.drawable.ic_backspace),
                        contentDescription = stringResource(R.string.keypad_backspace),
                    )
                },
            )
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        keys.chunked(KEYPAD_COLUMNS).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                row.forEach { (input, label) ->
                    Surface(
                        onClick = { onInput(input) },
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHighest,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) { label() }
                    }
                }
            }
        }
    }
}

private fun digitKey(digit: Int): Pair<CombatInput, @Composable () -> Unit> =
    CombatInput.Digit(digit) to { KeyLabel(digit.toString()) }

@Composable
private fun KeyLabel(text: String, small: Boolean = false) {
    Text(text, style = if (small) MaterialTheme.typography.labelLarge else MaterialTheme.typography.titleLarge)
}

@Composable
private fun ApplyButton(dialog: CombatDialog.HitPoints, isSaving: Boolean, onConfirm: () -> Unit) {
    val amount = dialog.amountValue
    Button(
        onClick = onConfirm,
        enabled = dialog.isValid && !isSaving,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
    ) {
        if (isSaving) {
            CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
        } else {
            Text(
                when {
                    amount == null -> stringResource(R.string.apply_amount_missing)
                    dialog.action == HitPointsAction.DAMAGE -> stringResource(R.string.apply_damage, amount)
                    dialog.action == HitPointsAction.HEALING -> stringResource(R.string.apply_healing, amount)
                    else -> stringResource(R.string.apply_temporary, amount)
                },
            )
        }
    }
}
