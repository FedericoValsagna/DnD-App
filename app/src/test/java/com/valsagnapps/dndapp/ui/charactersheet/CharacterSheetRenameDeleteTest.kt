package com.valsagnapps.dndapp.ui.charactersheet

import com.valsagnapps.dndapp.data.FakeCharacterRepository
import com.valsagnapps.dndapp.data.RepositoryError
import com.valsagnapps.dndapp.data.sampleCharacter
import com.valsagnapps.dndapp.domain.Ability
import com.valsagnapps.dndapp.domain.AbilityScore
import com.valsagnapps.dndapp.ui.MainDispatcherRule
import kotlinx.coroutines.CompletableDeferred
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** Editing the name and the ability scores, and deleting the character, from the sheet's edit mode. */
class CharacterSheetRenameDeleteTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val tordek = sampleCharacter(id = "1", name = "Tordek")
    private val repository = FakeCharacterRepository(listOf(tordek))

    @Test
    fun `renaming sends the trimmed name and shows the updated character`() {
        val viewModel = editingViewModel()
        viewModel.onStartEdit(SheetEditTarget.NAME)
        assertEquals(SheetEdit.Name("Tordek"), viewModel.content.edit)

        viewModel.onEditInput(SheetEditInput.NameChange("  Tordek the Bold "))
        viewModel.onConfirmEdit()

        assertEquals(listOf("1" to "Tordek the Bold"), repository.nameUpdates)
        assertNull(viewModel.content.edit)
        assertEquals("Tordek the Bold", viewModel.content.character.name)
        assertTrue(viewModel.content.isEditing)
    }

    @Test
    fun `does not send a blank name`() {
        val viewModel = editingViewModel()
        viewModel.onStartEdit(SheetEditTarget.NAME)
        viewModel.onEditInput(SheetEditInput.NameChange("   "))

        viewModel.onConfirmEdit()

        assertEquals(emptyList<Pair<String, String>>(), repository.nameUpdates)
        assertEquals(SheetEdit.Name("   "), viewModel.content.edit)
    }

    @Test
    fun `editing the abilities starts from the current scores and sends all six`() {
        val viewModel = editingViewModel()
        viewModel.onStartEdit(SheetEditTarget.ABILITY_SCORES)
        val edit = viewModel.content.edit as SheetEdit.AbilityScores
        assertEquals("16", edit.values[Ability.STRENGTH])
        assertEquals("8", edit.values[Ability.CHARISMA])

        viewModel.onEditInput(SheetEditInput.AbilityScoreChange(Ability.STRENGTH, "18"))
        viewModel.onConfirmEdit()

        val sent = mapOf(
            Ability.STRENGTH to 18,
            Ability.DEXTERITY to 12,
            Ability.CONSTITUTION to 15,
            Ability.INTELLIGENCE to 10,
            Ability.WISDOM to 13,
            Ability.CHARISMA to 8,
        )
        assertEquals(listOf("1" to sent), repository.abilityScoreUpdates)
        assertNull(viewModel.content.edit)
        assertEquals(AbilityScore(18, 4), viewModel.content.character.abilities[Ability.STRENGTH])
    }

    @Test
    fun `does not send ability scores out of range or missing`() {
        val viewModel = editingViewModel()
        viewModel.onStartEdit(SheetEditTarget.ABILITY_SCORES)
        viewModel.onEditInput(SheetEditInput.AbilityScoreChange(Ability.WISDOM, ""))

        viewModel.onConfirmEdit()
        viewModel.onEditInput(SheetEditInput.AbilityScoreChange(Ability.WISDOM, "31"))
        viewModel.onConfirmEdit()

        assertEquals(emptyList<Any>(), repository.abilityScoreUpdates)
    }

    @Test
    fun `an older server without abilities starts the dialog empty`() {
        val viewModel = editingViewModel(FakeCharacterRepository(listOf(tordek.copy(abilities = emptyMap()))))

        viewModel.onStartEdit(SheetEditTarget.ABILITY_SCORES)

        assertEquals(SheetEdit.AbilityScores(Ability.entries.associateWith { "" }), viewModel.content.edit)
    }

    @Test
    fun `confirming the delete deletes the character and leaves the sheet`() {
        val viewModel = editingViewModel()
        viewModel.onStartEdit(SheetEditTarget.DELETE_CHARACTER)
        assertEquals(SheetEdit.DeleteCharacter("Tordek"), viewModel.content.edit)

        viewModel.onConfirmEdit()

        assertEquals(listOf("1"), repository.deleted)
        assertEquals(CharacterSheetUiState.Deleted, viewModel.uiState.value)
    }

    @Test
    fun `is saving while deleting`() {
        val viewModel = editingViewModel()
        viewModel.onStartEdit(SheetEditTarget.DELETE_CHARACTER)
        repository.gate = CompletableDeferred()

        viewModel.onConfirmEdit()
        assertTrue(viewModel.content.isSavingEdit)

        repository.gate!!.complete(Unit)
        assertEquals(CharacterSheetUiState.Deleted, viewModel.uiState.value)
    }

    @Test
    fun `keeps the sheet and the confirmation with the error when the delete fails`() {
        val viewModel = editingViewModel()
        viewModel.onStartEdit(SheetEditTarget.DELETE_CHARACTER)
        repository.failWith = RepositoryError.Network

        viewModel.onConfirmEdit()

        assertEquals(SheetEdit.DeleteCharacter("Tordek"), viewModel.content.edit)
        assertEquals(RepositoryError.Network, viewModel.content.editError)
        assertEquals(tordek, viewModel.content.character)
    }

    @Test
    fun `dismissing the confirmation does not delete`() {
        val viewModel = editingViewModel()
        viewModel.onStartEdit(SheetEditTarget.DELETE_CHARACTER)

        viewModel.onDismissEdit()

        assertNull(viewModel.content.edit)
        assertEquals(emptyList<String>(), repository.deleted)
    }

    @Test
    fun `nothing can be renamed, edited or deleted outside edit mode`() {
        val viewModel = CharacterSheetViewModel("1", repository)

        SheetEditTarget.entries.forEach { viewModel.onStartEdit(it) }

        assertNull(viewModel.content.edit)
    }

    private val CharacterSheetViewModel.content get() = uiState.value as CharacterSheetUiState.Content

    private fun editingViewModel(repository: FakeCharacterRepository = this.repository) =
        CharacterSheetViewModel("1", repository).apply { onToggleEditing() }
}
