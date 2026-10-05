package com.valsagnapps.dndapp.ui.charactersheet

import com.valsagnapps.dndapp.data.FakeCharacterRepository
import com.valsagnapps.dndapp.data.RepositoryError
import com.valsagnapps.dndapp.data.sampleCharacter
import com.valsagnapps.dndapp.domain.Proficiency
import com.valsagnapps.dndapp.domain.Skill
import com.valsagnapps.dndapp.ui.MainDispatcherRule
import kotlinx.coroutines.CompletableDeferred
import org.junit.Assert.assertEquals
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
        val viewModel = CharacterSheetViewModel("1", repository)

        viewModel.onSkillProficiencyChange(Skill.STEALTH, Proficiency.EXPERTISE)

        val (id, sent) = repository.skillUpdates.single()
        assertEquals("1", id)
        assertEquals(Proficiency.EXPERTISE, sent.getValue(Skill.STEALTH))
        // Keeps the proficiencies it already had.
        assertEquals(Proficiency.PROFICIENT, sent.getValue(Skill.PERCEPTION))
        assertEquals(CharacterSheetUiState.Content(repository.characters.single()), viewModel.uiState.value)
    }

    @Test
    fun `is saving while the skill change is sent and ignores other changes meanwhile`() {
        val viewModel = CharacterSheetViewModel("1", repository)
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
        val viewModel = CharacterSheetViewModel("1", repository)
        repository.failWith = RepositoryError.Network

        viewModel.onSkillProficiencyChange(Skill.STEALTH, Proficiency.PROFICIENT)

        assertEquals(
            CharacterSheetUiState.Content(tordek, isSavingSkills = false, skillsError = RepositoryError.Network),
            viewModel.uiState.value,
        )
    }

    @Test
    fun `clears the skill error after a successful change`() {
        val viewModel = CharacterSheetViewModel("1", repository)
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
}
