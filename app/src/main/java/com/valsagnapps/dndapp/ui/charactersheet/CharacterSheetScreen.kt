package com.valsagnapps.dndapp.ui.charactersheet

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.valsagnapps.dndapp.R
import com.valsagnapps.dndapp.data.RepositoryError
import com.valsagnapps.dndapp.domain.Ability
import com.valsagnapps.dndapp.domain.AbilityScore
import com.valsagnapps.dndapp.domain.Character
import com.valsagnapps.dndapp.domain.CharacterClass
import com.valsagnapps.dndapp.domain.ClassLevel
import com.valsagnapps.dndapp.domain.HitDice
import com.valsagnapps.dndapp.domain.Proficiency
import com.valsagnapps.dndapp.domain.SavingThrow
import com.valsagnapps.dndapp.domain.Skill
import com.valsagnapps.dndapp.domain.SkillValue
import com.valsagnapps.dndapp.ui.common.BackButton
import com.valsagnapps.dndapp.ui.common.ErrorContent
import com.valsagnapps.dndapp.ui.common.LoadingContent
import com.valsagnapps.dndapp.ui.common.ProficiencyMarker
import com.valsagnapps.dndapp.ui.common.SkillRow
import com.valsagnapps.dndapp.ui.common.classSummary
import com.valsagnapps.dndapp.ui.common.errorMessage
import com.valsagnapps.dndapp.ui.common.hitDiceSummary
import com.valsagnapps.dndapp.ui.common.nameRes
import com.valsagnapps.dndapp.ui.theme.DnDAppTheme

@Composable
fun CharacterSheetScreen(viewModel: CharacterSheetViewModel, onBack: () -> Unit) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    CharacterSheetContent(
        uiState = uiState,
        onRetry = viewModel::retry,
        onSkillProficiencyChange = viewModel::onSkillProficiencyChange,
        onBack = onBack,
        editActions = SheetEditActions(
            onEditClass = viewModel::onEditClass,
            onEditMaxHitPoints = viewModel::onEditMaxHitPoints,
            onClassChange = viewModel::onEditClassChange,
            onLevelChange = viewModel::onEditLevelChange,
            onMaxHitPointsChange = viewModel::onEditMaxHitPointsChange,
            onConfirm = viewModel::onConfirmEdit,
            onDismiss = viewModel::onDismissEdit,
        ),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterSheetContent(
    uiState: CharacterSheetUiState,
    onRetry: () -> Unit,
    onSkillProficiencyChange: (Skill, Proficiency) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    editActions: SheetEditActions = SheetEditActions(),
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    if (uiState is CharacterSheetUiState.Content) Text(uiState.character.name)
                },
                navigationIcon = { BackButton(onBack) },
            )
        },
    ) { innerPadding ->
        val contentModifier = Modifier.padding(innerPadding)
        when (uiState) {
            CharacterSheetUiState.Loading -> LoadingContent(contentModifier)
            is CharacterSheetUiState.Error -> ErrorContent(uiState.error, onRetry, contentModifier)
            is CharacterSheetUiState.Content ->
                CharacterSheet(uiState, onSkillProficiencyChange, editActions, contentModifier)
        }
    }
}

