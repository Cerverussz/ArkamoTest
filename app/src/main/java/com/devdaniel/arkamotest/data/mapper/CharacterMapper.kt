package com.devdaniel.arkamotest.data.mapper

import com.devdaniel.arkamotest.data.remote.dto.CharacterDto
import com.devdaniel.arkamotest.domain.model.Character
import com.devdaniel.arkamotest.domain.model.CharacterStatus

internal fun CharacterDto.toDomain(): Character = Character(
    id = id,
    name = name,
    image = image,
    status = CharacterStatus.fromApi(status),
)
