package com.example.paneldecontrolreposteria

data class Pedido(
    val id: String = "",
    val cliente: String = "",
    val producto: String = "",
    val estado: String = "En espera"
)
