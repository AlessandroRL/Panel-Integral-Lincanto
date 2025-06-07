package com.example.paneldecontrolreposteria.model

import java.util.Date

data class ProductoPedido(
    val nombre: String = "",
    val tamano: Int = 0,
    val cantidad: Int = 0
)

data class Pedido(
    var id: String = "",
    val cliente: String = "",
    val fechaRegistro: Date = Date(),
    val productos: List<ProductoPedido> = listOf(),
    var estado: String = "Pendiente",
    var fechaLimite: String = ""
)