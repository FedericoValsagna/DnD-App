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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.valsagnapps.dndapp.R
import com.valsagnapps.dndapp.data.RepositoryError
import com.valsagnapps.dndapp.domain.CharacterClass
import com.valsagnapps.dndapp.domain.CharacterRules
import com.valsagnapps.dndapp.domain.ClassInfo
import com.valsagnapps.dndapp.domain.Subclass
import com.valsagnapps.dndapp.ui.common.ClassDropdown
import com.valsagnapps.dndapp.ui.common.NumberField
import com.valsagnapps.dndapp.ui.common.errorMessage
import com.valsagnapps.dndapp.ui.theme.DnDAppTheme

/** Dialog for [edit]. It stays open while saving and if saving fails, showing [error]. */
@Composable
fun SheetEditDialog(
    edit: SheetEdit,
    isSaving: Boolean,
    error: RepositoryError?,
    actions: SheetEditActions,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = actions.onDismiss,
        title = {
            Text(
                stringResource(
                    when (edit) {
                        is SheetEdit.Class -> R.string.character_class
                        is SheetEdit.MaxHitPoints -> R.string.max_hit_points
                    },
                ),
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                when (edit) {
                    is SheetEdit.Class -> ClassFields(edit, actions)
                    is SheetEdit.MaxHitPoints -> NumberField(
                        value = edit.value,
                        onValueChange = actions.onMaxHitPointsChange,
                        label = stringResource(R.string.max_hit_points),
                        range = CharacterRules.MAX_HIT_POINTS_RANGE,
                        showError = !edit.isValid,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
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
            TextButton(onClick = actions.onConfirm, enabled = edit.isValid && !isSaving) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = actions.onDismiss, enabled = !isSaving) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}

@Composable
private fun ClassFields(edit: SheetEdit.Class, actions: SheetEditActions) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ClassDropdown(
            selected = edit.characterClass,
            onSelect = actions.onClassChange,
            showError = edit.characterClass == null,
            modifier = Modifier.fillMaxWidth(),
        )
        NumberField(
            value = edit.level,
            onValueChange = actions.onLevelChange,
            label = stringResource(R.string.level),
            range = CharacterRules.LEVEL_RANGE,
            showError = !edit.isLevelValid,
            modifier = Modifier.fillMaxWidth(),
        )
        // Without the catalog (or for a class without subclasses) there's nothing to choose.
        val subclassLevel = edit.subclassLevel
        if (subclassLevel != null && edit.subclassOptions.isNotEmpty()) {
            SubclassDropdown(
                selected = edit.subclass,
                options = edit.subclassOptions,
                enabled = edit.canChooseSubclass,
                subclassLevel = subclassLevel,
                onSelect = actions.onSubclassChange,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ClassEditDialogPreview() {
    DnDAppTheme {
        SheetEditDialog(
            edit = SheetEdit.Class(
                CharacterClass.CLERIC,
                "15",
                subclass = Subclass("LIFE", "Life Domain"),
                catalog = listOf(ClassInfo(CharacterClass.CLERIC, 1, listOf(Subclass("LIFE", "Life Domain")))),
            ),
            isSaving = false,
            error = null,
            actions = SheetEditActions(),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MaxHitPointsEditDialogErrorPreview() {
    DnDAppTheme {
        SheetEditDialog(
            edit = SheetEdit.MaxHitPoints("123"),
            isSaving = false,
            error = RepositoryError.Network,
            actions = SheetEditActions(),
        )
    }
}
