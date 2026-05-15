package com.devdaniel.arkamotest.domain.repository

import com.devdaniel.arkamotest.domain.model.Character

interface CharactersRepository {
    suspend fun getCharacters(): Result<List<Character>>
}
