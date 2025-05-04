package com.example.paneldecontrolreposteria.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.paneldecontrolreposteria.model.Producto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProductoViewModel : ViewModel() {
    private val repo = ProductoRepository()
    private val _productos = MutableStateFlow<List<Producto>>(emptyList())
    val productos: StateFlow<List<Producto>> get() = _productos

    init {
        cargarProductos()
    }

    fun cargarProductos() {
        viewModelScope.launch {
            _productos.value = repo.obtenerProductos()
        }
    }

    fun agregarProducto(producto: Producto) {
        viewModelScope.launch {
            repo.agregarProducto(producto)
            cargarProductos()
        }
    }

    fun eliminarProducto(id: String) {
        viewModelScope.launch {
            repo.eliminarProducto(id)
            cargarProductos()
        }
    }

    fun actualizarProducto(producto: Producto) {
        viewModelScope.launch {
            try {
                repo.actualizarProducto(producto)
                repo.obtenerProductos()
            } catch (e: Exception) {
                Log.e("ProductoViewModel", "Error al actualizar producto", e)
            }
        }
    }

}