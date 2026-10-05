package com.valsagnapps.dndapp.ui.common

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.valsagnapps.dndapp.R
import com.valsagnapps.dndapp.ui.theme.DnDAppTheme

/** Numeric text field that shows the valid [range] as its error. */
@Composable
fun NumberField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    range: IntRange,
    showError: Boolean,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        isError = showError,
        supportingText = if (showError) {
            { Text(stringResource(R.string.range_error, range.first, range.last)) }
        } else {
            null
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier,
    )
}

@Preview(showBackground = true)
@Composable
private fun NumberFieldPreview() {
    DnDAppTheme {
        NumberField(value = "25", onValueChange = {}, label = "Level", range = 1..20, showError = true)
    }
}
