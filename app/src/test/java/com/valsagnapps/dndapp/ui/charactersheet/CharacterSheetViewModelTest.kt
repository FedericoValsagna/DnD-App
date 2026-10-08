package com.valsagnapps.dndapp.ui.charactersheet

import com.valsagnapps.dndapp.data.FakeCharacterRepository
import com.valsagnapps.dndapp.data.RepositoryError
import com.valsagnapps.dndapp.data.sampleCharacter
import com.valsagnapps.dndapp.domain.CharacterClass
import com.valsagnapps.dndapp.domain.ClassLevel
import com.valsagnapps.dndapp.domain.Proficiency
import com.valsagnapps.dndapp.domain.Skill
import com.valsagnapps.dndapp.ui.MainDispatcherRule
import kotlinx.coroutines.CompletableDeferred
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
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

    @Test
    fun `changing a skill sends all the proficiencies and shows the updated character`() {
        val viewModel = editingViewModel()

        viewModel.onSkillProficiencyChange(Skill.STEALTH, Proficiency.EXPERTISE)

        val (id, sent) = repository.skillUpdates.single()
        assertEquals("1", id)
        assertEquals(Proficiency.EXPERTISE, sent.getValue(Skill.STEALTH))
        // Keeps the proficiencies it already had.
        assertEquals(Proficiency.PROFICIENT, sent.getValue(Skill.PERCEPTION))
        assertEquals(
            CharacterSheetUiState.Content(repository.characters.single(), isEditing = true),
            viewModel.uiState.value,
        )
    }

    @Test
    fun `is saving while the skill change is sent and ignores other changes meanwhile`() {
        val viewModel = editingViewModel()
        repository.gate = CompletableDeferred()

        viewModel.onSkillProficiencyChange(Skill.STEALTH, Proficiency.PROFICIENT)
        viewModel.onSkillProficiencyChange(Skill.ARCANA, Proficiency.PROFICIENT)
        assertTrue((viewModel.uiState.value as CharacterSheetUiState.Content).isSavingSkills)

        repository.gate!!.complete(Unit)
        assertEquals(1, repository.skillUpdates.size)
        assertEquals(false, (viewModel.uiState.value as CharacterSheetUiState.Content).isSavingSkills)
    }

    @Test
    fun `keeps the character and shows the error when the skill change fails`() {
        val viewModel = editingViewModel()
        repository.failWith = RepositoryError.Network

        viewModel.onSkillProficiencyChange(Skill.STEALTH, Proficiency.PROFICIENT)

        assertEquals(
            CharacterSheetUiState.Content(tordek, isEditing = true, skillsError = RepositoryError.Network),
            viewModel.uiState.value,
        )
    }

    @Test
    fun `clears the skill error after a successful change`() {
        val viewModel = editingViewModel()
        repository.failWith = RepositoryError.Network
        viewModel.onSkillProficiencyChange(Skill.STEALTH, Proficiency.PROFICIENT)

        repository.failWith = null
        viewModel.onSkillProficiencyChange(Skill.STEALTH, Proficiency.PROFICIENT)

        assertEquals(null, (viewModel.uiState.value as CharacterSheetUiState.Content).skillsError)
    }

    @Test
    fun `ignores skill changes until the character is loaded`() {
        repository.failWith = RepositoryError.Network
        val viewModel = CharacterSheetViewModel("1", repository)

        viewModel.onSkillProficiencyChange(Skill.STEALTH, Proficiency.PROFICIENT)

        assertTrue(repository.skillUpdates.isEmpty())
        assertEquals(CharacterSheetUiState.Error(RepositoryError.Network), viewModel.uiState.value)
    }

    private val CharacterSheetViewModel.content get() = uiState.value as CharacterSheetUiState.Content

    @Test
    fun `editing the class starts from the current class and level`() {
        val viewModel = editingViewModel()

        viewModel.onEditClass()

        assertEquals(SheetEdit.Class(CharacterClass.FIGHTER, "5"), viewModel.content.edit)
    }

    @Test
    fun `changing the class sends it with the level and closes the dialog`() {
        val viewModel = editingViewModel()
        viewModel.onEditClass()

        viewModel.onEditInput(SheetEditInput.ClassChange(CharacterClass.CLERIC))
        viewModel.onEditInput(SheetEditInput.LevelChange("6"))
        viewModel.onConfirmEdit()

        assertEquals(listOf("1" to listOf(ClassLevel(CharacterClass.CLERIC, 6))), repository.classUpdates)
        assertNull(viewModel.content.edit)
        assertEquals(listOf(ClassLevel(CharacterClass.CLERIC, 6)), viewModel.content.character.classes)
        assertEquals(6, viewModel.content.character.level)
    }

    @Test
    fun `a character without class can pick one`() {
        val repository = FakeCharacterRepository(listOf(tordek.copy(classes = emptyList())))
        val viewModel = editingViewModel(repository)
        viewModel.onEditClass()
        assertEquals(SheetEdit.Class(null, "5"), viewModel.content.edit)
        assertFalse(viewModel.content.edit!!.isValid)

        viewModel.onEditInput(SheetEditInput.ClassChange(CharacterClass.RANGER))
        viewModel.onConfirmEdit()

        assertEquals(listOf("1" to listOf(ClassLevel(CharacterClass.RANGER, 5))), repository.classUpdates)
    }

    @Test
    fun `does not send an invalid edit`() {
        val viewModel = editingViewModel()
        viewModel.onEditClass()

        viewModel.onEditInput(SheetEditInput.LevelChange("21"))
        viewModel.onConfirmEdit()

        assertTrue(repository.classUpdates.isEmpty())
        assertEquals(SheetEdit.Class(CharacterClass.FIGHTER, "21"), viewModel.content.edit)
    }

    @Test
    fun `the class of a multiclass character cannot be edited yet`() {
        val multiclass = tordek.copy(
            classes = listOf(ClassLevel(CharacterClass.FIGHTER, 3), ClassLevel(CharacterClass.ROGUE, 2)),
        )
        val viewModel = editingViewModel(FakeCharacterRepository(listOf(multiclass)))

        viewModel.onEditClass()

        assertFalse(viewModel.content.canEditClass)
        assertNull(viewModel.content.edit)
    }

    @Test
    fun `changing the max hit points sends them and closes the dialog`() {
        val viewModel = editingViewModel()
        viewModel.onEditMaxHitPoints()
        assertEquals(SheetEdit.MaxHitPoints("44"), viewModel.content.edit)

        viewModel.onEditInput(SheetEditInput.MaxHitPointsChange("4a72"))
        assertEquals(SheetEdit.MaxHitPoints("472"), viewModel.content.edit)
        viewModel.onEditInput(SheetEditInput.MaxHitPointsChange("47"))
        viewModel.onConfirmEdit()

        assertEquals(listOf("1" to 47), repository.hitPointUpdates)
        assertNull(viewModel.content.edit)
        assertEquals(47, viewModel.content.character.maxHitPoints)
    }

    @Test
    fun `keeps the dialog open with the error when saving the edit fails`() {
        val viewModel = editingViewModel()
        viewModel.onEditMaxHitPoints()
        viewModel.onEditInput(SheetEditInput.MaxHitPointsChange("50"))
        repository.failWith = RepositoryError.Network

        viewModel.onConfirmEdit()

        val state = viewModel.content
        assertEquals(SheetEdit.MaxHitPoints("50"), state.edit)
        assertEquals(RepositoryError.Network, state.editError)
        assertFalse(state.isSavingEdit)
        assertEquals(tordek, state.character)
    }

    @Test
    fun `cannot change or dismiss the edit while saving`() {
        val viewModel = editingViewModel()
        viewModel.onEditMaxHitPoints()
        repository.gate = CompletableDeferred()

        viewModel.onConfirmEdit()
        viewModel.onEditInput(SheetEditInput.MaxHitPointsChange("12"))
        viewModel.onDismissEdit()
        viewModel.onConfirmEdit()
        assertTrue(viewModel.content.isSavingEdit)
        assertEquals(SheetEdit.MaxHitPoints("44"), viewModel.content.edit)

        repository.gate!!.complete(Unit)
        assertEquals(1, repository.hitPointUpdates.size)
        assertNull(viewModel.content.edit)
    }

    @Test
    fun `dismissing the dialog discards the edit and its error`() {
        val viewModel = editingViewModel()
        viewModel.onEditMaxHitPoints()
        repository.failWith = RepositoryError.Network
        viewModel.onConfirmEdit()

        viewModel.onDismissEdit()

        assertNull(viewModel.content.edit)
        assertNull(viewModel.content.editError)
    }

    @Test
    fun `starts in read mode and ignores edits until entering edit mode`() {
        val viewModel = CharacterSheetViewModel("1", repository)

        viewModel.onSkillProficiencyChange(Skill.STEALTH, Proficiency.EXPERTISE)
        viewModel.onEditClass()
        viewModel.onEditMaxHitPoints()

        assertFalse(viewModel.content.isEditing)
        assertTrue(repository.skillUpdates.isEmpty())
        assertNull(viewModel.content.edit)
    }

    @Test
    fun `stays in edit mode after saving`() {
        val viewModel = editingViewModel()

        viewModel.onSkillProficiencyChange(Skill.STEALTH, Proficiency.EXPERTISE)
        viewModel.onEditMaxHitPoints()
        viewModel.onConfirmEdit()

        assertTrue(viewModel.content.isEditing)
    }

    @Test
    fun `leaving edit mode closes the dialog and clears the errors`() {
        val viewModel = editingViewModel()
        repository.failWith = RepositoryError.Network
        viewModel.onSkillProficiencyChange(Skill.STEALTH, Proficiency.EXPERTISE)
        viewModel.onEditMaxHitPoints()

        viewModel.onToggleEditing()

        assertEquals(CharacterSheetUiState.Content(tordek), viewModel.uiState.value)
    }

    @Test
    fun `cannot leave edit mode while saving`() {
        val viewModel = editingViewModel()
        repository.gate = CompletableDeferred()
        viewModel.onSkillProficiencyChange(Skill.STEALTH, Proficiency.EXPERTISE)

        viewModel.onToggleEditing()
        assertTrue(viewModel.content.isEditing)

        repository.gate!!.complete(Unit)
    }

    private fun editingViewModel(repository: FakeCharacterRepository = this.repository) =
        CharacterSheetViewModel("1", repository).apply { onToggleEditing() }
}
