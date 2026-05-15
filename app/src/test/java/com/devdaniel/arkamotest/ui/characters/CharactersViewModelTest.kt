package com.devdaniel.arkamotest.ui.characters

import com.devdaniel.arkamotest.domain.model.Character
import com.devdaniel.arkamotest.domain.model.CharacterStatus
import com.devdaniel.arkamotest.domain.usecase.GetCharactersUC
import com.devdaniel.arkamotest.ui.characters.state.CharactersUiState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import java.net.UnknownHostException

@OptIn(ExperimentalCoroutinesApi::class)
class CharactersViewModelTest {

    private val getCharacters: GetCharactersUC = mockk()
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `happy path emits Loading then Success`() = runTest {
        val expected = listOf(
            Character(id = 1, name = "Rick", image = "url", status = CharacterStatus.Alive),
        )
        coEvery { getCharacters() } returns Result.success(expected)
        val viewModel = CharactersViewModel(getCharacters)

        val collector = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        assertEquals(CharactersUiState.Success(expected), viewModel.uiState.value)
        collector.cancel()
    }

    @Test
    fun `network failure emits Loading then Error with no-internet message`() = runTest {
        coEvery { getCharacters() } returns Result.failure(UnknownHostException("no net"))
        val viewModel = CharactersViewModel(getCharacters)

        val collector = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is CharactersUiState.Error)
        assertEquals(
            "Sin conexión. Verifica tu internet.",
            (state as CharactersUiState.Error).message,
        )
        collector.cancel()
    }

    @Test
    fun `http 500 emits Loading then Error with server-down message`() = runTest {
        val httpError = HttpException(
            retrofit2.Response.error<Any>(
                500,
                "".toResponseBody("text/plain".toMediaType()),
            ),
        )
        coEvery { getCharacters() } returns Result.failure(httpError)
        val viewModel = CharactersViewModel(getCharacters)

        val collector = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is CharactersUiState.Error)
        assertEquals(
            "El servidor está caído. Intenta en un momento.",
            (state as CharactersUiState.Error).message,
        )
        collector.cancel()
    }

    @Test
    fun `onRetry after error re-emits Loading then Success`() = runTest {
        val expected = listOf(
            Character(id = 1, name = "Rick", image = "url", status = CharacterStatus.Alive),
        )
        coEvery { getCharacters() } returnsMany listOf(
            Result.failure(UnknownHostException("no net")),
            Result.success(expected),
        )
        val viewModel = CharactersViewModel(getCharacters)

        val collector = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is CharactersUiState.Error)

        viewModel.onRetry()
        advanceUntilIdle()

        assertEquals(CharactersUiState.Success(expected), viewModel.uiState.value)
        collector.cancel()
    }

    @Test
    fun `onStart triggers load only once per WhileSubscribed window`() = runTest {
        val expected = listOf(
            Character(id = 1, name = "Rick", image = "url", status = CharacterStatus.Alive),
        )
        coEvery { getCharacters() } returns Result.success(expected)
        val viewModel = CharactersViewModel(getCharacters)

        val c1 = launch { viewModel.uiState.collect {} }
        val c2 = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        coVerify(exactly = 1) { getCharacters() }
        c1.cancel()
        c2.cancel()
    }
}
