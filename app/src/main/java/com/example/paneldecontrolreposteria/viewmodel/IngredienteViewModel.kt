package com.example.paneldecontrolreposteria.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.paneldecontrolreposteria.model.Ingrediente
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class IngredienteViewModel : ViewModel() {
    private val repository = IngredienteRepository()

    private val _ingredientes = MutableStateFlow<List<Ingrediente>>(emptyList())
    val ingredientes: StateFlow<List<Ingrediente>> = _ingredientes

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
}