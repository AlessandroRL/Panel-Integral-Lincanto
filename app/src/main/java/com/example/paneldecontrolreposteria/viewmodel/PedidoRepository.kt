package com.example.paneldecontrolreposteria.viewmodel

import android.util.Log
import com.example.paneldecontrolreposteria.model.Pedido
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObjects
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PedidoRepository {
    private val db = FirebaseFirestore.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
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

    suspend fun actualizarEstadoPedido(id: String, nuevoEstado: String): Boolean {
        return try {
            Log.d("PedidoRepository", "Actualizando pedido en Firestore: ID=$id, Estado=$nuevoEstado")

            firestore.collection("pedidos")
                .document(id)
                .update("estado", nuevoEstado)
                .await()

            Log.d("PedidoRepository", "Actualización en Firestore exitosa")
            true
        } catch (e: Exception) {
            Log.e("PedidoRepository", "Error Firestore: ${e.message}")
            false
        }
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


