package com.valsagnapps.dndapp.ui.charactersheet

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.valsagnapps.dndapp.R
import com.valsagnapps.dndapp.ui.common.BackButton

/**
 * The character's name and the edit mode button. In edit mode the name can be tapped to rename the
 * character, and there's a button to delete it.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SheetTopBar(
    uiState: CharacterSheetUiState,
    onBack: () -> Unit,
    editActions: SheetEditActions,
    modifier: Modifier = Modifier,
) {
    val content = uiState as? CharacterSheetUiState.Content
    TopAppBar(
        modifier = modifier,
        title = {
            if (content != null) {
                SheetTitle(
                    name = content.character.name,
                    onEdit = { editActions.onStartEdit(SheetEditTarget.NAME) }.takeIf { content.isEditing },
                )
            }
        },
        navigationIcon = { BackButton(onBack) },
        actions = {
            if (content != null) {
                if (content.isEditing) {
                    IconButton(onClick = { editActions.onStartEdit(SheetEditTarget.DELETE_CHARACTER) }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_delete),
                            contentDescription = stringResource(R.string.delete_character),
                            tint = MaterialTheme.colorScheme.error,
                        )
                    }
                }
                EditModeButton(content.isEditing, editActions.onToggleEditing)
            }
        },
    )
}

/** The name; tapping it opens the name dialog when [onEdit] is not null. */
@Composable
private fun SheetTitle(name: String, onEdit: (() -> Unit)?, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.then(if (onEdit != null) Modifier.clickable(onClick = onEdit) else Modifier),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(name, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
        if (onEdit != null) {
            Icon(
                painter = painterResource(R.drawable.ic_edit),
                contentDescription = stringResource(R.string.edit_name),
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

/** Pencil to enter edit mode, check to leave it. */
@Composable
private fun EditModeButton(isEditing: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    IconButton(onClick = onClick, modifier = modifier) {
        Icon(
            painter = painterResource(if (isEditing) R.drawable.ic_check else R.drawable.ic_edit),
            contentDescription = stringResource(if (isEditing) R.string.finish_editing else R.string.edit_sheet),
        )
    }
}
