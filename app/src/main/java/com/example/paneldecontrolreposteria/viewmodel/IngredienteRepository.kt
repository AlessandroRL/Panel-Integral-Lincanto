package com.example.paneldecontrolreposteria.viewmodel

import android.util.Log
import com.example.paneldecontrolreposteria.model.Ingrediente
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class IngredienteRepository {
    private val db = FirebaseFirestore.getInstance()
    private val collection = db.collection("ingredientes")

    suspend fun obtenerIngredientes(): List<Ingrediente> {
        return try {
            val snapshot = collection.get().await()
            snapshot.documents.mapNotNull { doc ->
                val ingrediente = doc.toObject(Ingrediente::class.java)
                ingrediente?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            Log.e("IngredienteRepo", "Error al obtener ingredientes", e)
            emptyList()
        }
    }

    suspend fun agregarIngrediente(ingrediente: Ingrediente) {
        collection.add(ingrediente).await()
    }

    suspend fun actualizarIngrediente(ingrediente: Ingrediente) {
        if (ingrediente.id.isNotBlank()) {
            collection.document(ingrediente.id).set(ingrediente).await()
        }
    }

    suspend fun eliminarIngrediente(id: String) {
        collection.document(id).delete().await()
    }
}
