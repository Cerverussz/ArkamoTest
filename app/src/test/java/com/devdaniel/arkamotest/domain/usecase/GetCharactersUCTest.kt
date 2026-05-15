package com.devdaniel.arkamotest.domain.usecase

import com.devdaniel.arkamotest.domain.model.Character
import com.devdaniel.arkamotest.domain.model.CharacterStatus
import com.devdaniel.arkamotest.domain.repository.CharactersRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetCharactersUCTest {

    private val repository: CharactersRepository = mockk()
    private val useCase = GetCharactersUC(repository)

    @Test
    fun `invoke delegates to repository and propagates Success`() = runTest {
        val expected = listOf(
            Character(id = 1, name = "Rick", image = "url", status = CharacterStatus.Alive),
        )
        coEvery { repository.getCharacters() } returns Result.success(expected)

        val result = useCase()

        assertEquals(Result.success(expected), result)
        coVerify(exactly = 1) { repository.getCharacters() }
    }

    @Test
    fun `invoke propagates Failure from repository`() = runTest {
        val error = RuntimeException("boom")
        coEvery { repository.getCharacters() } returns Result.failure(error)

        val result = useCase()

        assertTrue(result.isFailure)
        assertEquals(error, result.exceptionOrNull())
    }
}
