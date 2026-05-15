package com.devdaniel.arkamotest.domain.model

enum class CharacterStatus {
    Alive,
    Dead,
    Unknown,
    ;

    companion object {
        fun fromApi(raw: String): CharacterStatus = when (raw.lowercase()) {
            "alive" -> Alive
            "dead" -> Dead
            else -> Unknown
        }
    }
}
