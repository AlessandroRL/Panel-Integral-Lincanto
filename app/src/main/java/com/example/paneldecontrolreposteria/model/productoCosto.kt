package com.example.paneldecontrolreposteria.model

import java.util.Date

data class ProductoCosto(
    val nombre: String = "",
    val fechaCreacion: Date = Date(),
    val ingredientes: Map<String, IngredienteCosto> = emptyMap(),
    val costoTotal: Double = 0.0
)