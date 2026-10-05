package com.valsagnapps.dndapp.ui.createcharacter

import com.valsagnapps.dndapp.data.FakeCharacterRepository
import com.valsagnapps.dndapp.data.RepositoryError
import com.valsagnapps.dndapp.domain.Ability
import com.valsagnapps.dndapp.domain.NewCharacter
import com.valsagnapps.dndapp.ui.MainDispatcherRule
import kotlinx.coroutines.CompletableDeferred
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class CreateCharacterViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeCharacterRepository()
    private val viewModel = CreateCharacterViewModel(repository)

    private fun fillValidForm() {
        viewModel.onNameChange("  Tordek ")
        viewModel.onLevelChange("5")
        viewModel.onAbilityScoreChange(Ability.STRENGTH, "16")
    }

    @Test
    fun `does not show validation errors before trying to save`() {
        assertFalse(viewModel.uiState.value.showValidationErrors)
        assertFalse(viewModel.uiState.value.isNameValid)
    }

    @Test
    fun `shows validation errors and does not save when the form is invalid`() {
        viewModel.onLevelChange("21")

        viewModel.onSave()

        val state = viewModel.uiState.value
        assertTrue(state.showValidationErrors)
        assertFalse(state.isNameValid)
        assertFalse(state.isLevelValid)
        assertTrue(repository.created.isEmpty())
    }

    @Test
    fun `rejects ability scores outside 1 to 30`() {
        viewModel.onAbilityScoreChange(Ability.WISDOM, "0")
        viewModel.onAbilityScoreChange(Ability.CHARISMA, "31")

        val state = viewModel.uiState.value
        assertFalse(state.isAbilityScoreValid(Ability.WISDOM))
        assertFalse(state.isAbilityScoreValid(Ability.CHARISMA))
        assertTrue(state.isAbilityScoreValid(Ability.STRENGTH))
    }

    @Test
    fun `keeps only up to two digits in numeric fields`() {
        viewModel.onLevelChange("1a2b3")
        viewModel.onAbilityScoreChange(Ability.DEXTERITY, "-7")

        assertEquals("12", viewModel.uiState.value.level)
        assertEquals("7", viewModel.uiState.value.abilityScores[Ability.DEXTERITY])
    }

    @Test
    fun `creates the character with the trimmed name and the entered values`() {
        fillValidForm()

        viewModel.onSave()

        assertEquals(
            NewCharacter(
                name = "Tordek",
                level = 5,
                abilityScores = Ability.entries.associateWith { 10 } + (Ability.STRENGTH to 16),
            ),
            repository.created.single(),
        )
        assertEquals("created-1", viewModel.uiState.value.createdCharacterId)
    }

    @Test
    fun `is saving while waiting for the server and ignores repeated saves`() {
        fillValidForm()
        repository.gate = CompletableDeferred()

        viewModel.onSave()
        viewModel.onSave()
        assertTrue(viewModel.uiState.value.isSaving)

        repository.gate!!.complete(Unit)
        assertFalse(viewModel.uiState.value.isSaving)
        assertEquals(1, repository.created.size)
    }

    @Test
    fun `shows error when the server cannot create the character`() {
        fillValidForm()
        repository.failWith = RepositoryError.Server(400, "Invalid request content.")

        viewModel.onSave()

        val state = viewModel.uiState.value
        assertEquals(RepositoryError.Server(400, "Invalid request content."), state.saveError)
        assertFalse(state.isSaving)
        assertNull(state.createdCharacterId)
    }

    @Test
    fun `clears the previous error when saving again`() {
        fillValidForm()
        repository.failWith = RepositoryError.Network
        viewModel.onSave()

        repository.failWith = null
        viewModel.onSave()

        assertNull(viewModel.uiState.value.saveError)
        assertEquals("created-1", viewModel.uiState.value.createdCharacterId)
    }
}
