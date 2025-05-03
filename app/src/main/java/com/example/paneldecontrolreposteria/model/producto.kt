package com.example.paneldecontrolreposteria.model

data class Producto(
    val id: String = "",
    val nombre: String = "",
    val ingredientes: List<List<IngredienteProducto>> = emptyList(),
    val preparacion: String? = null,
    val utensilios: String? = null,
    val tips: String? = null
)

data class IngredienteProducto(
    val nombre: String = "",
    val cantidad: Double = 0.0,
    val unidad: String = "" // gr, ml, unidad, etc.
)
