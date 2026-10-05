package com.valsagnapps.dndapp.ui.characterlist

import com.valsagnapps.dndapp.data.FakeCharacterRepository
import com.valsagnapps.dndapp.data.RepositoryError
import com.valsagnapps.dndapp.data.sampleCharacter
import com.valsagnapps.dndapp.ui.MainDispatcherRule
import kotlinx.coroutines.CompletableDeferred
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class CharacterListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val tordek = sampleCharacter(id = "1", name = "Tordek")
    private val repository = FakeCharacterRepository(listOf(tordek))
    private val viewModel = CharacterListViewModel(repository)

    @Test
    fun `shows loading until the characters arrive`() {
        repository.gate = CompletableDeferred()

        viewModel.refresh()
        assertEquals(CharacterListUiState.Loading, viewModel.uiState.value)

        repository.gate!!.complete(Unit)
        assertEquals(CharacterListUiState.Content(listOf(tordek)), viewModel.uiState.value)
    }

    @Test
    fun `shows error when the characters cannot be loaded`() {
        repository.failWith = RepositoryError.Network

        viewModel.refresh()

        assertEquals(CharacterListUiState.Error(RepositoryError.Network), viewModel.uiState.value)
    }

    @Test
    fun `keeps showing the loaded characters while refreshing`() {
        viewModel.refresh()
        repository.gate = CompletableDeferred()

        viewModel.refresh()

        assertEquals(CharacterListUiState.Content(listOf(tordek)), viewModel.uiState.value)
    }

    @Test
    fun `shows characters created since the last refresh`() {
        viewModel.refresh()
        val mialee = sampleCharacter(id = "2", name = "Mialee")
        repository.characters += mialee

        viewModel.refresh()

        assertEquals(CharacterListUiState.Content(listOf(tordek, mialee)), viewModel.uiState.value)
    }

    @Test
    fun `retrying after an error loads the characters`() {
        repository.failWith = RepositoryError.Network
        viewModel.refresh()

        repository.failWith = null
        viewModel.refresh()

        assertEquals(CharacterListUiState.Content(listOf(tordek)), viewModel.uiState.value)
    }
}
