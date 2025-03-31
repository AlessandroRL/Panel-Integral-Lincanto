package com.example.paneldecontrolreposteria

import java.util.Date


data class Pedido(
    val id: String = "",
    val cliente: String = "",
    val productos: List<String> = listOf(),
    val cantidad: Int = 1,
    val estado: String = "Pendiente",
    val fecha: Date = Date()
)
