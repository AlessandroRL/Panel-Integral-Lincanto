package com.example.paneldecontrolreposteria.model

data class Producto(
    val id: String = "",
    val nombre: String = "",
    val ingredientes: List<String> = emptyList(),
    val preparacion: String = ""
)
