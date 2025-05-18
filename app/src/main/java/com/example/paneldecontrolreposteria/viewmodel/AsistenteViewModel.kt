package com.example.paneldecontrolreposteria.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.paneldecontrolreposteria.ui.asistente.voice.SpeechRecognizerManager
import kotlinx.coroutines.launch

class AsistenteViewModel : ViewModel() {

    val textoReconocido = mutableStateOf("")
    val estaEscuchando = mutableStateOf(false)
    val errorReconocimiento = mutableStateOf("")

    fun iniciarEscucha(contexto: android.content.Context) {
        errorReconocimiento.value = ""
        estaEscuchando.value = true

        val recognizer = SpeechRecognizerManager(
            context = contexto,
            onResult = { resultado ->
                textoReconocido.value = resultado
                estaEscuchando.value = false
                procesarTextoReconocido(resultado)
            },
            onError = { error ->
                errorReconocimiento.value = error
                estaEscuchando.value = false
            }
        )

        recognizer.startListening()
    }

    private fun procesarTextoReconocido(texto: String) {
        viewModelScope.launch {
            // TODO: Aquí irá la llamada a Gemini o lógica de comandos personalizados
            // Por ejemplo:
            // if (texto.lowercase().contains("agrega un pedido")) { ... }
        }
    }

    fun resetear() {
        textoReconocido.value = ""
        errorReconocimiento.value = ""
        estaEscuchando.value = false
    }
}
