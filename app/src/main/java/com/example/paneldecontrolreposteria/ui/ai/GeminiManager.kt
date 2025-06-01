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

    fun construirPromptParaPedido(instruccion: String): String {
        return """
        Analiza esta instrucción de usuario y responde SOLO en formato JSON. 
        Las posibles acciones son: agregar, editar o eliminar un pedido.

        Cada producto debe incluir:
        - nombre (string)
        - tamano (int) = número de personas
        - cantidad (int) = número de unidades

        Ejemplo de respuesta:
        {
          "accion": "agregar",
          "cliente": "Ana",
          "productos": [
            { "nombre": "torta", "tamano": 10, "cantidad": 1 },
            { "nombre": "cupcake", "tamano": 1, "cantidad": 2 }
          ]
        }

        Ahora responde según esta instrucción:
        "$instruccion"
    """.trimIndent()
    }
}