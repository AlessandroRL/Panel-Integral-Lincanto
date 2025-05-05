package com.example.paneldecontrolreposteria.viewmodel

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.paneldecontrolreposteria.model.Producto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await


class ProductoViewModel : ViewModel() {
    private val repo = ProductoRepository()
    private val _productos = MutableStateFlow<List<Producto>>(emptyList())
    val productos: StateFlow<List<Producto>> get() = _productos
    private val db = FirebaseFirestore.getInstance()

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

    @RequiresApi(Build.VERSION_CODES.O)
    fun actualizarProducto(nombreOriginal: String, productoEditado: Producto, onResult: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            try {
                val productoDoc = db.collection("productos")
                    .whereEqualTo("nombre", nombreOriginal)
                    .get()
                    .await()

                if (productoDoc.isEmpty.not()) {
                    val docSnapshot = productoDoc.documents.first()
                    val docId = docSnapshot.id

                    db.collection("productos").document(docId)
                        .set(productoEditado.copy(id = docId))
                        .await()

                    cargarProductos()

                    onResult(true)
                } else {
                    onResult(false)
                }
            } catch (e: Exception) {
                Log.e("ProductoViewModel", "Error al actualizar producto: ${e.message}")
                onResult(false)
            }
        }
    }
}