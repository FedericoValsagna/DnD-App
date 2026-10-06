package com.valsagnapps.dndapp.ui.charactersheet

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.valsagnapps.dndapp.R
import com.valsagnapps.dndapp.domain.Ability
import com.valsagnapps.dndapp.domain.Character
import com.valsagnapps.dndapp.domain.Proficiency
import com.valsagnapps.dndapp.domain.SavingThrow
import com.valsagnapps.dndapp.domain.Skill
import com.valsagnapps.dndapp.ui.common.ProficiencyMarker
import com.valsagnapps.dndapp.ui.common.SkillRow
import com.valsagnapps.dndapp.ui.common.errorMessage
import com.valsagnapps.dndapp.ui.common.nameRes

/** The sheet's tabs, below the header. Features and spells will come as new tabs. */
enum class SheetTab(@param:StringRes val titleRes: Int) {
    ABILITIES(R.string.abilities),
    SKILLS(R.string.skills),
    PROFICIENCIES(R.string.proficiencies),
}

@Composable
internal fun SheetTabs(
    state: CharacterSheetUiState.Content,
    onSkillProficiencyChange: (Skill, Proficiency) -> Unit,
    modifier: Modifier = Modifier,
) {
    var selected by rememberSaveable { mutableStateOf(SheetTab.ABILITIES) }
    Column(modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        PrimaryTabRow(selectedTabIndex = selected.ordinal) {
            SheetTab.entries.forEach { tab ->
                Tab(
                    selected = tab == selected,
                    onClick = { selected = tab },
                    text = { Text(stringResource(tab.titleRes)) },
                )
            }
        }
        when (selected) {
            SheetTab.ABILITIES -> AbilitiesTab(state.character)
            SheetTab.SKILLS -> SkillsTab(state, onSkillProficiencyChange)
            SheetTab.PROFICIENCIES -> ProficienciesSection(state.character.proficiencies)
        }
    }
}

@Composable
private fun AbilitiesTab(character: Character, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
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

/** The skills can be changed only in edit mode; the class suggestions (✦) are shown only then too. */
@Composable
private fun SkillsTab(
    state: CharacterSheetUiState.Content,
    onSkillProficiencyChange: (Skill, Proficiency) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        if (state.isEditing) {
            SkillChoicesHint(state.character.skillChoices, state.character.skillProficiencies)
        }
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
        val classSkills = if (state.isEditing) state.character.classSkills else emptySet()
        Skill.entries.forEach { skill ->
            val value = state.character.skills[skill]
            SkillRow(
                skill = skill,
                proficiency = value?.proficiency ?: Proficiency.NONE,
                onProficiencyChange = { onSkillProficiencyChange(skill, it) },
                trailing = value?.let { stringResource(R.string.signed_value, it.bonus) }
                    ?: stringResource(R.string.missing_value),
                enabled = state.isEditing && !state.isSavingSkills,
                isClassSkill = skill in classSkills,
            )
        }
    }
}
