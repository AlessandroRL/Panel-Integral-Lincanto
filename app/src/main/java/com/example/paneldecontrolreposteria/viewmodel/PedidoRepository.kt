package com.example.paneldecontrolreposteria.viewmodel

import com.example.paneldecontrolreposteria.model.Pedido
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObjects
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PedidoRepository {
    private val db = FirebaseFirestore.getInstance()
    private val _pedidos = MutableStateFlow<List<Pedido>>(emptyList())
    val pedidos: StateFlow<List<Pedido>> = _pedidos

    init {
        observarPedidos()
    }

    private fun observarPedidos() {
        db.collection("pedidos").addSnapshotListener { snapshot, error ->
            if (error != null) {
                println("Error obteniendo pedidos: ${error.message}")
                return@addSnapshotListener
            }
            if (snapshot != null) {
                _pedidos.value = snapshot.toObjects()
            }
        }
    }

    suspend fun agregarPedido(pedido: Pedido) {
        db.collection("pedidos").add(pedido).await()
    }

    suspend fun actualizarEstadoPedido(id: String, nuevoEstado: String) {
        db.collection("pedidos").document(id).update("estado", nuevoEstado).await()
    }

    suspend fun obtenerPedidos(): List<Pedido> {
        return try {
            val snapshot = db.collection("pedidos").get().await()
            snapshot.toObjects<Pedido>()
        } catch (e: Exception) {
            emptyList()
        }
    }

}


