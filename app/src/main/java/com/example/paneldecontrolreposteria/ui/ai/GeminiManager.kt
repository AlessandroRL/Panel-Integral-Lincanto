package com.example.paneldecontrolreposteria.ui.ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

class GeminiManager {

    suspend fun obtenerRespuesta(instruccion: String): String = withContext(Dispatchers.IO) {
        try {
            val request = GeminiRequest(
                contents = listOf(
                    GeminiContent(
                        parts = listOf(GeminiPart(text = instruccion))
                    )
                )
            )

            val response = GeminiService.api.generarRespuesta(request)

            if (response.isSuccessful) {
                val textoRespuesta = response.body()
                    ?.candidates
                    ?.firstOrNull()
                    ?.content
                    ?.parts
                    ?.firstOrNull()
                    ?.text

                return@withContext textoRespuesta ?: "No se recibió respuesta del modelo."
            } else {
                return@withContext "Error: ${response.code()} ${response.errorBody()?.string()}"
            }
        } catch (e: Exception) {
            return@withContext "Error al conectarse con Gemini: ${e.localizedMessage}"
        }
    }
}