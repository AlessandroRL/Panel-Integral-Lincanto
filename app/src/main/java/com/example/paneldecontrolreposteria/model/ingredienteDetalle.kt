package com.example.paneldecontrolreposteria.model

data class IngredienteDetalle(
    val nombre: String = "",
    val unidad: String = "", // gr, ml, unidad
    val cantidad: Double = 0.0,
    val observacion: String? = null // para aclaraciones: "para el ponqué", "equivale a una cucharadita"
)