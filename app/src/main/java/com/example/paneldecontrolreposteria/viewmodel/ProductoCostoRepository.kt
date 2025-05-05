package com.example.paneldecontrolreposteria.viewmodel

import com.google.firebase.firestore.FirebaseFirestore
import com.example.paneldecontrolreposteria.model.ProductoCosto
import kotlinx.coroutines.tasks.await
import java.util.Date

class ProductoCostoRepository {

    private val db = FirebaseFirestore.getInstance()
    private val productosCostoRef = db.collection("productosCosto")

    suspend fun agregarProductoCosto(producto: ProductoCosto): Boolean {
        return try {
            productosCostoRef.add(producto.copy(fechaCreacion = Date())).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun obtenerProductosCosto(): List<ProductoCosto> {
        return try {
            val snapshot = productosCostoRef.get().await()
            snapshot.toObjects(ProductoCosto::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun eliminarProductoCosto(id: String): Boolean {
        return try {
            productosCostoRef.document(id).delete().await()
            true
        } catch (e: Exception) {
            false
        }
    }
}