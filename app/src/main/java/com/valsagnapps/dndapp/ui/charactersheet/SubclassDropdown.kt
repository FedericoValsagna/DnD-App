package com.valsagnapps.dndapp.ui.charactersheet

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
import com.valsagnapps.dndapp.domain.Subclass
import com.valsagnapps.dndapp.ui.theme.DnDAppTheme

/**
 * Field to pick the subclass among [options], or none. Below [subclassLevel] it's disabled and says at which
 * level it's chosen. Same structure as `ClassDropdown`: read-only text field with a tap layer on top.
 */
@Composable
fun SubclassDropdown(
    selected: Subclass?,
    options: List<Subclass>,
    enabled: Boolean,
    subclassLevel: Int,
    onSelect: (Subclass?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier) {
        OutlinedTextField(
            value = if (enabled) selected?.name ?: stringResource(R.string.no_subclass) else "",
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            label = { Text(stringResource(R.string.subclass)) },
            trailingIcon = { Icon(painterResource(R.drawable.ic_arrow_drop_down), contentDescription = null) },
            supportingText = if (enabled) {
                null
            } else {
                { Text(stringResource(R.string.subclass_level_hint, subclassLevel)) }
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        if (enabled) {
            Box(
                Modifier
                    .matchParentSize()
                    .clickable { expanded = true },
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            (listOf(null) + options).forEach { option ->
                DropdownMenuItem(
                    text = { Text(option?.name ?: stringResource(R.string.no_subclass)) },
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
private fun SubclassDropdownPreview() {
    DnDAppTheme {
        SubclassDropdown(
            selected = Subclass("LIFE", "Life Domain"),
            options = listOf(Subclass("LIFE", "Life Domain")),
            enabled = true,
            subclassLevel = 1,
            onSelect = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SubclassDropdownDisabledPreview() {
    DnDAppTheme {
        SubclassDropdown(selected = null, options = emptyList(), enabled = false, subclassLevel = 3, onSelect = {})
    }
}
