package com.valsagnapps.dndapp.ui.charactersheet

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.valsagnapps.dndapp.data.RepositoryError
import com.valsagnapps.dndapp.domain.Ability
import com.valsagnapps.dndapp.domain.AbilityScore
import com.valsagnapps.dndapp.domain.ArmorProficiency
import com.valsagnapps.dndapp.domain.Character
import com.valsagnapps.dndapp.domain.CharacterClass
import com.valsagnapps.dndapp.domain.ClassLevel
import com.valsagnapps.dndapp.domain.HitDice
import com.valsagnapps.dndapp.domain.Proficiencies
import com.valsagnapps.dndapp.domain.Proficiency
import com.valsagnapps.dndapp.domain.SavingThrow
import com.valsagnapps.dndapp.domain.Skill
import com.valsagnapps.dndapp.domain.SkillChoice
import com.valsagnapps.dndapp.domain.SkillValue
import com.valsagnapps.dndapp.domain.Subclass
import com.valsagnapps.dndapp.domain.WeaponProficiency
import com.valsagnapps.dndapp.ui.common.ErrorContent
import com.valsagnapps.dndapp.ui.common.LoadingContent
import com.valsagnapps.dndapp.ui.theme.DnDAppTheme

/** [onNavigateToList] runs once the character has been deleted, to leave the sheet. */
@Composable
fun CharacterSheetScreen(viewModel: CharacterSheetViewModel, onNavigateToList: () -> Unit) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentOnNavigateToList by rememberUpdatedState(onNavigateToList)
    LaunchedEffect(uiState) {
        if (uiState == CharacterSheetUiState.Deleted) currentOnNavigateToList()
    }
    CharacterSheetContent(
        uiState = uiState,
        onRetry = viewModel::retry,
        onSkillProficiencyChange = viewModel::onSkillProficiencyChange,
        editActions = SheetEditActions(
            onToggleEditing = viewModel::onToggleEditing,
            onStartEdit = viewModel::onStartEdit,
            onClassChange = { viewModel.onEditInput(SheetEditInput.ClassChange(it)) },
            onLevelChange = { viewModel.onEditInput(SheetEditInput.LevelChange(it)) },
            onSubclassChange = { viewModel.onEditInput(SheetEditInput.SubclassChange(it)) },
            onMaxHitPointsChange = { viewModel.onEditInput(SheetEditInput.MaxHitPointsChange(it)) },
            onNameChange = { viewModel.onEditInput(SheetEditInput.NameChange(it)) },
            onAbilityScoreChange = { ability, value ->
                viewModel.onEditInput(SheetEditInput.AbilityScoreChange(ability, value))
            },
            onConfirm = viewModel::onConfirmEdit,
            onDismiss = viewModel::onDismissEdit,
        ),
    )
}

/** Opens on Skills ([initialDestination]) until there's a Combate screen. */
@Composable
fun CharacterSheetContent(
    uiState: CharacterSheetUiState,
    onRetry: () -> Unit,
    onSkillProficiencyChange: (Skill, Proficiency) -> Unit,
    modifier: Modifier = Modifier,
    editActions: SheetEditActions = SheetEditActions(),
    initialDestination: SheetDestination = SheetDestination.SKILLS,
) {
    var destination by rememberSaveable { mutableStateOf(initialDestination) }
    val content = uiState as? CharacterSheetUiState.Content
    Scaffold(
        modifier = modifier,
        topBar = { content?.let { CharacterHeader(it, editActions) } },
        bottomBar = { if (content != null) SheetBottomNav(destination, onSelect = { destination = it }) },
    ) { innerPadding ->
        val contentModifier = Modifier.padding(innerPadding)
        when (uiState) {
            // Deleted only lasts until the screen goes back to the list.
            CharacterSheetUiState.Loading, CharacterSheetUiState.Deleted -> LoadingContent(contentModifier)
            is CharacterSheetUiState.Error -> ErrorContent(uiState.error, onRetry, contentModifier)
            is CharacterSheetUiState.Content -> {
                uiState.edit?.let { SheetEditDialog(it, uiState.isSavingEdit, uiState.editError, editActions) }
                when (destination) {
                    SheetDestination.SKILLS ->
                        SkillsScreen(uiState, onSkillProficiencyChange, editActions, contentModifier)
                    SheetDestination.CHARACTER -> CharacterScreen(uiState, editActions, contentModifier)
                }
            }
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
    classes = listOf(ClassLevel(CharacterClass.FIGHTER, 5, Subclass("CHAMPION", "Champion"))),
    maxHitPoints = 44,
    hitDice = listOf(HitDice(die = 10, count = 5)),
    savingThrows = Ability.entries.associateWith { SavingThrow(Proficiency.NONE, 0) } +
        mapOf(
            Ability.STRENGTH to SavingThrow(Proficiency.PROFICIENT, 6),
            Ability.CONSTITUTION to SavingThrow(Proficiency.PROFICIENT, 5),
        ),
    skillChoices = mapOf(
        CharacterClass.FIGHTER to SkillChoice(
            count = 2,
            options = setOf(Skill.ACROBATICS, Skill.ATHLETICS, Skill.HISTORY, Skill.PERCEPTION, Skill.SURVIVAL),
        ),
    ),
    proficiencies = Proficiencies(
        armor = ArmorProficiency.entries,
        weapons = listOf(WeaponProficiency.SIMPLE, WeaponProficiency.MARTIAL),
    ),
)

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun CharacterSheetContentPreview() {
    DnDAppTheme {
        CharacterSheetContent(CharacterSheetUiState.Content(previewCharacter), {}, { _, _ -> })
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun CharacterSheetEditingPreview() {
    DnDAppTheme {
        CharacterSheetContent(CharacterSheetUiState.Content(previewCharacter, isEditing = true), {}, { _, _ -> })
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun CharacterSheetCharacterPreview() {
    DnDAppTheme {
        CharacterSheetContent(
            uiState = CharacterSheetUiState.Content(previewCharacter),
            onRetry = {},
            onSkillProficiencyChange = { _, _ -> },
            initialDestination = SheetDestination.CHARACTER,
        )
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun CharacterSheetSkillsErrorPreview() {
    DnDAppTheme {
        CharacterSheetContent(
            uiState = CharacterSheetUiState.Content(
                previewCharacter,
                isEditing = true,
                skillsError = RepositoryError.Network,
            ),
            onRetry = {},
            onSkillProficiencyChange = { _, _ -> },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CharacterSheetNotFoundPreview() {
    DnDAppTheme {
        CharacterSheetContent(CharacterSheetUiState.Error(RepositoryError.NotFound), {}, { _, _ -> })
    }
}
