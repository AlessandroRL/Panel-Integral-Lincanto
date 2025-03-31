package com.example.paneldecontrolreposteria.model

import java.util.Date

data class Pedido(
    var id: String = "",
    val cliente: String = "",
    val fecha: Date = Date(), // Fecha del pedido
    val productos: List<String> = listOf(),
    val cantidad: Int = 1,
    var estado: String = "Pendiente"
)
