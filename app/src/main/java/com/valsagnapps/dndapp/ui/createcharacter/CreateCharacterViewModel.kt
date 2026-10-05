package com.valsagnapps.dndapp.ui.createcharacter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.valsagnapps.dndapp.data.CharacterRepository
import com.valsagnapps.dndapp.data.RepositoryError
import com.valsagnapps.dndapp.data.RepositoryResult
import com.valsagnapps.dndapp.domain.Ability
import com.valsagnapps.dndapp.domain.CharacterRules
import com.valsagnapps.dndapp.domain.NewCharacter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Numeric fields are kept as text so the user can clear them while typing. */
data class CreateCharacterUiState(
    val name: String = "",
    val level: String = "1",
    val abilityScores: Map<Ability, String> = Ability.entries.associateWith { "10" },
    /** Validation errors are shown only after the first attempt to save. */
    val showValidationErrors: Boolean = false,
    val isSaving: Boolean = false,
    val saveError: RepositoryError? = null,
    /** Set when the server created the character; the screen then navigates to it. */
    val createdCharacterId: String? = null,
) {
    val isNameValid: Boolean get() = CharacterRules.isValidName(name.trim())

    val isLevelValid: Boolean get() = level.toIntOrNull()?.let(CharacterRules::isValidLevel) == true

    fun isAbilityScoreValid(ability: Ability): Boolean =
        abilityScores[ability]?.toIntOrNull()?.let(CharacterRules::isValidAbilityScore) == true

    val isValid: Boolean get() = isNameValid && isLevelValid && Ability.entries.all(::isAbilityScoreValid)
}

class CreateCharacterViewModel(private val repository: CharacterRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateCharacterUiState())
    val uiState: StateFlow<CreateCharacterUiState> = _uiState.asStateFlow()

    fun onNameChange(name: String) = _uiState.update { it.copy(name = name) }

    fun onLevelChange(level: String) = _uiState.update { it.copy(level = level.toNumericInput()) }

    fun onAbilityScoreChange(ability: Ability, score: String) = _uiState.update {
        it.copy(abilityScores = it.abilityScores + (ability to score.toNumericInput()))
    }

    fun onSave() {
        val state = _uiState.value
        if (state.isSaving) return
        if (!state.isValid) {
            _uiState.update { it.copy(showValidationErrors = true) }
            return
        }
        _uiState.update { it.copy(isSaving = true, saveError = null) }
        viewModelScope.launch {
            val newCharacter = NewCharacter(
                name = state.name.trim(),
                level = state.level.toInt(),
                abilityScores = state.abilityScores.mapValues { (_, score) -> score.toInt() },
            )
            _uiState.update {
                when (val result = repository.create(newCharacter)) {
                    is RepositoryResult.Success ->
                        it.copy(isSaving = false, createdCharacterId = result.value.id)
                    is RepositoryResult.Failure ->
                        it.copy(isSaving = false, saveError = result.error)
                }
            }
        }
    }

    /** Keeps only digits, at most two (every valid value fits). */
    private fun String.toNumericInput(): String = filter(Char::isDigit).take(2)
}
