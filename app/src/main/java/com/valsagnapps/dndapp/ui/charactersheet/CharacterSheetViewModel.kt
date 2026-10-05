package com.valsagnapps.dndapp.ui.charactersheet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.valsagnapps.dndapp.data.CharacterRepository
import com.valsagnapps.dndapp.data.RepositoryError
import com.valsagnapps.dndapp.data.RepositoryResult
import com.valsagnapps.dndapp.domain.Character
import com.valsagnapps.dndapp.domain.Proficiency
import com.valsagnapps.dndapp.domain.Skill
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface CharacterSheetUiState {
    data object Loading : CharacterSheetUiState

    data class Content(
        val character: Character,
        /** A skill change is being sent; the skills can't be edited until it finishes. */
        val isSavingSkills: Boolean = false,
        /** The last skill change failed; the character keeps the values it had. */
        val skillsError: RepositoryError? = null,
    ) : CharacterSheetUiState

    data class Error(val error: RepositoryError) : CharacterSheetUiState
}

class CharacterSheetViewModel(private val characterId: String, private val repository: CharacterRepository) :
    ViewModel() {

    private val _uiState = MutableStateFlow<CharacterSheetUiState>(CharacterSheetUiState.Loading)
    val uiState: StateFlow<CharacterSheetUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun retry() = load()

    /** Saves the change right away; the sheet shows what the server returns (with the new bonuses). */
    fun onSkillProficiencyChange(skill: Skill, proficiency: Proficiency) {
        val state = _uiState.value as? CharacterSheetUiState.Content ?: return
        if (state.isSavingSkills) return
        val skills = state.character.skillProficiencies + (skill to proficiency)
        _uiState.value = state.copy(isSavingSkills = true, skillsError = null)
        viewModelScope.launch {
            val result = repository.updateSkills(characterId, skills)
            _uiState.update { current ->
                when (result) {
                    is RepositoryResult.Success -> CharacterSheetUiState.Content(result.value)
                    is RepositoryResult.Failure ->
                        (current as? CharacterSheetUiState.Content)
                            ?.copy(isSavingSkills = false, skillsError = result.error)
                            ?: current
                }
            }
        }
    }

    private fun load() {
        _uiState.value = CharacterSheetUiState.Loading
        viewModelScope.launch {
            _uiState.value = when (val result = repository.get(characterId)) {
                is RepositoryResult.Success -> CharacterSheetUiState.Content(result.value)
                is RepositoryResult.Failure -> CharacterSheetUiState.Error(result.error)
            }
        }
    }
}
