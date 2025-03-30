package com.example.paneldecontrolreposteria.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.example.paneldecontrolreposteria.model.Pedido
import kotlinx.coroutines.launch

class PedidoViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    fun agregarPedido(cliente: String, productos: List<String>, cantidad: Int) {
        val pedido = hashMapOf(
            "cliente" to cliente,
            "fecha" to FieldValue.serverTimestamp(), // Asigna la fecha del servidor
            "productos" to productos,
            "cantidad" to cantidad,
            "estado" to "Pendiente"
        )

        viewModelScope.launch {
            db.collection("pedidos")
                .add(pedido)
                .addOnSuccessListener { println("Pedido agregado con éxito") }
                .addOnFailureListener { e -> println("Error: ${e.message}") }
        }
    }
}
