package com.example.paneldecontrolreposteria

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class PedidoRepository {
    private val db = FirebaseFirestore.getInstance()
    private val pedidosCollection = db.collection("pedidos")

    suspend fun agregarPedido(pedido: Pedido) {
        pedidosCollection.document(pedido.id).set(pedido).await()
    }

    suspend fun obtenerPedidos(): List<Pedido> {
        val snapshot = pedidosCollection.get().await()
        return snapshot.documents.mapNotNull { it.toObject(Pedido::class.java) }
    }

    suspend fun actualizarEstadoPedido(idPedido: String, nuevoEstado: String) {
        pedidosCollection.document(idPedido).update("estado", nuevoEstado).await()
    }
}
