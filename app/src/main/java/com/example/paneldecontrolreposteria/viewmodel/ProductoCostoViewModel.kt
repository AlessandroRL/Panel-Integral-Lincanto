package com.example.paneldecontrolreposteria.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.paneldecontrolreposteria.model.Ingrediente
import com.example.paneldecontrolreposteria.model.Producto
import com.example.paneldecontrolreposteria.model.ProductoCosto
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProductoCostoViewModel : ViewModel() {
    private val repository = ProductoCostoRepository(FirebaseFirestore.getInstance())

    private val _productosBase = MutableStateFlow<List<Producto>>(emptyList())
    val productosBase: StateFlow<List<Producto>> = _productosBase

    private val _ingredientesBase = MutableStateFlow<List<Ingrediente>>(emptyList())
    val ingredientesBase: StateFlow<List<Ingrediente>> = _ingredientesBase

    private val _productosCosto = MutableStateFlow<List<ProductoCosto>>(emptyList())
    val productosCosto: StateFlow<List<ProductoCosto>> = _productosCosto

    private val _cargando = MutableStateFlow(false)
    val cargando: StateFlow<Boolean> = _cargando

    private val _ingredientesDisponibles = MutableStateFlow<List<String>>(emptyList())
    val ingredientesDisponibles: StateFlow<List<String>> = _ingredientesDisponibles

    suspend fun obtenerIngredientesBase(): List<Ingrediente> {
        return repository.obtenerIngredientesBase()
    }

    fun cargarProductosBase() {
        viewModelScope.launch {
            _cargando.value = true
            _productosBase.value = repository.obtenerProductosBase()
            _cargando.value = false
        }
    }

    fun cargarProductosCosto() {
        viewModelScope.launch {
            _cargando.value = true
            _productosCosto.value = repository.obtenerProductosCosto()
            _cargando.value = false
        }
    }

    suspend fun crearProductoCostoDesdePlantilla(producto: Producto): ProductoCosto {
        return repository.crearProductoCostoDesdePlantilla(producto)
    }

    fun guardarProductoCosto(productoCosto: ProductoCosto) {
        viewModelScope.launch {
            repository.guardarProductoCosto(productoCosto)
            cargarProductosCosto()
        }
    }

    fun eliminarProductoCosto(nombre: String) {
        viewModelScope.launch {
            repository.eliminarProductoCosto(nombre)
            cargarProductosCosto()
        }
    }

    fun actualizarProductoCosto(productoCosto: ProductoCosto) {
        viewModelScope.launch {
            repository.actualizarProductoCosto(productoCosto)
            cargarProductosCosto()
        }
    }

    fun cargarIngredientesBase() {
        viewModelScope.launch {
            val ingredientes = repository.obtenerIngredientesBase()
            _ingredientesBase.value = ingredientes
            _ingredientesDisponibles.value = ingredientes.map { it.nombre }
        }
    }
}