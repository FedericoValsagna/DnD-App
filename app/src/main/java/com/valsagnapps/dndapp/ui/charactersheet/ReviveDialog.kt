package com.valsagnapps.dndapp.ui.charactersheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.valsagnapps.dndapp.R
import com.valsagnapps.dndapp.data.RepositoryError
import com.valsagnapps.dndapp.ui.common.NumberField
import com.valsagnapps.dndapp.ui.common.errorMessage

/** With how many HP a dead character comes back: 1 (Revivify, Raise Dead) unless changed. */
@Composable
internal fun ReviveDialog(
    dialog: CombatDialog.Revive,
    isSaving: Boolean,
    error: RepositoryError?,
    actions: CombatActions,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = actions.onDismiss,
        title = { Text(stringResource(R.string.revive)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                NumberField(
                    value = dialog.hitPoints,
                    onValueChange = { actions.onInput(CombatInput.ReviveHitPoints(it)) },
                    label = stringResource(R.string.revive_hit_points),
                    range = dialog.range,
                    showError = !dialog.isValid,
                    supportingText = stringResource(R.string.revive_hit_points_hint, dialog.maxHitPoints),
                    modifier = Modifier.fillMaxWidth(),
                )
                error?.let {
                    Text(
                        text = stringResource(R.string.edit_save_error, errorMessage(it)),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = actions.onConfirm, enabled = dialog.isValid && !isSaving) {
                Text(stringResource(R.string.revive))
            }
        },
        dismissButton = {
            TextButton(onClick = actions.onDismiss, enabled = !isSaving) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}
