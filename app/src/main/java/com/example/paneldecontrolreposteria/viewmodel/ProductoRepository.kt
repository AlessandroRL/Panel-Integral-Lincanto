package com.example.paneldecontrolreposteria.viewmodel

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.example.paneldecontrolreposteria.model.Producto
import kotlinx.coroutines.tasks.await

class ProductoRepository {

    private val db = FirebaseFirestore.getInstance()
    private val productosRef = db.collection("productos")

    suspend fun agregarProducto(producto: Producto) {
        productosRef.document(producto.id).set(producto).await()
    }

    suspend fun obtenerProductos(): List<Producto> {
        val snapshot = productosRef.get().await()
        return snapshot.documents.mapNotNull { it.toObject<Producto>()?.copy(id = it.id) }
    }

    suspend fun eliminarProducto(id: String) {
        productosRef.document(id).delete().await()
    }

    suspend fun actualizarProducto(producto: Producto) {
        productosRef.document(producto.id).set(producto).await()
    }
}