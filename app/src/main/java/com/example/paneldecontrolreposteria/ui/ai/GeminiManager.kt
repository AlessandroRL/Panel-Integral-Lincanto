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
        return when {
            Regex("consultar|cu[aá]les?|qu[eé]|tengo|hay|mu[eé]strame|lista|cu[aá]ntos?", RegexOption.IGNORE_CASE).containsMatchIn(instruccion) -> {
                construirPromptParaConsultaInformativa(instruccion)
            }

            Regex("ingrediente[s]?", RegexOption.IGNORE_CASE).containsMatchIn(instruccion) -> {
                construirPromptParaIngrediente(instruccion)
            }

            Regex("producto[s]?", RegexOption.IGNORE_CASE).containsMatchIn(instruccion) -> {
                construirPromptParaProducto(instruccion)
            }

            else -> {
                construirPromptParaPedido(instruccion)
            }
        }
    return """

        IMPORTANTE:
        - Si en la misma instruccion se mencionan las palabras "pedido" y "producto", asume que se trata de un pedido y no de un producto general.
        - "Agregar un producto a un pedido" NO es lo mismo que "Agregar un nuevo producto a la base de productos".
        - "Agregar un ingrediente a un producto" NO es lo mismo que "Agregar un nuevo ingrediente a la base de ingredientes".
        - Si la instrucción habla de un pedido, se espera una acción sobre un pedido.
        - Si la instrucción habla de un producto, asume que es sobre su contenido, no sobre la base general.
        - En general interpreta correctamente la instrucción según el contexto de pedidos, productos o ingredientes.
        Instrucción: "$instruccion"
        """.trimIndent()
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
        Ademas no tengas e cuenta tildes para ningún campo.
        
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
        - Si en la misma instruccion se mencionan las palabras "pedido" y "producto", asume que se trata de un pedido y no de un producto general.

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
        Ademas no tengas e cuenta tildes para ningún campo.

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

    private fun construirPromptParaProducto(instruccion: String): String {
        return """
        Eres un asistente experto en gestión de productos de repostería. 
        Analiza el siguiente texto del usuario y devuelve un JSON limpio que identifique la intención y los campos claves del producto.
        
        En caso de agregar o editar un producto, haz que la primera letra de la primera palabra esté en mayúscula (lo mismo al agregar o editar un ingrediente).
        Ademas no tengas e cuenta tildes para ningún campo.
        
        El JSON debe tener:
        - tipo: "producto"
        - intencion: "agregar", "editar" o "eliminar"
        - nombre: nombre del producto
        - ingredientes: lista de objetos con nombre, unidad (la unidad solo puede ser gr, ml o unidad), cantidad y observación (si aplica)
        - preparacion: texto opcional
        - utensilios: lista opcional
        - tips: texto opcional

        Notas especiales:
        - Si el usuario quiere eliminar un ingrediente específico, incluye ese ingrediente en la lista con cantidad 0.
        - Si quiere eliminar varios ingredientes, incluye todos con cantidad 0.
        - Si quiere eliminar todos los ingredientes, ingredientes debe ser una lista vacía [].
        - No incluyas campos como null si no se mencionan explícitamente. Omítelos.
        - No reemplaces campos existentes a menos que el usuario lo indique. 
        - Si en la misma instruccion se mencionan las palabras "pedido" y "producto", asume que se trata de un pedido y no de un producto general.

        Formato esperado para agregar:
        {
          "tipo": "producto",
          "intencion": "agregar",
          "nombre": "Tarta de manzana",
          "preparacion": "Mezclar los ingredientes y hornear a 180°C por 40 minutos",
          "tips": "Servir con helado"
        }

        Formato esperado para eliminar:
        {
          "tipo": "producto",
          "intencion": "eliminar",
          "nombre": "Tarta clásica"
        }

        Solo responde el JSON, sin explicaciones.
        
        Instrucción del usuario:
        "$instruccion"
    """.trimIndent()
    }

    private fun construirPromptParaConsultaInformativa(instruccion: String): String {
        return """
        Eres un asistente de gestión de una repostería. El usuario hará preguntas informativas sobre los pedidos, ingredientes o productos.

        Devuelve exclusivamente un objeto JSON, sin ningún texto adicional.
        Si no se menciona específicamente la palabra "consulta", no se trata de una consulta informativa.

        FORMATO DEL JSON SEGÚN TIPO DE CONSULTA:

        - Para consultar pedidos:
        {
          "intencion": "consultar_pedidos",
          "rango": "semana" // puede ser "hoy", "mañana", "semana", "mes" o "todos"
        }

        - Para saber si un ingrediente existe:
        {
          "intencion": "consultar_ingredientes",
          "nombreIngrediente": "canela"
        }

        - Para saber cuántos ingredientes hay en total:
        {
          "intencion": "consultar_ingredientes",
          "cantidadTotal": true
        }

        - Para listar todos los ingredientes:
        {
          "intencion": "consultar_ingredientes"
        }

        - Para listar productos registrados:
        {
          "intencion": "consultar_productos"
        }

        - Para obtener toda la información de un producto específico:
        {
          "intencion": "consultar_productos",
          "nombreProducto": "torta tres leches"
        }

        - Para obtener una parte específica de un producto (ingredientes, preparación, utensilios o tips):
        {
          "intencion": "consultar_productos",
          "nombreProducto": "torta tres leches",
          "campo": "ingredientes" // puede ser "ingredientes", "preparacion", "utensilios", "tips" o "todo"
        }

        IMPORTANTE:
        - Siempre responde solo con un JSON.
        - No agregues explicaciones ni texto adicional.
        - No incluyas saltos de línea innecesarios.
        - Usa exactamente los nombres de clave indicados.
        
        Instrucción: "$instruccion"
    """.trimIndent()
    }

    suspend fun obtenerRespuestaLibre(pregunta: String): String = withContext(Dispatchers.IO) {
        try {
            val request = GeminiRequest(
                contents = listOf(
                    GeminiContent(
                        parts = listOf(GeminiPart(text = pregunta))
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