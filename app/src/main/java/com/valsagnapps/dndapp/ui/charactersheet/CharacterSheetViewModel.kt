package com.valsagnapps.dndapp.ui.charactersheet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.valsagnapps.dndapp.data.CharacterRepository
import com.valsagnapps.dndapp.data.RepositoryError
import com.valsagnapps.dndapp.data.RepositoryResult
import com.valsagnapps.dndapp.domain.Character
import com.valsagnapps.dndapp.domain.ClassInfo
import com.valsagnapps.dndapp.domain.ClassLevel
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
        /** Edit mode: only then can the class, the max HP and the skills be changed. */
        val isEditing: Boolean = false,
        /** A skill change is being sent; the skills can't be edited until it finishes. */
        val isSavingSkills: Boolean = false,
        /** The last skill change failed; the character keeps the values it had. */
        val skillsError: RepositoryError? = null,
        /** Value being edited in a dialog, or null if no dialog is open. */
        val edit: SheetEdit? = null,
        val isSavingEdit: Boolean = false,
        /** Saving the edit failed; the dialog stays open to retry. */
        val editError: RepositoryError? = null,
        /** Classes with their subclasses, for the class dialog. Empty until loaded (or if it failed). */
        val classCatalog: List<ClassInfo> = emptyList(),
    ) : CharacterSheetUiState {
        /** The class can be edited only for single-class characters (multiclass comes later). */
        val canEditClass: Boolean get() = character.classes.size <= 1
    }

    data class Error(val error: RepositoryError) : CharacterSheetUiState
}

