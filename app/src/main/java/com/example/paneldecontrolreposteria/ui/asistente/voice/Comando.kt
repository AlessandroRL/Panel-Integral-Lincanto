package com.example.paneldecontrolreposteria.ui.asistente.voice

sealed class Comando {
    object AbrirAgregarPedido : Comando()
    object AbrirSeccionPedidos : Comando()
    object AbrirSeccionIngredientes : Comando()
    object AbrirSeccionCostos : Comando()
    data class EliminarIngrediente(val nombre: String) : Comando()
    data class EditarProducto(val nombre: String) : Comando()
    object NoReconocido : Comando()
}