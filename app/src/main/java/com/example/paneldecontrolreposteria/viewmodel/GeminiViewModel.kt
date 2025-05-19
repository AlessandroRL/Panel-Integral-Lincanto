package com.example.paneldecontrolreposteria.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.paneldecontrolreposteria.ui.ai.GeminiManager
import kotlinx.coroutines.launch

class GeminiViewModel : ViewModel() {

    private val geminiManager = GeminiManager()

    private val _respuesta = MutableLiveData<String>()
    val respuesta: LiveData<String> = _respuesta

    fun procesarInstruccion(texto: String) {
        _respuesta.value = "Procesando..."
        viewModelScope.launch {
            val resultado = geminiManager.obtenerRespuesta(texto)
            _respuesta.value = resultado
        }
    }
}

