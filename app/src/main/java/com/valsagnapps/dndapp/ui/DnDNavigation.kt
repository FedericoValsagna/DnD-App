package com.valsagnapps.dndapp.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.valsagnapps.dndapp.data.CharacterRepository
import com.valsagnapps.dndapp.ui.characterlist.CharacterListScreen
import com.valsagnapps.dndapp.ui.characterlist.CharacterListViewModel
import com.valsagnapps.dndapp.ui.charactersheet.CharacterSheetScreen
import com.valsagnapps.dndapp.ui.charactersheet.CharacterSheetViewModel
import com.valsagnapps.dndapp.ui.createcharacter.CreateCharacterScreen
import com.valsagnapps.dndapp.ui.createcharacter.CreateCharacterViewModel
import kotlinx.serialization.Serializable

@Serializable
data object CharacterListRoute : NavKey

@Serializable
data object CreateCharacterRoute : NavKey

@Serializable
data class CharacterSheetRoute(val characterId: String) : NavKey

/**
 * The back stack is a list of routes: the last one is the screen being shown.
 * Navigating forward adds a route; going back removes the last one.
 */
@Composable
fun DnDNavigation(repository: CharacterRepository) {
    val backStack = rememberNavBackStack(CharacterListRoute)
    val goBack: () -> Unit = { backStack.removeLastOrNull() }

    NavDisplay(
        backStack = backStack,
        onBack = goBack,
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            // Each screen in the back stack gets its own ViewModels.
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = entryProvider {
            entry<CharacterListRoute> {
                CharacterListScreen(
                    viewModel = viewModel { CharacterListViewModel(repository) },
                    onCharacterClick = { id -> backStack.add(CharacterSheetRoute(id)) },
                    onCreateClick = { backStack.add(CreateCharacterRoute) },
                )
            }
            entry<CreateCharacterRoute> {
                CreateCharacterScreen(
                    viewModel = viewModel { CreateCharacterViewModel(repository) },
                    onNavigateToCharacter = { id ->
                        // Replace the form with the new sheet, so going back returns to the list.
                        backStack.removeLastOrNull()
                        backStack.add(CharacterSheetRoute(id))
                    },
                    onBack = goBack,
                )
            }
            entry<CharacterSheetRoute> { route ->
                CharacterSheetScreen(
                    viewModel = viewModel { CharacterSheetViewModel(route.characterId, repository) },
                    onBack = goBack,
                )
            }
        },
    )
}
