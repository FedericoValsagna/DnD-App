package com.valsagnapps.dndapp.ui.charactersheet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.valsagnapps.dndapp.data.CharacterRepository
import com.valsagnapps.dndapp.data.RepositoryError
import com.valsagnapps.dndapp.data.RepositoryResult
import com.valsagnapps.dndapp.domain.Character
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface CharacterSheetUiState {
    data object Loading : CharacterSheetUiState
    data class Content(val character: Character) : CharacterSheetUiState
    data class Error(val error: RepositoryError) : CharacterSheetUiState
}

class CharacterSheetViewModel(
    private val characterId: String,
    private val repository: CharacterRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<CharacterSheetUiState>(CharacterSheetUiState.Loading)
    val uiState: StateFlow<CharacterSheetUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun retry() = load()

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
