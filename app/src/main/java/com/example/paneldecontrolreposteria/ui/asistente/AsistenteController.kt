package com.example.paneldecontrolreposteria.ui.asistente

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.paneldecontrolreposteria.ui.ai.GeminiCommandInterpreter

class AsistenteController(private val interpreter: GeminiCommandInterpreter) {

    @RequiresApi(Build.VERSION_CODES.O)
    fun interpretarYActuar(respuestaGemini: String, onRespuestaFinal: (String) -> Unit) {
        val comando = interpreter.interpretar(respuestaGemini)
        val resultado = interpreter.ejecutar(comando)
        onRespuestaFinal(resultado)
    }
}
