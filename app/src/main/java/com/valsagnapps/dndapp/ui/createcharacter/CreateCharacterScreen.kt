package com.valsagnapps.dndapp.ui.createcharacter

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.valsagnapps.dndapp.R
import com.valsagnapps.dndapp.data.RepositoryError
import com.valsagnapps.dndapp.domain.Ability
import com.valsagnapps.dndapp.domain.CharacterRules
import com.valsagnapps.dndapp.ui.common.BackButton
import com.valsagnapps.dndapp.ui.common.errorMessage
import com.valsagnapps.dndapp.ui.common.nameRes
import com.valsagnapps.dndapp.ui.theme.DnDAppTheme

@Composable
fun CreateCharacterScreen(
    viewModel: CreateCharacterViewModel,
    onCreated: (id: String) -> Unit,
    onBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(uiState.createdCharacterId) {
        uiState.createdCharacterId?.let(onCreated)
    }
    CreateCharacterContent(
        uiState = uiState,
        onNameChange = viewModel::onNameChange,
        onLevelChange = viewModel::onLevelChange,
        onAbilityScoreChange = viewModel::onAbilityScoreChange,
        onSave = viewModel::onSave,
        onBack = onBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCharacterContent(
    uiState: CreateCharacterUiState,
    onNameChange: (String) -> Unit,
    onLevelChange: (String) -> Unit,
    onAbilityScoreChange: (Ability, String) -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit,
) {
    val showErrors = uiState.showValidationErrors
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.create_character_title)) },
                navigationIcon = { BackButton(onBack) },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                value = uiState.name,
                onValueChange = onNameChange,
                label = { Text(stringResource(R.string.name)) },
                isError = showErrors && !uiState.isNameValid,
                supportingText = if (showErrors && !uiState.isNameValid) {
                    { Text(stringResource(R.string.name_error, CharacterRules.NAME_MAX_LENGTH)) }
                } else null,
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                modifier = Modifier.fillMaxWidth(),
            )
            NumberField(
                value = uiState.level,
                onValueChange = onLevelChange,
                label = stringResource(R.string.level),
                range = CharacterRules.LEVEL_RANGE,
                showError = showErrors && !uiState.isLevelValid,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(stringResource(R.string.abilities), style = MaterialTheme.typography.titleMedium)
            Ability.entries.chunked(2).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    row.forEach { ability ->
                        NumberField(
                            value = uiState.abilityScores[ability].orEmpty(),
                            onValueChange = { onAbilityScoreChange(ability, it) },
                            label = stringResource(ability.nameRes()),
                            range = CharacterRules.ABILITY_SCORE_RANGE,
                            showError = showErrors && !uiState.isAbilityScoreValid(ability),
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
            uiState.saveError?.let { error ->
                Text(
                    text = errorMessage(error),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Button(
                onClick = onSave,
                enabled = !uiState.isSaving,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text(stringResource(R.string.create))
                }
            }
        }
    }
}

@Composable
private fun NumberField(
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
        } else null,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier,
    )
}

@Preview(showBackground = true)
@Composable
private fun CreateCharacterContentPreview() {
    DnDAppTheme {
        CreateCharacterContent(CreateCharacterUiState(name = "Tordek"), {}, {}, { _, _ -> }, {}, {})
    }
}

@Preview(showBackground = true)
@Composable
private fun CreateCharacterErrorsPreview() {
    DnDAppTheme {
        CreateCharacterContent(
            uiState = CreateCharacterUiState(
                level = "25",
                showValidationErrors = true,
                saveError = RepositoryError.Network,
            ),
            onNameChange = {},
            onLevelChange = {},
            onAbilityScoreChange = { _, _ -> },
            onSave = {},
            onBack = {},
        )
    }
}
