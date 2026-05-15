package com.devdaniel.arkamotest.ui.characters

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.devdaniel.arkamotest.domain.model.Character
import com.devdaniel.arkamotest.ui.characters.components.CharacterItem
import com.devdaniel.arkamotest.ui.characters.components.ErrorContent
import com.devdaniel.arkamotest.ui.characters.components.LoadingContent
import com.devdaniel.arkamotest.ui.characters.state.CharactersUiState

@Composable
fun CharactersScreen(
    modifier: Modifier = Modifier,
    viewModel: CharactersViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    Box(modifier.fillMaxSize()) {
        when (val s = state) {
            CharactersUiState.Loading -> LoadingContent()
            is CharactersUiState.Success -> CharactersList(s.characters)
            is CharactersUiState.Error -> ErrorContent(
                message = s.message,
                onRetry = viewModel::onRetry,
            )
        }
    }
}

@Composable
private fun CharactersList(characters: List<Character>) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(characters, key = { it.id }) { character ->
            CharacterItem(character)
            HorizontalDivider()
        }
    }
}
