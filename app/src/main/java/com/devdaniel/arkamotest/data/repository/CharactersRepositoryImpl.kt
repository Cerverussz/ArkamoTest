package com.devdaniel.arkamotest.data.repository

import com.devdaniel.arkamotest.data.mapper.toDomain
import com.devdaniel.arkamotest.data.remote.RickAndMortyApi
import com.devdaniel.arkamotest.di.IoDispatcher
import com.devdaniel.arkamotest.domain.model.Character
import com.devdaniel.arkamotest.domain.repository.CharactersRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CharactersRepositoryImpl @Inject constructor(
    private val api: RickAndMortyApi,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : CharactersRepository {

    override suspend fun getCharacters(): Result<List<Character>> =
        withContext(ioDispatcher) {
            try {
                Result.success(api.getCharacters().results.map { it.toDomain() })
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                Result.failure(e)
            }
        }
}
