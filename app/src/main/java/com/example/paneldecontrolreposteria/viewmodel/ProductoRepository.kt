package com.example.paneldecontrolreposteria.viewmodel

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.example.paneldecontrolreposteria.model.Producto
import kotlinx.coroutines.tasks.await
import android.util.Log

class ProductoRepository {
    private val db = FirebaseFirestore.getInstance()
    private val productosRef = db.collection("productos")

    suspend fun obtenerProductos(): List<Producto> {
        return try {
            productosRef.get().await().documents.mapNotNull { doc ->
                doc.toObject<Producto>()?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun agregarProducto(producto: Producto) {
        productosRef.document(producto.nombre).set(producto).await()
    }

    suspend fun eliminarProducto(id: String) {
        productosRef.document(id).delete().await()
    }

    suspend fun actualizarProducto(producto: Producto) {
        try {
            db.collection("productos").document(producto.nombre).set(producto).await()
        } catch (e: Exception) {
            Log.e("ProductoRepository", "Error al actualizar producto", e)
            throw e
        }
    }
}