package com.valsagnapps.dndapp.ui.charactersheet

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.valsagnapps.dndapp.R
import com.valsagnapps.dndapp.ui.theme.SheetType

/**
 * Replaces the top app bar, the same on every screen of the sheet: the name, the HP and the menu.
 * There's no back button: the system's back goes to the list.
 */
@Composable
internal fun CharacterHeader(
    state: CharacterSheetUiState.Content,
    editActions: SheetEditActions,
    modifier: Modifier = Modifier,
) {
    Surface(modifier, color = MaterialTheme.colorScheme.surface) {
        // Edge to edge: the header goes below the status bar, as the top app bar did.
        Column(Modifier.windowInsetsPadding(WindowInsets.statusBars)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(start = 16.dp, end = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                CharacterName(
                    name = state.character.name,
                    onEdit = { editActions.onStartEdit(SheetEditTarget.NAME) }.takeIf { state.isEditing },
                    modifier = Modifier.weight(1f),
                )
                HitPoints(state.character.maxHitPoints)
                HeaderMenu(state.isEditing, editActions)
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        }
    }
}

/** The name; tapping it opens the name dialog when [onEdit] is not null. */
@Composable
private fun CharacterName(name: String, onEdit: (() -> Unit)?, modifier: Modifier = Modifier) {
    Box(modifier) {
        Row(
            modifier = if (onEdit != null) Modifier.clickable(onClick = onEdit) else Modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = name,
                style = SheetType.characterName,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false),
            )
            if (onEdit != null) {
                Icon(
                    painter = painterResource(R.drawable.ic_edit),
                    contentDescription = stringResource(R.string.edit_name),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

/** For now the max HP: the current HP comes with combat. */
@Composable
private fun HitPoints(maxHitPoints: Int?, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_favorite),
            contentDescription = stringResource(R.string.max_hit_points),
            tint = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = maxHitPoints?.toString() ?: stringResource(R.string.missing_value),
            style = SheetType.hpValue,
        )
    }
}

/** Short and long rest (disabled until rests exist), edit mode and, while editing, deleting the character. */
@Composable
private fun HeaderMenu(isEditing: Boolean, editActions: SheetEditActions, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier) {
        IconButton(onClick = { expanded = true }) {
            Icon(
                painter = painterResource(R.drawable.ic_more_vert),
                contentDescription = stringResource(R.string.sheet_menu),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(text = { Text(stringResource(R.string.short_rest)) }, onClick = {}, enabled = false)
            DropdownMenuItem(text = { Text(stringResource(R.string.long_rest)) }, onClick = {}, enabled = false)
            DropdownMenuItem(
                text = { Text(stringResource(if (isEditing) R.string.finish_editing else R.string.edit_sheet)) },
                onClick = {
                    expanded = false
                    editActions.onToggleEditing()
                },
            )
            if (isEditing) {
                DropdownMenuItem(
                    text = {
                        Text(stringResource(R.string.delete_character), color = MaterialTheme.colorScheme.error)
                    },
                    onClick = {
                        expanded = false
                        editActions.onStartEdit(SheetEditTarget.DELETE_CHARACTER)
                    },
                )
            }
        }
    }
}
