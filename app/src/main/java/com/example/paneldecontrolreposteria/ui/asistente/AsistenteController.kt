package com.example.paneldecontrolreposteria.ui.asistente

import com.example.paneldecontrolreposteria.ui.ai.GeminiCommandInterpreter

class AsistenteController(private val interpreter: GeminiCommandInterpreter) {

    fun interpretarYActuar(respuestaGemini: String, onRespuestaFinal: (String) -> Unit) {
        val comando = interpreter.interpretar(respuestaGemini)
        val resultado = interpreter.ejecutar(comando)
        onRespuestaFinal(resultado)
    }
}
