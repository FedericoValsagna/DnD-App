package com.valsagnapps.dndapp.ui.charactersheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.valsagnapps.dndapp.R
import com.valsagnapps.dndapp.data.RepositoryError
import com.valsagnapps.dndapp.domain.Ability
import com.valsagnapps.dndapp.domain.AbilityScore
import com.valsagnapps.dndapp.domain.Character
import com.valsagnapps.dndapp.ui.common.BackButton
import com.valsagnapps.dndapp.ui.common.ErrorContent
import com.valsagnapps.dndapp.ui.common.LoadingContent
import com.valsagnapps.dndapp.ui.common.nameRes
import com.valsagnapps.dndapp.ui.theme.DnDAppTheme

@Composable
fun CharacterSheetScreen(viewModel: CharacterSheetViewModel, onBack: () -> Unit) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    CharacterSheetContent(uiState = uiState, onRetry = viewModel::retry, onBack = onBack)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterSheetContent(
    uiState: CharacterSheetUiState,
    onRetry: () -> Unit,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (uiState is CharacterSheetUiState.Content) Text(uiState.character.name)
                },
                navigationIcon = { BackButton(onBack) },
            )
        },
    ) { innerPadding ->
        val modifier = Modifier.padding(innerPadding)
        when (uiState) {
            CharacterSheetUiState.Loading -> LoadingContent(modifier)
            is CharacterSheetUiState.Error -> ErrorContent(uiState.error, onRetry, modifier)
            is CharacterSheetUiState.Content -> CharacterSheet(uiState.character, modifier)
        }
    }
}

@Composable
private fun CharacterSheet(character: Character, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
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
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(label, style = MaterialTheme.typography.labelMedium)
            Text(value, style = MaterialTheme.typography.headlineMedium)
        }
    }
}

@Composable
private fun AbilityCard(ability: Ability, score: AbilityScore?, modifier: Modifier = Modifier) {
    Card(modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(stringResource(ability.nameRes()), style = MaterialTheme.typography.labelLarge)
            if (score == null) {
                // The server didn't send this ability.
                Text(stringResource(R.string.missing_value), style = MaterialTheme.typography.headlineMedium)
            } else {
                Text(
                    text = stringResource(R.string.signed_value, score.modifier),
                    style = MaterialTheme.typography.headlineMedium,
                )
                Text(
                    text = stringResource(R.string.ability_score_value, score.score),
                    style = MaterialTheme.typography.bodyMedium,
                )
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
)

@Preview(showBackground = true)
@Composable
private fun CharacterSheetContentPreview() {
    DnDAppTheme {
        CharacterSheetContent(CharacterSheetUiState.Content(previewCharacter), {}, {})
    }
}

@Preview(showBackground = true)
@Composable
private fun CharacterSheetNotFoundPreview() {
    DnDAppTheme {
        CharacterSheetContent(CharacterSheetUiState.Error(RepositoryError.NotFound), {}, {})
    }
}
