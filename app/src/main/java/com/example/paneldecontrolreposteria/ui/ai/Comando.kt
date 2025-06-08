package com.example.paneldecontrolreposteria.ui.ai

import com.example.paneldecontrolreposteria.model.IngredienteDetalle
import com.example.paneldecontrolreposteria.model.ProductoPedido
import java.time.LocalDate

sealed class Comando {
    data class AgregarPedido(val cliente: String, val productos: List<ProductoPedido>, val fechaLimite: String) : Comando()
    data class EditarPedido(
        val cliente: String,
        val productos: List<ProductoPedido>?,
        val fechaLimite: String?
    ) : Comando()
    data class EliminarPedido(val cliente: String) : Comando()

    data class AgregarIngrediente(val nombre: String, val unidad: String, val costoUnidad: Double): Comando()
    data class EditarIngrediente(
        val nombre: String,
        val unidad: String?,
        val costoUnidad: Double?
    ) : Comando()
    data class EliminarIngrediente(val nombre: String): Comando()

    data class AgregarProducto(
        val nombre: String,
        val ingredientes: List<IngredienteDetalle>?,
        val preparacion: String? = null,
        val utensilios: List<String>?,
        val tips: String? = null
    ) : Comando()
    data class EditarProducto(
        val nombre: String,
        val ingredientes: List<IngredienteDetalle>?,
        val preparacion: String?,
        val utensilios: List<String>?,
        val tips: String?
    ) : Comando()
    data class EliminarProducto(val nombre: String) : Comando()

    data class ConsultarPedidos(
        val tipo: String,
        val mesNombre: String = "",
        val anio: Int = LocalDate.now().year
    ) : Comando()
    data class ConsultarPedidoPorCliente(val cliente: String) : Comando()
    data class ConsultarIngredientesTotales(val dummy: Boolean = true) : Comando()
    data class ConsultarSiIngredienteExiste(val nombre: String) : Comando()
    data class ConsultarListaIngredientes(val dummy: Boolean = true) : Comando()
    data class ConsultarListaProductos(val dummy: Boolean = true) : Comando()
    data class ConsultarInfoProducto(val nombre: String, val campo: String = "todo") : Comando()

    data class ConsultarComando(val consulta: String) : Comando()

    data class RespuestaSimple(val respuesta: String) : Comando()
    object ComandoNoReconocido : Comando()
}