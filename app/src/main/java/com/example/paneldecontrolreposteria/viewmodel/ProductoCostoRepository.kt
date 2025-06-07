package com.example.paneldecontrolreposteria.viewmodel

import com.example.paneldecontrolreposteria.model.Ingrediente
import com.example.paneldecontrolreposteria.model.IngredienteCosto
import com.example.paneldecontrolreposteria.model.Producto
import com.example.paneldecontrolreposteria.model.ProductoCosto
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.Date
import java.util.UUID

class ProductoCostoRepository(private val db: FirebaseFirestore) {

    suspend fun obtenerProductosBase(): List<Producto> {
        val snapshot = db.collection("productos").get().await()
        return snapshot.documents.mapNotNull { it.toObject(Producto::class.java) }
    }

    suspend fun obtenerIngredientesBase(): List<Ingrediente> {
        val snapshot = db.collection("ingredientes").get().await()
        return snapshot.documents.mapNotNull { it.toObject(Ingrediente::class.java) }
    }

    suspend fun crearProductoCostoDesdePlantilla(producto: Producto): ProductoCosto {
        val ingredientesBase = obtenerIngredientesBase()
        val ingredientesCosto = mutableMapOf<String, IngredienteCosto>()

        for (detalle in producto.ingredientes) {
            val ingredienteDB = ingredientesBase.firstOrNull { it.nombre.equals(detalle.nombre, ignoreCase = true) }

            if (ingredienteDB != null && detalle.cantidad > 0) {
                val costoUnidad = ingredienteDB.costoUnidad
                val costoTotal = costoUnidad * detalle.cantidad

                ingredientesCosto[UUID.randomUUID().toString()] = IngredienteCosto(
                    nombre = detalle.nombre,
                    unidad = detalle.unidad.ifBlank { ingredienteDB.unidad },
                    cantidad = detalle.cantidad,
                    costoUnidad = costoUnidad,
                    costoTotal = costoTotal
                )
            }
        }

        val costoTotalProducto = ingredientesCosto.values.sumOf { it.costoTotal }

        return ProductoCosto(
            nombre = producto.nombre,
            fechaCreacion = Date(),
            ingredientes = ingredientesCosto,
            costoTotal = costoTotalProducto
        )
    }

    suspend fun guardarProductoCosto(productoCosto: ProductoCosto) {
        db.collection("gestionCostos")
            .document(productoCosto.nombre)
            .set(productoCosto)
            .await()
    }

    suspend fun obtenerProductosCosto(): List<ProductoCosto> {
        val snapshot = db.collection("gestionCostos").get().await()
        return snapshot.documents.mapNotNull { it.toObject(ProductoCosto::class.java) }
    }

    suspend fun eliminarProductoCosto(nombre: String) {
        db.collection("gestionCostos").document(nombre).delete().await()
    }

    suspend fun actualizarProductoCosto(productoCosto: ProductoCosto) {
        db.collection("gestionCostos")
            .document(productoCosto.nombre)
            .set(productoCosto)
            .await()
    }
}