package com.example.paneldecontrolreposteria.model

data class Producto(
    val id: String = "",
    val nombre: String = "",
    val ingredientes: List<IngredienteDetalle> = emptyList(),
    val preparacion: String? = null,
    val utensilios: List<String>? = null,
    val tips: String? = null
)