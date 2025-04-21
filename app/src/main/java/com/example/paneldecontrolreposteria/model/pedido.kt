package com.example.paneldecontrolreposteria.model

import java.util.Date

data class Pedido(
    var id: String = "",
    val cliente: String = "",
    val fechaRegistro: Date = Date(),
    val productos: List<String> = listOf(),
    val cantidad: Int = 1,
    var estado: String = "Pendiente",
    var fechaLimite: String = "",
    var tamano: Int = 1
)
