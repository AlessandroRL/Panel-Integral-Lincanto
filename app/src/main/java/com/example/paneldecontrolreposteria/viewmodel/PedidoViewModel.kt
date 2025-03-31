package com.example.paneldecontrolreposteria.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.paneldecontrolreposteria.model.Pedido
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class PedidoViewModel : ViewModel() {
    private val repository = PedidoRepository()
    private val firestore = FirebaseFirestore.getInstance()

    private val _pedidos = MutableStateFlow<List<Pedido>>(emptyList())
    val pedidos: StateFlow<List<Pedido>> = _pedidos

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
                println("Error al agregar pedido: ${e.message}")
            }
        }
    }

    fun actualizarEstadoPedido(id: String, nuevoEstado: String): Boolean {
        if (id.isBlank()) {
            Log.e("PedidoViewModel", "Error: ID del pedido vacío")
            return false
        }

        return try {
            viewModelScope.launch {
                firestore.collection("pedidos").document(id)
                    .update("estado", nuevoEstado)
                    .addOnSuccessListener {
                        Log.d("PedidoViewModel", "Estado actualizado correctamente")
                        obtenerPedidos()
                    }
                    .addOnFailureListener { e ->
                        Log.e("PedidoViewModel", "Error al actualizar el estado del pedido", e)
                    }
            }
            true
        } catch (e: Exception) {
            Log.e("PedidoViewModel", "Excepción al actualizar el estado", e)
            false
        }
    }
}