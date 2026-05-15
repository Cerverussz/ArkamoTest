package com.devdaniel.arkamotest.ui.characters.state

import com.devdaniel.arkamotest.domain.model.Character

sealed interface CharactersUiState {
    data object Loading : CharactersUiState
    data class Success(val characters: List<Character>) : CharactersUiState
    data class Error(val message: String) : CharactersUiState
}
