package com.valsagnapps.dndapp.ui.characterlist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.valsagnapps.dndapp.R
import com.valsagnapps.dndapp.data.RepositoryError
import com.valsagnapps.dndapp.domain.Character
import com.valsagnapps.dndapp.ui.common.ErrorContent
import com.valsagnapps.dndapp.ui.common.LoadingContent
import com.valsagnapps.dndapp.ui.theme.DnDAppTheme

@Composable
fun CharacterListScreen(
    viewModel: CharacterListViewModel,
    onCharacterClick: (id: String) -> Unit,
    onCreateClick: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.refresh() }
    CharacterListContent(
        uiState = uiState,
        onRetry = viewModel::refresh,
        onCharacterClick = onCharacterClick,
        onCreateClick = onCreateClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterListContent(
    uiState: CharacterListUiState,
    onRetry: () -> Unit,
    onCharacterClick: (id: String) -> Unit,
    onCreateClick: () -> Unit,
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.character_list_title)) }) },
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = onCreateClick) {
                Text(stringResource(R.string.create_character))
            }
        },
    ) { innerPadding ->
        val modifier = Modifier.padding(innerPadding)
        when (uiState) {
            CharacterListUiState.Loading -> LoadingContent(modifier)
            is CharacterListUiState.Error -> ErrorContent(uiState.error, onRetry, modifier)
            is CharacterListUiState.Content ->
                if (uiState.characters.isEmpty()) {
                    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = stringResource(R.string.character_list_empty),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(24.dp),
                        )
                    }
                } else {
                    LazyColumn(modifier.fillMaxSize()) {
                        items(uiState.characters, key = { it.id }) { character ->
                            ListItem(
                                headlineContent = { Text(character.name) },
                                supportingContent = {
                                    Text(stringResource(R.string.level_value, character.level))
                                },
                                modifier = Modifier.clickable { onCharacterClick(character.id) },
                            )
                            HorizontalDivider()
                        }
                    }
                }
        }
    }
}

private val previewCharacters = listOf(
    Character(id = "1", name = "Tordek", level = 5, proficiencyBonus = 3, abilities = emptyMap()),
    Character(id = "2", name = "Mialee", level = 3, proficiencyBonus = 2, abilities = emptyMap()),
)

@Preview(showBackground = true)
@Composable
private fun CharacterListContentPreview() {
    DnDAppTheme {
        CharacterListContent(CharacterListUiState.Content(previewCharacters), {}, {}, {})
    }
}

@Preview(showBackground = true)
@Composable
private fun CharacterListEmptyPreview() {
    DnDAppTheme {
        CharacterListContent(CharacterListUiState.Content(emptyList()), {}, {}, {})
    }
}

@Preview(showBackground = true)
@Composable
private fun CharacterListErrorPreview() {
    DnDAppTheme {
        CharacterListContent(CharacterListUiState.Error(RepositoryError.Network), {}, {}, {})
    }
}
