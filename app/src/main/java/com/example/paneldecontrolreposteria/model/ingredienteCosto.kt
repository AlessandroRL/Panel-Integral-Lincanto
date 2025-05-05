package com.example.paneldecontrolreposteria.model

data class IngredienteCosto(
    val nombre: String = "",
    val unidad: String = "",
    val cantidad: Double = 0.0,
    val costoUnidad: Double = 0.0,
    val costoTotal: Double = 0.0
)