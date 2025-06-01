package com.example.paneldecontrolreposteria.ui.asistente

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.paneldecontrolreposteria.ui.ai.GeminiCommandInterpreter
import com.example.paneldecontrolreposteria.viewmodel.GeminiViewModel

class AsistenteController(
    private val interpreter: GeminiCommandInterpreter,
    private val geminiViewModel: GeminiViewModel
) {
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    fun procesarInstruccion(texto: String) {
        geminiViewModel.procesarInstruccion(texto)
    }

    /**
     * Interpreta la respuesta generada por Gemini y ejecuta el comando correspondiente.
     * @param respuestaGemini Respuesta en lenguaje natural generada por Gemini.
     * @param onRespuestaFinal Callback con el resultado textual de la acción realizada.
     */
    fun interpretarYActuar(respuestaGemini: String, onRespuestaFinal: (String) -> Unit) {
        coroutineScope.launch {
            try {
                val comando = interpreter.interpretar(respuestaGemini)
                val resultado = interpreter.ejecutar(comando)
                onRespuestaFinal(resultado)
            } catch (e: Exception) {
                onRespuestaFinal("Ocurrió un error al procesar la instrucción: ${e.message}")
            }
        }
    }
}