package com.example.paneldecontrolreposteria.viewmodel

import android.util.Log
import com.example.paneldecontrolreposteria.Producto
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

    suspend fun obtenerProductos(): List<Producto> {
        return try {
            val snapshot = db.collection("productos").get().await()
            snapshot.toObjects<Producto>()
        } catch (e: Exception) {
            Log.e("PedidoRepository", "Error al obtener productos: ${e.message}")
            emptyList()
        }
    }
}