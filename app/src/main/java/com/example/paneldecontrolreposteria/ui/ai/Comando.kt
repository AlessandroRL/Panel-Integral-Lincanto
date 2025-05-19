package com.example.paneldecontrolreposteria.ui.ai

sealed class Comando {
    data class AgregarPedido(val cliente: String, val productos: List<String>) : Comando()
    data class EditarPedido(val cliente: String, val nuevoProducto: String) : Comando()
    data class EliminarPedido(val cliente: String) : Comando()

    data class AgregarIngrediente(val nombre: String, val unidad: String, val costoUnidad: Double) : Comando()
    data class EditarIngrediente(val nombre: String, val unidad: String, val costoUnidad: Double) : Comando()
    data class EliminarIngrediente(val nombre: String) : Comando()

    data class AgregarProducto(val nombre: String) : Comando()
    data class EliminarProducto(val nombre: String) : Comando()

    data class AgregarProductoCostoDesdePlantilla(val nombreProductoBase: String) : Comando()
    data class EliminarProductoCosto(val nombre: String) : Comando()

    data class ConsultarCostoProducto(val nombre: String) : Comando()
    object ComandoNoReconocido : Comando()
}