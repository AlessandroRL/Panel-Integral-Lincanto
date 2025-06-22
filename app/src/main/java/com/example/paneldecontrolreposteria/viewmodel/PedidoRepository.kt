package com.example.paneldecontrolreposteria.viewmodel

import android.util.Log
import com.example.paneldecontrolreposteria.model.Pedido
import com.example.paneldecontrolreposteria.model.Producto
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObjects
import kotlinx.coroutines.tasks.await

class PedidoRepository {
    private val db = FirebaseFirestore.getInstance()

    suspend fun agregarPedido(pedido: Pedido) {
        try {
            db.collection("pedidos").add(pedido).await()
        } catch (e: Exception) {
            Log.e("PedidoRepository", "Error al agregar pedido: ${e.message}")
        }
    }

    suspend fun editarPedido(pedido: Pedido): Boolean {
        return try {
            val datosActualizados = mapOf(
                "cliente" to pedido.cliente,
                "productos" to pedido.productos.map { producto ->
                    mapOf(
                        "nombre" to producto.nombre,
                        "tamano" to producto.tamano,
                        "cantidad" to producto.cantidad
                    )
                },
                "estado" to pedido.estado,
                "fechaLimite" to pedido.fechaLimite,
                "notificaciones" to pedido.notificaciones
            )

            db.collection("pedidos")
                .document(pedido.id)
                .update(datosActualizados)
                .await()

            true
        } catch (e: Exception) {
            Log.e("PedidoRepository", "Error al editar pedido: ${e.message}")
            false
        }
    }

    suspend fun eliminarPedido(id: String) {
        try {
            db.collection("pedidos").document(id).delete().await()
        } catch (e: Exception) {
            Log.e("PedidoRepository", "Error al eliminar pedido: ${e.message}")
        }
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