package com.valsagnapps.dndapp.ui.common

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import com.valsagnapps.dndapp.R
import com.valsagnapps.dndapp.domain.CharacterRules

/** The character's name, with the valid length as its error. */
@Composable
fun NameField(name: String, onNameChange: (String) -> Unit, showError: Boolean, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = name,
        onValueChange = onNameChange,
        label = { Text(stringResource(R.string.name)) },
        isError = showError,
        supportingText = if (showError) {
            { Text(stringResource(R.string.name_error, CharacterRules.NAME_MAX_LENGTH)) }
        } else {
            null
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
        modifier = modifier,
    )
}
