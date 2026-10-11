package com.valsagnapps.dndapp.ui.charactersheet

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.valsagnapps.dndapp.R
import com.valsagnapps.dndapp.domain.ClassLevel
import com.valsagnapps.dndapp.ui.common.classSummary
import com.valsagnapps.dndapp.ui.common.hitDiceSummary

/** The internal tabs of Personaje: what's seldom used goes here instead of adding destinations. */
enum class CharacterTab(@param:StringRes val titleRes: Int) {
    PROFICIENCIES(R.string.proficiencies),
    FEATURES(R.string.features),
}

/**
 * Personaje: the class block always on top (class with its levels, PB, Max HP, Hit Dice, Speed) and below
 * it the internal tabs, each with its own scroll. In edit mode the class, the max HP and the speed can be
 * tapped, and the hint goes at the foot, as in Skills.
 */
@Composable
internal fun CharacterScreen(
    state: CharacterSheetUiState.Content,
    editActions: SheetEditActions,
    modifier: Modifier = Modifier,
) {
    var selected by rememberSaveable { mutableStateOf(CharacterTab.PROFICIENCIES) }
    Column(modifier.fillMaxSize()) {
        ClassBlock(state, editActions, Modifier.padding(12.dp))
        PrimaryTabRow(selectedTabIndex = selected.ordinal) {
            CharacterTab.entries.forEach { tab ->
                Tab(
                    selected = tab == selected,
                    onClick = { selected = tab },
                    text = { Text(stringResource(tab.titleRes)) },
                )
            }
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            when (selected) {
                CharacterTab.PROFICIENCIES -> ProficienciesSection(state.character.proficiencies)
                CharacterTab.FEATURES -> FeaturesSection(state.character)
            }
        }
        if (state.isEditing) {
            CharacterEditHint(Modifier.padding(12.dp))
        }
    }
}

@Composable
private fun ClassBlock(
    state: CharacterSheetUiState.Content,
    editActions: SheetEditActions,
    modifier: Modifier = Modifier,
) {
    val character = state.character
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ClassSummary(
            classes = character.classes,
            onEdit = {
                editActions.onStartEdit(SheetEditTarget.CLASS)
            }.takeIf { state.isEditing && state.canEditClass },
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCell(
                label = stringResource(R.string.proficiency_bonus_short),
                value = stringResource(R.string.signed_value, character.proficiencyBonus),
                modifier = Modifier.weight(1f),
            )
            StatCell(
                label = stringResource(R.string.max_hit_points),
                value = character.maxHitPoints?.toString() ?: stringResource(R.string.missing_value),
                onClick = { editActions.onStartEdit(SheetEditTarget.MAX_HIT_POINTS) }.takeIf { state.isEditing },
                modifier = Modifier.weight(1f),
            )
            StatCell(
                label = stringResource(R.string.hit_dice),
                value = hitDiceSummary(character.hitDice),
                modifier = Modifier.weight(1f),
            )
            // With the class bonuses; what's edited is the base.
            StatCell(
                label = stringResource(R.string.speed),
                value = character.speed?.toString() ?: stringResource(R.string.missing_value),
                onClick = { editActions.onStartEdit(SheetEditTarget.SPEED) }
                    .takeIf { state.isEditing && character.baseSpeed != null },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

/** The classes and levels; tapping them opens the class dialog when [onEdit] is not null. */
@Composable
private fun ClassSummary(classes: List<ClassLevel>, onEdit: (() -> Unit)?, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.then(if (onEdit != null) Modifier.clickable(onClick = onEdit) else Modifier),
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

@Composable
private fun CharacterEditHint(modifier: Modifier = Modifier) {
    StripBox(MaterialTheme.colorScheme.secondaryContainer, modifier) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(
                painter = painterResource(R.drawable.ic_edit),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = stringResource(R.string.character_edit_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }
    }
}
