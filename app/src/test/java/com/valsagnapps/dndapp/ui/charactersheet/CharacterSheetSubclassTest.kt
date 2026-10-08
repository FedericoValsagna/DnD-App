package com.valsagnapps.dndapp.ui.charactersheet

import com.valsagnapps.dndapp.data.FakeCharacterRepository
import com.valsagnapps.dndapp.data.RepositoryError
import com.valsagnapps.dndapp.data.battleMaster
import com.valsagnapps.dndapp.data.champion
import com.valsagnapps.dndapp.data.sampleCatalog
import com.valsagnapps.dndapp.data.sampleCharacter
import com.valsagnapps.dndapp.domain.CharacterClass
import com.valsagnapps.dndapp.domain.ClassLevel
import com.valsagnapps.dndapp.ui.MainDispatcherRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** Subclass in the class dialog of [CharacterSheetViewModel]. */
class CharacterSheetSubclassTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val championTordek = sampleCharacter(id = "1").let {
        it.copy(classes = listOf(ClassLevel(CharacterClass.FIGHTER, 5, champion)))
    }
    private val repository = FakeCharacterRepository(listOf(championTordek)).apply { classCatalog = sampleCatalog() }

    @Test
    fun `loads the class catalog with the character`() {
        val viewModel = CharacterSheetViewModel("1", repository)

        assertEquals(sampleCatalog(), viewModel.content.classCatalog)
    }

    @Test
    fun `editing the class starts from the current subclass with the catalog`() {
        val viewModel = editingViewModel()

        viewModel.onEditClass()

        assertEquals(
            SheetEdit.Class(CharacterClass.FIGHTER, "5", champion, sampleCatalog()),
            viewModel.content.edit,
        )
    }

    @Test
    fun `changing only the subclass sends it after the classes`() {
        val viewModel = editingViewModel()
        viewModel.onEditClass()

        viewModel.onEditInput(SheetEditInput.SubclassChange(battleMaster))
        viewModel.onConfirmEdit()

        assertEquals(listOf("1" to listOf(ClassLevel(CharacterClass.FIGHTER, 5))), repository.classUpdates)
        assertEquals(listOf(Triple("1", CharacterClass.FIGHTER, "BATTLE_MASTER")), repository.subclassUpdates)
        assertEquals(battleMaster, viewModel.content.character.classes.single().subclass)
        assertNull(viewModel.content.edit)
    }

    @Test
    fun `does not send the subclass when the server already kept it`() {
        val viewModel = editingViewModel()
        viewModel.onEditClass()

        viewModel.onEditInput(SheetEditInput.LevelChange("6"))
        viewModel.onConfirmEdit()

        assertTrue(repository.subclassUpdates.isEmpty())
        assertEquals(ClassLevel(CharacterClass.FIGHTER, 6, champion), viewModel.content.character.classes.single())
    }

    @Test
    fun `removing the subclass sends null`() {
        val viewModel = editingViewModel()
        viewModel.onEditClass()

        viewModel.onEditInput(SheetEditInput.SubclassChange(null))
        viewModel.onConfirmEdit()

        assertEquals(listOf(Triple("1", CharacterClass.FIGHTER, null)), repository.subclassUpdates)
        assertNull(viewModel.content.character.classes.single().subclass)
    }

    @Test
    fun `lowering the level below the subclass level leaves the subclass to the server`() {
        val viewModel = editingViewModel()
        viewModel.onEditClass()

        viewModel.onEditInput(SheetEditInput.LevelChange("2"))
        viewModel.onConfirmEdit()

        assertTrue(repository.subclassUpdates.isEmpty())
        assertEquals(ClassLevel(CharacterClass.FIGHTER, 2), viewModel.content.character.classes.single())
    }

    @Test
    fun `without the catalog the subclass is not touched`() {
        repository.classCatalog = emptyList()
        val viewModel = editingViewModel()
        viewModel.onEditClass()

        viewModel.onEditInput(SheetEditInput.LevelChange("6"))
        viewModel.onConfirmEdit()

        assertTrue(repository.subclassUpdates.isEmpty())
    }

    @Test
    fun `retries loading the catalog when opening the class dialog`() {
        repository.classCatalog = emptyList()
        val viewModel = editingViewModel()
        assertEquals(emptyList<Any>(), viewModel.content.classCatalog)
        repository.classCatalog = sampleCatalog()

        viewModel.onEditClass()

        assertEquals(sampleCatalog(), viewModel.content.classCatalog)
        assertEquals(sampleCatalog(), (viewModel.content.edit as SheetEdit.Class).catalog)
    }

    @Test
    fun `keeps the dialog open with the error when the subclass is rejected`() {
        val viewModel = editingViewModel()
        viewModel.onEditClass()
        viewModel.onEditInput(SheetEditInput.SubclassChange(battleMaster))
        repository.failWith = RepositoryError.Server(400, "rejected")

        viewModel.onConfirmEdit()

        assertEquals(RepositoryError.Server(400, "rejected"), viewModel.content.editError)
        assertEquals(battleMaster, (viewModel.content.edit as SheetEdit.Class).subclass)
    }

    private val CharacterSheetViewModel.content get() = uiState.value as CharacterSheetUiState.Content

    private fun editingViewModel() = CharacterSheetViewModel("1", repository).apply { onToggleEditing() }
}