@Composable
private fun CharacterSheet(
    state: CharacterSheetUiState.Content,
    onSkillProficiencyChange: (Skill, Proficiency) -> Unit,
    editActions: SheetEditActions,
    modifier: Modifier = Modifier,
) {
    val character = state.character
    state.edit?.let { SheetEditDialog(it, state.isSavingEdit, state.editError, editActions) }
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ClassSummary(character.classes, onEdit = editActions.onEditClass.takeIf { state.canEditClass })
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(
                label = stringResource(R.string.level),
                value = character.level.toString(),
                modifier = Modifier.weight(1f),
            )
            StatCard(
                label = stringResource(R.string.proficiency_bonus),
                value = stringResource(R.string.signed_value, character.proficiencyBonus),
                modifier = Modifier.weight(1f),
            )
            StatCard(
                label = stringResource(R.string.passive_perception),
                value = character.passivePerception?.toString() ?: stringResource(R.string.missing_value),
                modifier = Modifier.weight(1f),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(
                label = stringResource(R.string.max_hit_points),
                value = character.maxHitPoints?.toString() ?: stringResource(R.string.missing_value),
                onClick = editActions.onEditMaxHitPoints,
                modifier = Modifier.weight(1f),
            )
            StatCard(
                label = stringResource(R.string.hit_dice),
                value = hitDiceSummary(character.hitDice),
                modifier = Modifier.weight(1f),
            )
        }
        Text(stringResource(R.string.abilities), style = MaterialTheme.typography.titleMedium)
        // Two columns: STR/DEX, CON/INT, WIS/CHA.
        Ability.entries.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { ability ->
                    AbilityCard(
                        ability = ability,
                        score = character.abilities[ability],
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
        SavingThrowsSection(character.savingThrows)
        SkillsSection(state, onSkillProficiencyChange)
    }
}

@Composable
private fun SavingThrowsSection(savingThrows: Map<Ability, SavingThrow>, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(stringResource(R.string.saving_throws), style = MaterialTheme.typography.titleMedium)
        Ability.entries.forEach { ability ->
            val savingThrow = savingThrows[ability]
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ProficiencyMarker(savingThrow?.proficiency ?: Proficiency.NONE)
                Text(stringResource(ability.nameRes()), style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.weight(1f))
                Text(
                    text = savingThrow?.let { stringResource(R.string.signed_value, it.bonus) }
                        ?: stringResource(R.string.missing_value),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }
    }
}

@Composable
private fun SkillsSection(
    state: CharacterSheetUiState.Content,
    onSkillProficiencyChange: (Skill, Proficiency) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        Text(stringResource(R.string.skills), style = MaterialTheme.typography.titleMedium)
        if (state.isSavingSkills) {
            LinearProgressIndicator(Modifier.fillMaxWidth().padding(top = 8.dp))
        }
        state.skillsError?.let { error ->
            Text(
                text = stringResource(R.string.skills_save_error, errorMessage(error)),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
        Skill.entries.forEach { skill ->
            val value = state.character.skills[skill]
            SkillRow(
                skill = skill,
                proficiency = value?.proficiency ?: Proficiency.NONE,
                onProficiencyChange = { onSkillProficiencyChange(skill, it) },
                trailing = value?.let { stringResource(R.string.signed_value, it.bonus) }
                    ?: stringResource(R.string.missing_value),
                enabled = !state.isSavingSkills,
            )
        }
    }
}

/** The classes and levels; tapping them opens the class dialog when [onEdit] is not null. */
@Composable
private fun ClassSummary(classes: List<ClassLevel>, onEdit: (() -> Unit)?, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.then(
            if (onEdit != null) Modifier.clickable(onClick = onEdit) else Modifier,
        ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(classSummary(classes), style = MaterialTheme.typography.titleLarge)
        if (onEdit != null) {
            Icon(
                painter = painterResource(R.drawable.ic_edit),
                contentDescription = stringResource(R.string.edit_class),
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

private val previewCharacter = Character(
    id = "1",
    name = "Tordek",
    level = 5,
    proficiencyBonus = 3,
    abilities = mapOf(
        Ability.STRENGTH to AbilityScore(16, 3),
        Ability.DEXTERITY to AbilityScore(12, 1),
        Ability.CONSTITUTION to AbilityScore(15, 2),
        Ability.INTELLIGENCE to AbilityScore(10, 0),
        Ability.WISDOM to AbilityScore(13, 1),
        Ability.CHARISMA to AbilityScore(8, -1),
    ),
    skills = Skill.entries.associateWith { SkillValue(Proficiency.NONE, 0) } +
        mapOf(
            Skill.ATHLETICS to SkillValue(Proficiency.PROFICIENT, 6),
            Skill.PERCEPTION to SkillValue(Proficiency.EXPERTISE, 7),
        ),
    passivePerception = 17,
    classes = listOf(ClassLevel(CharacterClass.FIGHTER, 5)),
    maxHitPoints = 44,
    hitDice = listOf(HitDice(die = 10, count = 5)),
    savingThrows = Ability.entries.associateWith { SavingThrow(Proficiency.NONE, 0) } +
        mapOf(
            Ability.STRENGTH to SavingThrow(Proficiency.PROFICIENT, 6),
            Ability.CONSTITUTION to SavingThrow(Proficiency.PROFICIENT, 5),
        ),
)

@Preview(showBackground = true)
@Composable
private fun CharacterSheetContentPreview() {
    DnDAppTheme {
        CharacterSheetContent(CharacterSheetUiState.Content(previewCharacter), {}, { _, _ -> }, {})
    }
}

@Preview(showBackground = true)
@Composable
private fun CharacterSheetSkillsErrorPreview() {
    DnDAppTheme {
        CharacterSheetContent(
            uiState = CharacterSheetUiState.Content(previewCharacter, skillsError = RepositoryError.Network),
            onRetry = {},
            onSkillProficiencyChange = { _, _ -> },
            onBack = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CharacterSheetNotFoundPreview() {
    DnDAppTheme {
        CharacterSheetContent(CharacterSheetUiState.Error(RepositoryError.NotFound), {}, { _, _ -> }, {})
    }
}
