package com.devdaniel.arkamotest.domain.usecase

import com.devdaniel.arkamotest.domain.model.Character
import com.devdaniel.arkamotest.domain.repository.CharactersRepository
import javax.inject.Inject

class GetCharactersUC @Inject constructor(
    private val repository: CharactersRepository,
) {
    suspend operator fun invoke(): Result<List<Character>> = repository.getCharacters()
}
