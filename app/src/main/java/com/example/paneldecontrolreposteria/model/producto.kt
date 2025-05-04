package com.example.paneldecontrolreposteria.model

data class Producto(
    val id: String = "",
    val nombre: String = "",
    val ingredientes: List<String> = listOf(),
    val preparacion: String? = null,
    val utensilios: List<String>? = null,
    val tips: String? = null
)

