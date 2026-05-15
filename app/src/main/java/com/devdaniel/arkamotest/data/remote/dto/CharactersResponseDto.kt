package com.devdaniel.arkamotest.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CharactersResponseDto(
    val results: List<CharacterDto>,
)
