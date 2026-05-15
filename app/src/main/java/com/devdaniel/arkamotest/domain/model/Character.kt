package com.devdaniel.arkamotest.domain.model

data class Character(
    val id: Int,
    val name: String,
    val image: String,
    val status: CharacterStatus,
)
