package com.example.paneldecontrolreposteria

data class Producto(
    val id: String = "",
    val nombre: String = "",
    val ingredientes: List<String> = emptyList(),
    val preparacion: String = ""
)
