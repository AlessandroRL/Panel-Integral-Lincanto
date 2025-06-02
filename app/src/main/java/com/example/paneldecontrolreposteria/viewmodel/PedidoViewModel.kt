package com.example.paneldecontrolreposteria.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.paneldecontrolreposteria.model.Pedido
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.google.firebase.firestore.FirebaseFirestore

class PedidoViewModel : ViewModel() {
    private val repository = PedidoRepository()
    private val firestore = FirebaseFirestore.getInstance()

    private val _pedidos = MutableStateFlow<List<Pedido>>(emptyList())
    val pedidos: StateFlow<List<Pedido>> = _pedidos

    private val _productos = MutableStateFlow<List<String>>(emptyList())
    val productos: StateFlow<List<String>> = _productos

    init {
        obtenerPedidos()
    }

    fun obtenerPedidos() {
        firestore.collection("pedidos").get()
            .addOnSuccessListener { result ->
                val listaPedidos = result.documents.mapNotNull { doc ->
                    val pedido = doc.toObject(Pedido::class.java)
                    pedido?.id = doc.id
                    Log.d("PedidoViewModel", "Pedido obtenido: ${pedido?.id}")
                    pedido
                }
                _pedidos.value = listaPedidos
            }
            .addOnFailureListener { e ->
                Log.e("PedidoViewModel", "Error al obtener pedidos", e)
            }
    }

    fun agregarPedido(pedido: Pedido) {
        viewModelScope.launch {
            try {
                repository.agregarPedido(pedido)
                obtenerPedidos()
            } catch (e: Exception) {
                Log.e("PedidoViewModel", "Error al agregar pedido: ${e.message}")
            }
        }
    }

    fun editarPedido(pedido: Pedido, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = repository.editarPedido(pedido)
            if (result) obtenerPedidos()
            onResult(result)
        }
    }

    fun eliminarPedido(pedidoId: String) {
        viewModelScope.launch {
            try {
                repository.eliminarPedido(pedidoId)
                obtenerPedidos()
            } catch (e: Exception) {
                Log.e("PedidoViewModel", "Error al eliminar pedido: ${e.message}")
            }
        }
    }

    fun obtenerNombresProductos(onResultado: (List<String>) -> Unit) {
        viewModelScope.launch {
            try {
                val productos = repository.obtenerProductos()
                val nombres = productos.map { it.nombre }
                onResultado(nombres)
            } catch (e: Exception) {
                Log.e("PedidoViewModel", "Error al obtener nombres de productos: ${e.message}")
                onResultado(emptyList())
            }
        }
    }
}