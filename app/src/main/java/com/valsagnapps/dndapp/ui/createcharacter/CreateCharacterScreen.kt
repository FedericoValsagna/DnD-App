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
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.valsagnapps.dndapp.R
import com.valsagnapps.dndapp.data.RepositoryError
import com.valsagnapps.dndapp.domain.Ability
import com.valsagnapps.dndapp.domain.CharacterClass
import com.valsagnapps.dndapp.domain.CharacterRules
import com.valsagnapps.dndapp.domain.Proficiency
import com.valsagnapps.dndapp.domain.Skill
import com.valsagnapps.dndapp.ui.common.BackButton
import com.valsagnapps.dndapp.ui.common.ClassDropdown
import com.valsagnapps.dndapp.ui.common.NumberField
import com.valsagnapps.dndapp.ui.common.SkillRow
import com.valsagnapps.dndapp.ui.common.errorMessage
import com.valsagnapps.dndapp.ui.common.nameRes
import com.valsagnapps.dndapp.ui.theme.DnDAppTheme

@Composable
fun CreateCharacterScreen(
    viewModel: CreateCharacterViewModel,
    onNavigateToCharacter: (id: String) -> Unit,
    onBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentOnNavigateToCharacter by rememberUpdatedState(onNavigateToCharacter)
    LaunchedEffect(uiState.createdCharacterId) {
        uiState.createdCharacterId?.let(currentOnNavigateToCharacter)
    }
    CreateCharacterContent(
        uiState = uiState,
        onNameChange = viewModel::onNameChange,
        onClassChange = viewModel::onClassChange,
        onLevelChange = viewModel::onLevelChange,
        onMaxHitPointsChange = viewModel::onMaxHitPointsChange,
        onAbilityScoreChange = viewModel::onAbilityScoreChange,
        onSkillProficiencyChange = viewModel::onSkillProficiencyChange,
        onSave = viewModel::onSave,
        onBack = onBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCharacterContent(
    uiState: CreateCharacterUiState,
    onNameChange: (String) -> Unit,
    onClassChange: (CharacterClass) -> Unit,
    onLevelChange: (String) -> Unit,
    onMaxHitPointsChange: (String) -> Unit,
    onAbilityScoreChange: (Ability, String) -> Unit,
    onSkillProficiencyChange: (Skill, Proficiency) -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val showErrors = uiState.showValidationErrors
    Scaffold(
        modifier = modifier,
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
            NameField(
                name = uiState.name,
                onNameChange = onNameChange,
                showError = showErrors && !uiState.isNameValid,
            )
            ClassFields(
                uiState = uiState,
                onClassChange = onClassChange,
                onLevelChange = onLevelChange,
                onMaxHitPointsChange = onMaxHitPointsChange,
            )
            AbilityScoreFields(uiState = uiState, onAbilityScoreChange = onAbilityScoreChange)
            SkillFields(skills = uiState.skills, onSkillProficiencyChange = onSkillProficiencyChange)
            uiState.saveError?.let { error ->
                Text(
                    text = errorMessage(error),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            SaveButton(isSaving = uiState.isSaving, onSave = onSave)
        }
    }
}

@Composable
private fun NameField(name: String, onNameChange: (String) -> Unit, showError: Boolean) {
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
        modifier = Modifier.fillMaxWidth(),
    )
}

/** Class, level and max HP. */
@Composable
private fun ClassFields(
    uiState: CreateCharacterUiState,
    onClassChange: (CharacterClass) -> Unit,
    onLevelChange: (String) -> Unit,
    onMaxHitPointsChange: (String) -> Unit,
) {
    val showErrors = uiState.showValidationErrors
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ClassDropdown(
            selected = uiState.characterClass,
            onSelect = onClassChange,
            showError = showErrors && !uiState.isClassValid,
            modifier = Modifier.fillMaxWidth(),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            NumberField(
                value = uiState.level,
                onValueChange = onLevelChange,
                label = stringResource(R.string.level),
                range = CharacterRules.LEVEL_RANGE,
                showError = showErrors && !uiState.isLevelValid,
                modifier = Modifier.weight(1f),
            )
            NumberField(
                value = uiState.maxHitPoints,
                onValueChange = onMaxHitPointsChange,
                label = stringResource(R.string.max_hit_points),
                range = CharacterRules.MAX_HIT_POINTS_RANGE,
                showError = showErrors && !uiState.isMaxHitPointsValid,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun AbilityScoreFields(uiState: CreateCharacterUiState, onAbilityScoreChange: (Ability, String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(stringResource(R.string.abilities), style = MaterialTheme.typography.titleMedium)
        // Two columns: STR/DEX, CON/INT, WIS/CHA.
        Ability.entries.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { ability ->
                    NumberField(
                        value = uiState.abilityScores[ability].orEmpty(),
                        onValueChange = { onAbilityScoreChange(ability, it) },
                        label = stringResource(ability.nameRes()),
                        range = CharacterRules.ABILITY_SCORE_RANGE,
                        showError = uiState.showValidationErrors && !uiState.isAbilityScoreValid(ability),
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun SkillFields(skills: Map<Skill, Proficiency>, onSkillProficiencyChange: (Skill, Proficiency) -> Unit) {
    Column {
        Text(stringResource(R.string.skills), style = MaterialTheme.typography.titleMedium)
        Skill.entries.forEach { skill ->
            SkillRow(
                skill = skill,
                proficiency = skills[skill] ?: Proficiency.NONE,
                onProficiencyChange = { onSkillProficiencyChange(skill, it) },
            )
        }
    }
}

@Composable
private fun SaveButton(isSaving: Boolean, onSave: () -> Unit) {
    Button(
        onClick = onSave,
        enabled = !isSaving,
        modifier = Modifier.fillMaxWidth(),
    ) {
        if (isSaving) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
        } else {
            Text(stringResource(R.string.create))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CreateCharacterContentPreview() {
    DnDAppTheme {
        CreateCharacterContent(
            uiState = CreateCharacterUiState(
                name = "Tordek",
                characterClass = CharacterClass.FIGHTER,
                level = "5",
                maxHitPoints = "44",
                skills = mapOf(Skill.ATHLETICS to Proficiency.PROFICIENT),
            ),
            onNameChange = {},
            onClassChange = {},
            onLevelChange = {},
            onMaxHitPointsChange = {},
            onAbilityScoreChange = { _, _ -> },
            onSkillProficiencyChange = { _, _ -> },
            onSave = {},
            onBack = {},
        )
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
            onClassChange = {},
            onLevelChange = {},
            onMaxHitPointsChange = {},
            onAbilityScoreChange = { _, _ -> },
            onSkillProficiencyChange = { _, _ -> },
            onSave = {},
            onBack = {},
        )
    }
}
