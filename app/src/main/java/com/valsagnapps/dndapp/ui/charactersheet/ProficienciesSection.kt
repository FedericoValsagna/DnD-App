package com.valsagnapps.dndapp.ui.charactersheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.valsagnapps.dndapp.R
import com.valsagnapps.dndapp.domain.CharacterClass
import com.valsagnapps.dndapp.domain.Proficiencies
import com.valsagnapps.dndapp.domain.Proficiency
import com.valsagnapps.dndapp.domain.Skill
import com.valsagnapps.dndapp.domain.SkillChoice
import com.valsagnapps.dndapp.domain.ToolChoice
import com.valsagnapps.dndapp.ui.common.nameRes

/** Armor, weapons and tools; "—" for each one if the server didn't send them or there are none. */
@Composable
internal fun ProficienciesSection(proficiencies: Proficiencies?, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ProficiencyLine(
            label = stringResource(R.string.armor),
            names = proficiencies?.armor.orEmpty().map { stringResource(it.nameRes()) },
        )
        ProficiencyLine(
            label = stringResource(R.string.weapons),
            names = proficiencies?.weapons.orEmpty().map { stringResource(it.nameRes()) },
        )
        ProficiencyLine(
            label = stringResource(R.string.tools),
            names = proficiencies?.tools.orEmpty().map { stringResource(it.nameRes()) } +
                proficiencies?.toolChoices.orEmpty().map { toolChoiceText(it) },
        )
    }
}

@Composable
private fun ProficiencyLine(label: String, names: List<String>) {
    Column {
        Text(label, style = MaterialTheme.typography.labelLarge)
        Text(
            text = names.ifEmpty { listOf(stringResource(R.string.missing_value)) }
                .joinToString(stringResource(R.string.list_separator)),
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

/** E.g. "3 × Musical instrument (a elegir)". */
@Composable
private fun toolChoiceText(choice: ToolChoice): String = stringResource(
    R.string.tool_choice,
    choice.count,
    choice.options.map { stringResource(it.nameRes()) }.joinToString(stringResource(R.string.tool_choice_separator)),
)

/** One line per class that offers skills, e.g. "Fighter: elegí 2 de las marcadas con ✦ (tenés 1)". */
@Composable
internal fun SkillChoicesHint(
    skillChoices: Map<CharacterClass, SkillChoice>,
    proficiencies: Map<Skill, Proficiency>,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        skillChoices.filterValues { it.count > 0 }.forEach { (characterClass, choice) ->
            val className = stringResource(characterClass.nameRes())
            Text(
                text = if (choice.isAnySkill) {
                    stringResource(R.string.skill_choice_any, className, choice.count)
                } else {
                    stringResource(
                        R.string.skill_choice_from_list,
                        className,
                        choice.count,
                        choice.chosenIn(proficiencies),
                    )
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
