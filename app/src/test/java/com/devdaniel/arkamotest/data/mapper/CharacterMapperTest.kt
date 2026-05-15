package com.devdaniel.arkamotest.data.mapper

import com.devdaniel.arkamotest.data.remote.dto.CharacterDto
import com.devdaniel.arkamotest.domain.model.CharacterStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class CharacterMapperTest {

    @Test
    fun `toDomain maps Alive status correctly`() {
        val dto = CharacterDto(id = 1, name = "Rick", status = "Alive", image = "url")

        val result = dto.toDomain()

        assertEquals(CharacterStatus.Alive, result.status)
        assertEquals(1, result.id)
        assertEquals("Rick", result.name)
        assertEquals("url", result.image)
    }

    @Test
    fun `toDomain maps Dead status correctly`() {
        val dto = CharacterDto(id = 2, name = "Birdperson", status = "Dead", image = "url")

        val result = dto.toDomain()

        assertEquals(CharacterStatus.Dead, result.status)
    }

    @Test
    fun `toDomain maps unknown and unrecognized status to Unknown`() {
        val unknownDto = CharacterDto(id = 3, name = "??", status = "unknown", image = "url")
        val garbageDto = CharacterDto(id = 4, name = "??", status = "garbage", image = "url")

        assertEquals(CharacterStatus.Unknown, unknownDto.toDomain().status)
        assertEquals(CharacterStatus.Unknown, garbageDto.toDomain().status)
    }
}
