package com.example.paneldecontrolreposteria.viewmodel

import com.google.firebase.firestore.FirebaseFirestore
import com.example.paneldecontrolreposteria.model.Producto
import kotlinx.coroutines.tasks.await
import android.util.Log

class ProductoRepository {
    private val db = FirebaseFirestore.getInstance()
    private val productosRef = db.collection("productos")

    suspend fun obtenerProductos(): List<Producto> {
        return try {
            productosRef.get().await().documents.mapNotNull { doc ->
                val nombre = doc.getString("nombre") ?: return@mapNotNull null

                val ingredientes = (doc.get("ingredientes") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()

                val preparacion = doc.getString("preparacion")

                val utensilios = when (val utensiliosData = doc.get("utensilios")) {
                    is List<*> -> utensiliosData.mapNotNull { it?.toString() }
                    is String -> utensiliosData.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                    else -> null
                }

                val tips = doc.getString("tips")
                    ?: doc.getString("notas adicionales")
                    ?: doc.getString("nota")
                    ?: doc.getString("notas")

                Producto(
                    id = doc.id,
                    nombre = nombre,
                    ingredientes = ingredientes,
                    preparacion = preparacion,
                    utensilios = utensilios,
                    tips = tips
                )
            }
        } catch (e: Exception) {
            Log.e("ProductoRepository", "Error al obtener productos", e)
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
            productosRef.document(producto.nombre).set(producto).await()
        } catch (e: Exception) {
            Log.e("ProductoRepository", "Error al actualizar producto", e)
            throw e
        }
    }
}