class CharacterSheetViewModel(private val characterId: String, private val repository: CharacterRepository) :
    ViewModel() {

    private val _uiState = MutableStateFlow<CharacterSheetUiState>(CharacterSheetUiState.Loading)
    val uiState: StateFlow<CharacterSheetUiState> = _uiState.asStateFlow()

    init {
        retry()
    }

    /** Enters or leaves edit mode. Leaving it closes the open dialog and clears the errors. */
    fun onToggleEditing() {
        val state = _uiState.value as? CharacterSheetUiState.Content ?: return
        if (state.isSavingEdit || state.isSavingSkills) return
        _uiState.value = state.copy(isEditing = !state.isEditing, edit = null, editError = null, skillsError = null)
    }

    /** Saves the change right away; the sheet shows what the server returns (with the new bonuses). */
    fun onSkillProficiencyChange(skill: Skill, proficiency: Proficiency) {
        val state = _uiState.value.editing() ?: return
        if (state.isSavingSkills) return
        val skills = state.character.skillProficiencies + (skill to proficiency)
        _uiState.value = state.copy(isSavingSkills = true, skillsError = null)
        viewModelScope.launch {
            val result = repository.updateSkills(characterId, skills)
            _uiState.update { current ->
                when (result) {
                    is RepositoryResult.Success -> current.withCharacter(result.value)
                    is RepositoryResult.Failure ->
                        (current as? CharacterSheetUiState.Content)
                            ?.copy(isSavingSkills = false, skillsError = result.error)
                            ?: current
                }
            }
        }
    }

    fun onEditClass() {
        val state = _uiState.value.editing() ?: return
        if (!state.canEditClass) return
        val current = state.character.classes.firstOrNull()
        val edit = SheetEdit.Class(
            characterClass = current?.characterClass,
            level = state.character.level.toString(),
            subclass = current?.subclass,
            catalog = state.classCatalog,
        )
        _uiState.value = state.copy(edit = edit, editError = null)
        if (state.classCatalog.isEmpty()) loadClassCatalog()
    }

    fun onEditMaxHitPoints() {
        val state = _uiState.value.editing() ?: return
        val edit = SheetEdit.MaxHitPoints(state.character.maxHitPoints?.toString().orEmpty())
        _uiState.value = state.copy(edit = edit, editError = null)
    }

    /** Applies a change in a field of the open dialog. */
    fun onEditInput(input: SheetEditInput) {
        val state = _uiState.value as? CharacterSheetUiState.Content
        val edit = state?.edit
        if (edit == null || state.isSavingEdit) return
        _uiState.value = state.copy(edit = edit.changedBy(input))
    }

    fun onDismissEdit() {
        val state = _uiState.value as? CharacterSheetUiState.Content ?: return
        if (state.isSavingEdit) return
        _uiState.value = state.copy(edit = null, editError = null)
    }

    /** Sends the edit; on success the dialog closes and the sheet shows what the server returns. */
    fun onConfirmEdit() {
        val state = _uiState.value as? CharacterSheetUiState.Content ?: return
        val edit = state.edit?.takeIf { it.isValid && !state.isSavingEdit } ?: return
        _uiState.value = state.copy(isSavingEdit = true, editError = null)
        viewModelScope.launch {
            val result = when (edit) {
                is SheetEdit.Class -> saveClass(edit)
                is SheetEdit.MaxHitPoints -> repository.updateMaxHitPoints(characterId, edit.value.toInt())
            }
            _uiState.update { current ->
                when (result) {
                    is RepositoryResult.Success -> current.withCharacter(result.value)
                    is RepositoryResult.Failure ->
                        (current as? CharacterSheetUiState.Content)
                            ?.copy(isSavingEdit = false, editError = result.error)
                            ?: current
                }
            }
        }
    }

    /**
     * The server keeps or removes the subclass when the classes change, so the subclass is sent afterwards,
     * only if what it kept isn't what the user chose (and the catalog was there to choose it).
     */
    private suspend fun saveClass(edit: SheetEdit.Class): RepositoryResult<Character> {
        val characterClass = checkNotNull(edit.characterClass)
        val result = repository.updateClasses(characterId, listOf(ClassLevel(characterClass, edit.level.toInt())))
        val kept = (result as? RepositoryResult.Success)?.value?.classes?.firstOrNull()?.subclass
        if (result is RepositoryResult.Failure || edit.catalog.isEmpty() || kept?.id == edit.subclassToSave?.id) {
            return result
        }
        return repository.updateSubclass(characterId, characterClass, edit.subclassToSave?.id)
    }

    /** Loads the character (also the first time), and then the class catalog. */
    fun retry() {
        _uiState.value = CharacterSheetUiState.Loading
        viewModelScope.launch {
            _uiState.value = when (val result = repository.get(characterId)) {
                is RepositoryResult.Success -> CharacterSheetUiState.Content(result.value)
                is RepositoryResult.Failure -> CharacterSheetUiState.Error(result.error)
            }
            if (_uiState.value is CharacterSheetUiState.Content) loadClassCatalog()
        }
    }

    /** If it fails the sheet works the same, without choosing subclasses; it's retried on the next class edit. */
    private fun loadClassCatalog() {
        viewModelScope.launch {
            val catalog = (repository.listClasses() as? RepositoryResult.Success)?.value ?: return@launch
            _uiState.update { current ->
                val content = current as? CharacterSheetUiState.Content ?: return@update current
                val edit = content.edit
                content.copy(
                    classCatalog = catalog,
                    edit = if (edit is SheetEdit.Class) edit.copy(catalog = catalog) else edit,
                )
            }
        }
    }
}

/** The content, only if it is in edit mode. */
private fun CharacterSheetUiState.editing(): CharacterSheetUiState.Content? =
    (this as? CharacterSheetUiState.Content)?.takeIf { it.isEditing }

/** The character the server returned after a save; stays in edit mode, without dialog or errors. */
private fun CharacterSheetUiState.withCharacter(character: Character): CharacterSheetUiState =
    (this as? CharacterSheetUiState.Content)?.copy(
        character = character,
        isSavingSkills = false,
        skillsError = null,
        edit = null,
        isSavingEdit = false,
        editError = null,
    ) ?: CharacterSheetUiState.Content(character)
