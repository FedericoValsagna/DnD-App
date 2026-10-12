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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.valsagnapps.dndapp.R
import com.valsagnapps.dndapp.data.RepositoryError
import com.valsagnapps.dndapp.domain.ActionType
import com.valsagnapps.dndapp.domain.ClassFeature
import com.valsagnapps.dndapp.domain.Recharge
import com.valsagnapps.dndapp.domain.Resource
import com.valsagnapps.dndapp.domain.UsesDisplay
import com.valsagnapps.dndapp.ui.common.errorMessage
import com.valsagnapps.dndapp.ui.theme.DnDAppTheme
import com.valsagnapps.dndapp.ui.theme.SheetType

/**
 * An action's sheet, from the bottom: its name, type and text, and its uses. Points are tapped as in the
 * row (it closes when saved); a pool is spent or recovered with the keypad, showing how it's left first.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun FeatureSheet(
    dialog: CombatDialog.Feature,
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
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            FeatureText(dialog)
            dialog.resource?.let { resource ->
                if (dialog.isPool) {
                    PoolControls(dialog, resource, actions)
                } else {
                    UsePips(
                        resource,
                        enabled = !isSaving,
                        onUsedChange = { actions.onResourceUsedChange(resource.id, it) },
                    )
                }
            }
            error?.let {
                Text(
                    text = stringResource(R.string.edit_save_error, errorMessage(it)),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            if (dialog.isPool) {
                ApplyButton(dialog, isSaving, actions.onConfirm)
            }
        }
    }
}

/** Name, "Bonus Action · Ki · short rest" and the summary. */
@Composable
private fun FeatureText(dialog: CombatDialog.Feature) {
    val details = listOfNotNull(
        dialog.feature.actionType?.let { stringResource(it.label) },
        dialog.resource?.let { resource -> resource.name.takeIf { it != dialog.feature.name } },
        dialog.resource?.recharge?.let { stringResource(it.label) },
    )
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(dialog.feature.name, style = SheetType.characterName)
        if (details.isNotEmpty()) {
            Text(
                text = details.joinToString(" · "),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(dialog.feature.summary, style = MaterialTheme.typography.bodyMedium)
    }
}

private val Recharge.label: Int
    get() = when (this) {
        Recharge.SHORT_REST -> R.string.recharge_short_rest
        Recharge.LONG_REST -> R.string.recharge_long_rest
    }

/** Spend or recover, the amount typed with how the pool is left ("25/25 → 20/25") and the keypad. */
@Composable
private fun PoolControls(
    dialog: CombatDialog.Feature,
    resource: Resource,
    actions: CombatActions,
    modifier: Modifier = Modifier,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        PoolModeSelector(dialog.recovering, onSelect = { actions.onInput(CombatInput.Recovering(it)) })
        PoolAmount(dialog, resource)
        Keypad(onInput = actions.onInput)
    }
}

@Composable
private fun PoolModeSelector(recovering: Boolean, onSelect: (Boolean) -> Unit) {
    val modes = listOf(false to R.string.spend, true to R.string.recover)
    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
        modes.forEachIndexed { index, (mode, label) ->
            SegmentedButton(
                selected = recovering == mode,
                onClick = { onSelect(mode) },
                shape = SegmentedButtonDefaults.itemShape(index, modes.size),
                label = { Text(stringResource(label)) },
            )
        }
    }
}

@Composable
private fun PoolAmount(dialog: CombatDialog.Feature, resource: Resource) {
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
        val now = poolText(resource)
        Text(
            text =
            dialog.newUsed?.let { stringResource(R.string.preview_result, now, poolText(resource.copy(used = it))) }
                ?: now,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ApplyButton(dialog: CombatDialog.Feature, isSaving: Boolean, onConfirm: () -> Unit) {
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
                    dialog.recovering -> stringResource(R.string.apply_recover, amount)
                    else -> stringResource(R.string.apply_spend, amount)
                },
            )
        }
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun FeatureSheetPreview() {
    DnDAppTheme {
        FeatureSheet(
            dialog = CombatDialog.Feature(
                feature = ClassFeature(
                    "PALADIN_LAY_ON_HANDS",
                    "Lay on Hands",
                    1,
                    "Action: restore HP from a pool of 5 × paladin level, or spend 5 to cure a disease or poison.",
                    actionType = ActionType.ACTION,
                    resource = "LAY_ON_HANDS",
                ),
                resource = Resource(
                    id = "LAY_ON_HANDS",
                    name = "Lay on Hands",
                    max = 25,
                    used = 5,
                    recharge = Recharge.LONG_REST,
                    display = UsesDisplay.POOL,
                ),
                amount = "8",
            ),
            isSaving = false,
            error = null,
            actions = CombatActions(),
        )
    }
}
