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
    fun procesarInstruccion(texto: String) {
        geminiViewModel.procesarInstruccion(texto)
    }

    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    fun interpretarYActuar(respuestaGemini: String, onRespuestaFinal: (String) -> Unit) {
        val comando = interpreter.interpretar(respuestaGemini)
        coroutineScope.launch {
            val resultado = interpreter.ejecutar(comando)
            onRespuestaFinal(resultado)
        }
    }
}