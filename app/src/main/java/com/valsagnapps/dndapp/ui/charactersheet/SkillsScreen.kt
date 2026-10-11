package com.valsagnapps.dndapp.ui.charactersheet

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.valsagnapps.dndapp.R
import com.valsagnapps.dndapp.domain.Ability
import com.valsagnapps.dndapp.domain.Proficiency
import com.valsagnapps.dndapp.domain.SavingThrow
import com.valsagnapps.dndapp.domain.Skill
import com.valsagnapps.dndapp.ui.common.ClassSkillMarker
import com.valsagnapps.dndapp.ui.common.ProficiencyMarker
import com.valsagnapps.dndapp.ui.common.ProficiencyMenu
import com.valsagnapps.dndapp.ui.common.errorMessage
import com.valsagnapps.dndapp.ui.common.nameRes
import com.valsagnapps.dndapp.ui.common.shortNameRes
import com.valsagnapps.dndapp.ui.theme.SheetType

// Two columns of 9 skills each. CON has no skills: its box shows only the header, so its score is somewhere.
private val leftColumn = listOf(Ability.STRENGTH, Ability.DEXTERITY, Ability.INTELLIGENCE)
private val rightColumn = listOf(Ability.CONSTITUTION, Ability.WISDOM, Ability.CHARISMA)

/** Weight of an ability box's header, in skill rows: the boxes stretch in proportion to their rows. */
private const val HEADER_WEIGHT = 1.2f

/**
 * Saving throws, the 18 skills in a box per ability and Passive Perception, without scrolling.
 * In edit mode the hint takes the place of Passive Perception (nothing moves), the class skills get
 * their ✦, tapping a skill opens the proficiency menu and tapping an ability opens the scores dialog.
 */
@Composable
internal fun SkillsScreen(
    state: CharacterSheetUiState.Content,
    onSkillProficiencyChange: (Skill, Proficiency) -> Unit,
    editActions: SheetEditActions,
    modifier: Modifier = Modifier,
) {
    val character = state.character
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        SectionLabel(stringResource(R.string.saving_throws))
        SavingThrows(character.savingThrows)
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            listOf(leftColumn, rightColumn).forEach { abilities ->
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    abilities.forEach { ability ->
                        AbilityBox(ability, state, onSkillProficiencyChange, editActions)
                    }
                }
            }
        }
        if (state.isEditing) {
            SkillsEditHint(state)
        } else {
            StatStrip(
                label = stringResource(R.string.passive_perception),
                value = character.passivePerception?.toString() ?: stringResource(R.string.missing_value),
            )
        }
    }
}

@Composable
private fun SavingThrows(savingThrows: Map<Ability, SavingThrow>, modifier: Modifier = Modifier) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Ability.entries.forEach { ability ->
            SaveCell(ability, savingThrows[ability], Modifier.weight(1f))
        }
    }
}

