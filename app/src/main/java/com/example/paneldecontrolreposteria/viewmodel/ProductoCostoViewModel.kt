package com.example.paneldecontrolreposteria.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.example.paneldecontrolreposteria.model.Ingrediente
import com.example.paneldecontrolreposteria.model.ProductoCosto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProductoCostoViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    private val _ingredientes = MutableStateFlow<List<Ingrediente>>(emptyList())
    val ingredientes: StateFlow<List<Ingrediente>> = _ingredientes

    private val _productosCosto = MutableStateFlow<List<ProductoCosto>>(emptyList())
    val productosCosto: StateFlow<List<ProductoCosto>> = _productosCosto

    init {
        obtenerIngredientes()
        obtenerProductosCosto()
    }

    fun obtenerIngredientes() {
        db.collection("ingredientes")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("CostosViewModel", "Error al obtener ingredientes", e)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val listaIngredientes = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Ingrediente::class.java)
                    }
                    _ingredientes.value = listaIngredientes
                }
            }
    }

    fun obtenerProductosCosto() {
        db.collection("productosCosto")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("CostosViewModel", "Error al obtener productosCosto", e)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val listaProductos = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(ProductoCosto::class.java)
                    }
                    _productosCosto.value = listaProductos
                }
            }
    }

    fun agregarProductoCosto(productoCosto: ProductoCosto, onResult: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            try {
                db.collection("productosCosto")
                    .add(productoCosto)
                    .await()
                onResult(true)
            } catch (e: Exception) {
                Log.e("CostosViewModel", "Error al agregar productoCosto: ${e.message}")
                onResult(false)
            }
        }
    }

    fun cargarProductosCosto() {
        viewModelScope.launch {
            try {
                val snapshot = db.collection("productosCosto").get().await()
                val productos = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(ProductoCosto::class.java)
                }
                _productosCosto.value = productos
            } catch (e: Exception) {
                Log.e("ProductoCostoViewModel", "Error al cargar productos de costo: ${e.message}")
            }
        }
    }
}
