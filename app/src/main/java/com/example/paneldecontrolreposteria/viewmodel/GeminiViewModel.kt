package com.example.paneldecontrolreposteria.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.paneldecontrolreposteria.ui.ai.GeminiManager
import com.example.paneldecontrolreposteria.ui.asistente.AsistenteController
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class GeminiViewModel : ViewModel() {

    private val geminiManager = GeminiManager()

    data class MensajeAsistente(val emisor: Emisor, val contenido: String)
    enum class Emisor { USUARIO, ASISTENTE }
    var hablarRespuestas by mutableStateOf(false)


    private val _mensajes = MutableLiveData<List<MensajeAsistente>>(emptyList())
    val mensajes: LiveData<List<MensajeAsistente>> = _mensajes

    private fun agregarMensaje(contenido: String, emisor: Emisor) {
        val nuevos = _mensajes.value.orEmpty() + MensajeAsistente(emisor, contenido)
        _mensajes.value = nuevos
    }

    private fun formatearRespuestaLegible(respuesta: String): String {
        val inicio = respuesta.indexOf("{")
        val fin = respuesta.lastIndexOf("}")
        if (inicio != -1 && fin != -1 && fin > inicio) {
            val posibleJson = respuesta.substring(inicio, fin + 1)
            return try {
                val json = JSONObject(posibleJson)
                buildString {
                    json.keys().forEach { clave ->
                        val valor = json.get(clave)
                        val valorFormateado = when (valor) {
                            is JSONArray -> {
                                if (valor.length() > 0 && valor.get(0) is JSONObject) {
                                    // Lista de objetos
                                    (0 until valor.length()).joinToString("\n") { i ->
                                        val obj = valor.getJSONObject(i)
                                        val detalles = obj.keys().asSequence().joinToString("\n") { k ->
                                            "   - ${k.replaceFirstChar { it.uppercase() }}: ${obj.get(k)}"
                                        }
                                        "•\n$detalles"
                                    }
                                } else {
                                    // Lista simple
                                    (0 until valor.length()).joinToString("\n") { i -> "• ${valor.get(i)}" }
                                }
                            }
                            is JSONObject -> {
                                valor.keys().asSequence().joinToString("\n") { k ->
                                    "  ${k.replaceFirstChar { it.uppercase() }}: ${valor.get(k)}"
                                }
                            }
                            else -> valor.toString()
                        }
                        appendLine("${clave.replaceFirstChar { it.uppercase() }}:\n$valorFormateado")
                    }
                }.trim()
            } catch (_: Exception) {
                respuesta
            }
        }

        return respuesta
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun procesarYEjecutar(
        texto: String,
        asistenteController: AsistenteController,
        onRespuestaFinal: (String) -> Unit
    ) {
        agregarMensaje(texto, Emisor.USUARIO)

        val respuestaLocal = asistenteController.procesarRespuestaSimple(texto)
        if (respuestaLocal != null) {
            agregarMensaje(respuestaLocal, Emisor.ASISTENTE)
            onRespuestaFinal(respuestaLocal)
            return
        }

        viewModelScope.launch {
            val resultado = geminiManager.obtenerRespuesta(texto)
            val resultadoFormateado = formatearRespuestaLegible(resultado)
            agregarMensaje(resultadoFormateado, Emisor.ASISTENTE)
            asistenteController.interpretarYActuar(resultado) { respuestaFinal ->
                if (respuestaFinal.isNotBlank()) {
                    agregarMensaje(respuestaFinal, Emisor.ASISTENTE)
                }
                onRespuestaFinal(respuestaFinal)
            }
        }
    }
}