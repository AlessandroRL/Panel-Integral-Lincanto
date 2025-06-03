package com.example.paneldecontrolreposteria.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.paneldecontrolreposteria.ui.ai.GeminiManager
import com.example.paneldecontrolreposteria.ui.asistente.AsistenteController
import kotlinx.coroutines.launch

class GeminiViewModel : ViewModel() {

    private val geminiManager = GeminiManager()

    private val _respuesta = MutableLiveData<String>()
    val respuesta: LiveData<String> = _respuesta

    @RequiresApi(Build.VERSION_CODES.O)
    fun procesarYEjecutar(
        texto: String,
        asistenteController: AsistenteController,
        onRespuestaFinal: (String) -> Unit
    ) {
        _respuesta.value = "Procesando..."
        viewModelScope.launch {
            val resultado = geminiManager.obtenerRespuesta(texto)
            _respuesta.value = resultado
            asistenteController.interpretarYActuar(resultado, onRespuestaFinal)
        }
    }
}