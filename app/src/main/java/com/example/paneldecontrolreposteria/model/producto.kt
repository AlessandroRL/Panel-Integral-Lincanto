package com.example.paneldecontrolreposteria.model

data class Producto(
    val id: String = "",
    val nombre: String = "",
    val ingredientes: List<List<String>> = emptyList(), // múltiples conjuntos
    val preparacion: String? = null,
    val utensilios: String? = null,
    val tips: String? = null
)

