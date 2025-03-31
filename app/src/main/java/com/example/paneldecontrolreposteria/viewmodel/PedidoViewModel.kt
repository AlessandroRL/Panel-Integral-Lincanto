package com.example.paneldecontrolreposteria.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.paneldecontrolreposteria.model.Pedido
import com.example.paneldecontrolreposteria.viewmodel.PedidoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PedidoViewModel : ViewModel() {
    private val repository = PedidoRepository()

    private val _pedidos = MutableStateFlow<List<Pedido>>(emptyList())
    val pedidos: StateFlow<List<Pedido>> = _pedidos

    init {
        obtenerPedidos()
    }

    fun obtenerPedidos() {
        viewModelScope.launch {
            _pedidos.value = repository.obtenerPedidos()
        }
    }

    fun actualizarEstadoPedido(id: String, nuevoEstado: String) {
        viewModelScope.launch {
            repository.actualizarEstadoPedido(id, nuevoEstado)
            obtenerPedidos()
        }
    }
}

