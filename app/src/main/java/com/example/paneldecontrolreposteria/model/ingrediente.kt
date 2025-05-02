package com.example.paneldecontrolreposteria.model

data class Ingrediente(
    var id: String = "",
    val nombre: String = "",
    val unidad: String = "",  // gr, ml, unidad, cucharada, etc.
    val costoUnidad: Double = 0.0
)
