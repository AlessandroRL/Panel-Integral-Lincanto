package com.example.paneldecontrolreposteria.ui.ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GeminiManager {

    suspend fun obtenerRespuesta(instruccionUsuario: String): String = withContext(Dispatchers.IO) {
        try {
            val prompt = construirPrompt(instruccionUsuario)

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

    private fun construirPrompt(instruccion: String): String {
        return if (Regex("ingrediente[s]?", RegexOption.IGNORE_CASE).containsMatchIn(instruccion)) {
            construirPromptParaIngrediente(instruccion)
        } else {
            construirPromptParaPedido(instruccion)
        }
    }

    private fun construirPromptParaPedido(instruccion: String): String {
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

        Si la intención es "editar", incluye SOLO los campos que el usuario quiere modificar:
        - "productos": si desea cambiar o añadir productos (formato igual al anterior)
        - "fechaLimite": si desea modificar la fecha (formato "yyyy-MM-dd")
        
        Para editar pedidos puedes:
        - Cambiar la fecha límite.
        - Agregar nuevos productos.
        - Modificar productos existentes (mismo nombre y tamaño).
        - Eliminar productos (usando cantidad 0).

        Devuelve solo el JSON. No escribas ningún texto adicional, explicación ni etiquetas como "Respuesta:" o "Output:".

        En el nombre del pedido haz que la primera letra de cada palabra esté en mayúscula, excepto artículos y preposiciones (ejemplo: "Pastel De Chocolate").
        
        Ejemplo válido de agregar un pedido:
        {
          "intencion": "agregar",
          "cliente": "María",
          "fechaLimite": "2025-06-10",
          "productos": [
            { "nombre": "pastel", "tamano": 10, "cantidad": 1 },
            { "nombre": "cupcake", "tamano": 1, "cantidad": 3 }
          ]
        }
        
        Ejemplo de edición parcial:
        {
          "intencion": "editar",
          "cliente": "nombre del cliente",
          "fechaLimite": "AAAA-MM-DD", // opcional
          "productos": [
          {
            "nombre": "nombre del producto",
            "tamano": tamaño en personas,
            "cantidad": cantidad deseada (0 para eliminar)
          }
          ]
        }
        
        - Si la intención es "eliminar", omite el campo "productos" y "fechaLimite".
        - Si se desea eliminar un producto, inclúyelo en la lista de productos con cantidad igual a 0.
        - No reemplaces todos los productos del pedido a menos que se indique explícitamente.

        Ejemplo de respuesta para eliminar un producto:
        {
          "intencion": "editar",
          "cliente": "Andrea",
          "productos": [
            {
              "nombre": "Pastel de vainilla",
              "tamano": 15,
              "cantidad": 0
            }
          ]
        }

        Instrucción del usuario:
        "$instruccion"
    """.trimIndent()
    }

    private fun construirPromptParaIngrediente(instruccion: String): String {
        return """
        Eres un asistente que interpreta instrucciones sobre ingredientes de repostería.
        Devuelve exclusivamente un objeto JSON válido con los siguientes campos:
        
        En caso de agregar o editar un ingrediente: haz que la primera letra de la primera palabra esté en mayúscula.

        - "tipo": "ingrediente"
        - "intencion": uno de ["agregar", "editar", "eliminar"]
        - "nombre": nombre del ingrediente (string)
        - "unidad": unidad de medida (string, valores posibles: "gr", "ml", "unidad")
        - "costoUnidad": costo numérico por unidad (float)

        Para "eliminar", solo se necesita el campo "nombre".

        Devuelve solo el JSON, sin explicaciones.

        Instrucción:
        "$instruccion"
        """.trimIndent()
    }
}