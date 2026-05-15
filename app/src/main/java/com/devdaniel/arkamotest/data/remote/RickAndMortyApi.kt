package com.devdaniel.arkamotest.data.remote

import com.devdaniel.arkamotest.data.remote.dto.CharactersResponseDto
import retrofit2.http.GET

interface RickAndMortyApi {
    @GET("character")
    suspend fun getCharacters(): CharactersResponseDto
}
