package com.valsagnapps.dndapp.ui.characterlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.valsagnapps.dndapp.data.CharacterRepository
import com.valsagnapps.dndapp.data.RepositoryError
import com.valsagnapps.dndapp.data.RepositoryResult
import com.valsagnapps.dndapp.domain.Character
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface CharacterListUiState {
    data object Loading : CharacterListUiState
    data class Content(val characters: List<Character>) : CharacterListUiState
    data class Error(val error: RepositoryError) : CharacterListUiState
}

class CharacterListViewModel(private val repository: CharacterRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<CharacterListUiState>(CharacterListUiState.Loading)
    val uiState: StateFlow<CharacterListUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    /**
     * Reloads the list. The screen calls it every time it is shown, so characters created
     * meanwhile show up. Already loaded characters stay visible while reloading.
     */
    fun refresh() {
        if (_uiState.value !is CharacterListUiState.Content) {
            _uiState.value = CharacterListUiState.Loading
        }
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.value = when (val result = repository.list()) {
                is RepositoryResult.Success -> CharacterListUiState.Content(result.value)
                is RepositoryResult.Failure -> CharacterListUiState.Error(result.error)
            }
        }
    }
}
