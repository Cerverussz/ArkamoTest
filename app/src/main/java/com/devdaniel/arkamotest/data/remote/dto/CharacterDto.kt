package com.devdaniel.arkamotest.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CharacterDto(
    val id: Int,
    val name: String,
    val status: String,
    val image: String,
)
