package com.example.paneldecontrolreposteria.viewmodel

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.paneldecontrolreposteria.model.Ingrediente
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class IngredienteViewModel : ViewModel() {
    private val repository = IngredienteRepository()

    private val _ingredientes = MutableStateFlow<List<Ingrediente>>(emptyList())
    val ingredientes: StateFlow<List<Ingrediente>> = _ingredientes
    private val _busqueda = MutableStateFlow("")
    val busqueda: StateFlow<String> = _busqueda

    init {
        cargarIngredientes()
    }

    fun cargarIngredientes() {
        viewModelScope.launch {
            _ingredientes.value = repository.obtenerIngredientes()
        }
    }

    fun agregarIngrediente(ingrediente: Ingrediente) {
        viewModelScope.launch {
            repository.agregarIngrediente(ingrediente)
            cargarIngredientes()
        }
    }

    fun editarIngrediente(ingrediente: Ingrediente) {
        viewModelScope.launch {
            repository.agregarIngrediente(ingrediente) // Reutilizamos set con el mismo ID
            cargarIngredientes()
        }
    }

    fun eliminarIngrediente(id: String) {
        viewModelScope.launch {
            repository.eliminarIngrediente(id)
            cargarIngredientes()
        }
    }

    fun actualizarBusqueda(texto: String) {
        _busqueda.value = texto
    }

    val ingredientesFiltrados: StateFlow<List<Ingrediente>> = combine(
        ingredientes, busqueda
    ) { lista, texto ->
        if (texto.isBlank()) lista
        else lista.filter { it.nombre.contains(texto, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

}