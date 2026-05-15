package com.devdaniel.arkamotest.ui.characters

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devdaniel.arkamotest.domain.usecase.GetCharactersUC
import com.devdaniel.arkamotest.ui.characters.state.CharactersUiState
import com.devdaniel.arkamotest.ui.characters.state.toUserMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CharactersViewModel @Inject constructor(
    private val getCharacters: GetCharactersUC,
) : ViewModel() {

    private val _uiState = MutableStateFlow<CharactersUiState>(CharactersUiState.Loading)
    val uiState: StateFlow<CharactersUiState> = _uiState
        .onStart { load() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = CharactersUiState.Loading,
        )

    fun onRetry() = load()

    private fun load() {
        viewModelScope.launch {
            _uiState.value = CharactersUiState.Loading
            getCharacters()
                .onSuccess { _uiState.value = CharactersUiState.Success(it) }
                .onFailure { _uiState.value = CharactersUiState.Error(it.toUserMessage()) }
        }
    }
}
