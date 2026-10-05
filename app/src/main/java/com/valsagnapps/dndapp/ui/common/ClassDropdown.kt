package com.valsagnapps.dndapp.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.valsagnapps.dndapp.R
import com.valsagnapps.dndapp.domain.CharacterClass
import com.valsagnapps.dndapp.ui.theme.DnDAppTheme

/**
 * Field to pick one of the classes: tapping it opens a menu. [selected] is null until the user picks one.
 * The text field is read-only; a transparent layer on top takes the tap.
 */
@Composable
fun ClassDropdown(
    selected: CharacterClass?,
    onSelect: (CharacterClass) -> Unit,
    modifier: Modifier = Modifier,
    showError: Boolean = false,
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier) {
        OutlinedTextField(
            value = selected?.let { stringResource(it.nameRes()) }.orEmpty(),
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.character_class)) },
            trailingIcon = { Icon(painterResource(R.drawable.ic_arrow_drop_down), contentDescription = null) },
            isError = showError,
            supportingText = if (showError) {
                { Text(stringResource(R.string.class_error)) }
            } else {
                null
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Box(
            Modifier
                .matchParentSize()
                .clickable { expanded = true },
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            CharacterClass.entries.forEach { option ->
                DropdownMenuItem(
                    text = { Text(stringResource(option.nameRes())) },
                    onClick = {
                        expanded = false
                        onSelect(option)
                    },
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ClassDropdownPreview() {
    DnDAppTheme {
        ClassDropdown(selected = CharacterClass.CLERIC, onSelect = {})
    }
}
