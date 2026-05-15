package com.devdaniel.arkamotest.ui.characters.state

import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

fun Throwable.toUserMessage(): String = when (this) {
    is UnknownHostException -> "Sin conexión. Verifica tu internet."
    is SocketTimeoutException -> "La conexión tardó demasiado. Intenta de nuevo."
    is HttpException -> if (code() in 500..599) {
        "El servidor está caído. Intenta en un momento."
    } else {
        "No se pudieron cargar los personajes."
    }
    is IOException -> "Problema de red. Intenta de nuevo."
    is SerializationException -> "Respuesta del servidor inválida."
    else -> "Algo salió mal. Intenta de nuevo."
}
