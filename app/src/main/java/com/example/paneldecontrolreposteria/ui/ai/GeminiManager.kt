package com.example.paneldecontrolreposteria.ui.ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

class GeminiManager {

    suspend fun obtenerRespuesta(instruccionUsuario: String): String = withContext(Dispatchers.IO) {
        try {
            val prompt = construirPromptParaPedido(instruccionUsuario)

            val request = GeminiRequest(
                contents = listOf(
                    GeminiContent(
                        parts = listOf(GeminiPart(text = prompt))
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
        Eres un asistente que interpreta instrucciones sobre pedidos de repostería. 
        Devuelve exclusivamente un objeto JSON válido con los siguientes campos, según la intención detectada:

        - "intencion": uno de ["agregar", "editar", "eliminar"]
        - "cliente": nombre del cliente (string)
        - "productos": lista de objetos con:
            - "nombre": nombre del producto (string)
            - "tamano": número de personas o tamaño del producto (int)
            - "cantidad": cantidad de unidades (int)
        - "fechaLimite": solo si la intención es "agregar" o "editar", debe estar en formato "yyyy-MM-dd". Este campo es obligatorio en esos casos.

        Si la intención es "eliminar", omite el campo "productos" y "fechaLimite".

        Devuelve solo el JSON. No escribas ningún texto adicional, explicación ni etiquetas como "Respuesta:" o "Output:".

        Ejemplo válido:
        {
          "intencion": "agregar",
          "cliente": "María",
          "fechaLimite": "2025-06-10",
          "productos": [
            { "nombre": "pastel", "tamano": 10, "cantidad": 1 },
            { "nombre": "cupcake", "tamano": 1, "cantidad": 3 }
          ]
        }

        Instrucción del usuario:
        "$instruccion"
    """.trimIndent()
    }
}