/** Proficient saves have a border and their value in primary; the screen reader says "Proficient". */
@Composable
private fun SaveCell(ability: Ability, savingThrow: SavingThrow?, modifier: Modifier = Modifier) {
    val proficient = savingThrow != null && savingThrow.proficiency != Proficiency.NONE
    val proficiencyName = stringResource((savingThrow?.proficiency ?: Proficiency.NONE).nameRes())
    Surface(
        modifier = modifier.semantics(mergeDescendants = true) { stateDescription = proficiencyName },
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        border = if (proficient) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
    ) {
        Column(
            modifier = Modifier.padding(vertical = 6.dp, horizontal = 1.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(ability.shortNameRes()),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = savingThrow?.let { stringResource(R.string.signed_value, it.bonus) }
                    ?: stringResource(R.string.missing_value),
                style = SheetType.saveValue,
                color = if (proficient) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

/** An ability with its score and modifier, and below it the skills that use it. */
@Composable
private fun ColumnScope.AbilityBox(
    ability: Ability,
    state: CharacterSheetUiState.Content,
    onSkillProficiencyChange: (Skill, Proficiency) -> Unit,
    editActions: SheetEditActions,
) {
    val skills = Skill.entries.filter { it.ability == ability }
    Column(
        modifier = Modifier
            .then(if (skills.isEmpty()) Modifier else Modifier.weight(skills.size + HEADER_WEIGHT))
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            .padding(start = 8.dp, end = 8.dp, top = 6.dp, bottom = if (skills.isEmpty()) 4.dp else 2.dp),
    ) {
        AbilityBoxHeader(
            ability = ability,
            state = state,
            onEdit = { editActions.onStartEdit(SheetEditTarget.ABILITY_SCORES) }.takeIf { state.isEditing },
        )
        if (skills.isNotEmpty()) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        skills.forEachIndexed { index, skill ->
            if (index > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            AbilityBoxSkill(
                skill = skill,
                state = state,
                onProficiencyChange = { onSkillProficiencyChange(skill, it) },
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 34.dp),
            )
        }
    }
}

/** "CHA 16" small and red on the left, the modifier big on the right: the modifier is what's rolled. */
@Composable
private fun AbilityBoxHeader(
    ability: Ability,
    state: CharacterSheetUiState.Content,
    onEdit: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val score = state.character.abilities[ability]
    val missing = stringResource(R.string.missing_value)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onEdit != null) Modifier.clickable(onClick = onEdit) else Modifier)
            .padding(bottom = 3.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = stringResource(ability.shortNameRes()),
            style = SheetType.sectionLabel,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.alignByBaseline(),
        )
        Text(
            text = score?.score?.toString() ?: missing,
            style = SheetType.abilityScore,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .weight(1f)
                .alignByBaseline(),
        )
        Text(
            text = score?.let { stringResource(R.string.signed_value, it.modifier) } ?: missing,
            style = SheetType.abilityModifier,
            modifier = Modifier.alignByBaseline(),
        )
    }
}

/** Only in edit mode can it be tapped (to pick the proficiency) and does it show the ✦ of a class skill. */
@Composable
private fun AbilityBoxSkill(
    skill: Skill,
    state: CharacterSheetUiState.Content,
    onProficiencyChange: (Proficiency) -> Unit,
    modifier: Modifier = Modifier,
) {
    val value = state.character.skills[skill]
    val proficiency = value?.proficiency ?: Proficiency.NONE
    var menuExpanded by remember { mutableStateOf(false) }
    Box(modifier) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .clickable(enabled = state.isEditing && !state.isSavingSkills) { menuExpanded = true },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            ProficiencyMarker(proficiency, Modifier.width(14.dp))
            Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(skill.nameRes()),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f, fill = false),
                )
                if (state.isEditing && skill in state.character.classSkills) {
                    ClassSkillMarker(Modifier.padding(start = 4.dp))
                }
            }
            ValueChip(
                value?.let { stringResource(R.string.signed_value, it.bonus) }
                    ?: stringResource(R.string.missing_value),
            )
        }
        ProficiencyMenu(
            expanded = menuExpanded,
            current = proficiency,
            onDismiss = { menuExpanded = false },
            onProficiencyChange = onProficiencyChange,
        )
    }
}

/**
 * In the place of Passive Perception: what can be tapped and the skills to choose per class. Saving
 * shows a progress bar, and a failed save its error.
 */
@Composable
private fun SkillsEditHint(state: CharacterSheetUiState.Content, modifier: Modifier = Modifier) {
    StripBox(MaterialTheme.colorScheme.secondaryContainer, modifier) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(
                painter = painterResource(R.drawable.ic_edit),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(18.dp),
            )
            Column(Modifier.weight(1f)) {
                val error = state.skillsError
                if (error != null) {
                    Text(
                        text = stringResource(R.string.skills_save_error, errorMessage(error)),
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.error,
                    )
                } else {
                    Text(
                        text = stringResource(R.string.skills_edit_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                    SkillChoicesHint(
                        skillChoices = state.character.skillChoices,
                        proficiencies = state.character.skillProficiencies,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                }
                if (state.isSavingSkills) {
                    LinearProgressIndicator(
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                    )
                }
            }
        }
    }
}
