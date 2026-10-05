package com.valsagnapps.dndapp.ui.charactersheet

import com.valsagnapps.dndapp.data.FakeCharacterRepository
import com.valsagnapps.dndapp.data.RepositoryError
import com.valsagnapps.dndapp.data.sampleCharacter
import com.valsagnapps.dndapp.ui.MainDispatcherRule
import kotlinx.coroutines.CompletableDeferred
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class CharacterSheetViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val tordek = sampleCharacter(id = "1")
    private val repository = FakeCharacterRepository(listOf(tordek))

    @Test
    fun `shows loading and then the character`() {
        repository.gate = CompletableDeferred()

        val viewModel = CharacterSheetViewModel("1", repository)
        assertEquals(CharacterSheetUiState.Loading, viewModel.uiState.value)

        repository.gate!!.complete(Unit)
        assertEquals(CharacterSheetUiState.Content(tordek), viewModel.uiState.value)
    }

    @Test
    fun `shows error when character is not found`() {
        val viewModel = CharacterSheetViewModel("missing", repository)

        assertEquals(CharacterSheetUiState.Error(RepositoryError.NotFound), viewModel.uiState.value)
    }

    @Test
    fun `retrying after a network error loads the character`() {
        repository.failWith = RepositoryError.Network
        val viewModel = CharacterSheetViewModel("1", repository)
        assertEquals(CharacterSheetUiState.Error(RepositoryError.Network), viewModel.uiState.value)

        repository.failWith = null
        viewModel.retry()

        assertEquals(CharacterSheetUiState.Content(tordek), viewModel.uiState.value)
    }
}
