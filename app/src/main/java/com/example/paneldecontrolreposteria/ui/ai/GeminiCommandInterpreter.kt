package com.example.paneldecontrolreposteria.ui.ai

import com.example.paneldecontrolreposteria.model.Ingrediente
import com.example.paneldecontrolreposteria.model.Pedido
import com.example.paneldecontrolreposteria.model.Producto
import com.example.paneldecontrolreposteria.viewmodel.PedidoViewModel
import com.example.paneldecontrolreposteria.viewmodel.IngredienteViewModel
import com.example.paneldecontrolreposteria.viewmodel.ProductoViewModel
import com.example.paneldecontrolreposteria.viewmodel.ProductoCostoViewModel

class GeminiCommandInterpreter(
    private val pedidoViewModel: PedidoViewModel,
    private val ingredienteViewModel: IngredienteViewModel,
    private val productoViewModel: ProductoViewModel,
    private val productoCostoViewModel: ProductoCostoViewModel
) {
    fun interpretar(texto: String): Comando {
        val lower = texto.lowercase()

        return when {
            "agregar pedido" in lower -> {
                val cliente = extraerDato(texto, "cliente") ?: return Comando.ComandoNoReconocido
                val productos = extraerLista(texto, "productos")
                Comando.AgregarPedido(cliente, productos)
            }
            "editar pedido" in lower -> {
                val cliente = extraerDato(texto, "cliente") ?: return Comando.ComandoNoReconocido
                val nuevoProducto = extraerDato(texto, "producto") ?: return Comando.ComandoNoReconocido
                Comando.EditarPedido(cliente, nuevoProducto)
            }
            "eliminar pedido" in lower -> {
                val cliente = extraerDato(texto, "cliente") ?: return Comando.ComandoNoReconocido
                Comando.EliminarPedido(cliente)
            }

            "agregar ingrediente" in lower -> {
                val nombre = extraerDato(texto, "ingrediente") ?: return Comando.ComandoNoReconocido
                val unidad = extraerDato(texto, "unidad") ?: "unidad"
                val costo = extraerDouble(texto, "costo") ?: return Comando.ComandoNoReconocido
                Comando.AgregarIngrediente(nombre, unidad, costo)
            }
            "editar ingrediente" in lower -> {
                val nombre = extraerDato(texto, "ingrediente") ?: return Comando.ComandoNoReconocido
                val unidad = extraerDato(texto, "unidad") ?: "unidad"
                val costo = extraerDouble(texto, "costo") ?: return Comando.ComandoNoReconocido
                Comando.EditarIngrediente(nombre, unidad, costo)
            }
            "eliminar ingrediente" in lower -> {
                val nombre = extraerDato(texto, "ingrediente") ?: return Comando.ComandoNoReconocido
                Comando.EliminarIngrediente(nombre)
            }

            "agregar producto costo desde plantilla" in lower -> {
                val nombre = extraerDato(texto, "producto") ?: return Comando.ComandoNoReconocido
                Comando.AgregarProductoCostoDesdePlantilla(nombre)
            }
            "eliminar producto costo" in lower -> {
                val nombre = extraerDato(texto, "producto") ?: return Comando.ComandoNoReconocido
                Comando.EliminarProductoCosto(nombre)
            }
            "consultar costo" in lower || "cuánto cuesta" in lower -> {
                val nombre = extraerDato(texto, "producto") ?: return Comando.ComandoNoReconocido
                Comando.ConsultarCostoProducto(nombre)
            }

            "agregar producto" in lower -> {
                val nombre = extraerDato(texto, "producto") ?: return Comando.ComandoNoReconocido
                Comando.AgregarProducto(nombre)
            }
            "eliminar producto" in lower -> {
                val nombre = extraerDato(texto, "producto") ?: return Comando.ComandoNoReconocido
                Comando.EliminarProducto(nombre)
            }

            else -> Comando.ComandoNoReconocido
        }
    }

    suspend fun ejecutar(comando: Comando): String {
        return when (comando) {
            is Comando.AgregarPedido -> {
                val pedido = Pedido(cliente = comando.cliente, productos = comando.productos)
                pedidoViewModel.agregarPedido(pedido)
                "Pedido agregado correctamente."
            }
            is Comando.EditarPedido -> {
                val pedidos = pedidoViewModel.pedidos.value
                val pedido = pedidos.find { it.cliente.equals(comando.cliente, ignoreCase = true) }
                    ?: return "No se encontró el pedido del cliente ${comando.cliente}"
                val nuevo = pedido.copy(productos = pedido.productos + comando.nuevoProducto)
                pedidoViewModel.editarPedido(nuevo) { resultado ->
                    if (resultado) {
                        println("Pedido editado correctamente.")
                    } else {
                        println("Error al editar el pedido.")
                    }
                }
                "Pedido actualizado."
            }
            is Comando.EliminarPedido -> {
                val pedidos = pedidoViewModel.pedidos.value
                val pedido = pedidos.find { it.cliente.equals(comando.cliente, ignoreCase = true) }
                    ?: return "No se encontró el pedido del cliente ${comando.cliente}"
                pedidoViewModel.eliminarPedido(pedido.id)
                "Pedido eliminado correctamente."
            }

            is Comando.AgregarIngrediente -> {
                val ingrediente = Ingrediente(nombre = comando.nombre, unidad = comando.unidad, costoUnidad = comando.costoUnidad)
                ingredienteViewModel.agregarIngrediente(ingrediente)
                "Ingrediente agregado."
            }
            is Comando.EditarIngrediente -> {
                val ingredientes = ingredienteViewModel.ingredientes.value
                val actual = ingredientes.find { it.nombre.equals(comando.nombre, ignoreCase = true) }
                    ?: return "No se encontró el ingrediente."
                val nuevo = actual.copy(unidad = comando.unidad, costoUnidad = comando.costoUnidad)
                ingredienteViewModel.editarIngrediente(nuevo)
                "Ingrediente editado."
            }
            is Comando.EliminarIngrediente -> {
                val ingredientes = ingredienteViewModel.ingredientes.value
                val encontrado = ingredientes.find { it.nombre.equals(comando.nombre, ignoreCase = true) }
                    ?: return "No se encontró el ingrediente."
                ingredienteViewModel.eliminarIngrediente(encontrado.id)
                "Ingrediente eliminado."
            }

            is Comando.AgregarProducto -> {
                val producto = Producto(nombre = comando.nombre)
                productoViewModel.agregarProducto(producto)
                "Producto agregado."
            }
            is Comando.EliminarProducto -> {
                val productos = productoViewModel.productos.value
                val encontrado = productos.find { it.nombre.equals(comando.nombre, ignoreCase = true) }
                    ?: return "No se encontró el producto."
                productoViewModel.eliminarProducto(encontrado.id)
                "Producto eliminado."
            }

            is Comando.AgregarProductoCostoDesdePlantilla -> {
                val productoBase = productoCostoViewModel.productosBase.value
                    .find { it.nombre.equals(comando.nombreProductoBase, ignoreCase = true) }
                    ?: return "No se encontró la plantilla del producto."
                val nuevo = productoCostoViewModel.crearProductoCostoDesdePlantilla(productoBase)
                productoCostoViewModel.guardarProductoCosto(nuevo)
                "Producto de costos creado desde plantilla."
            }
            is Comando.EliminarProductoCosto -> {
                productoCostoViewModel.eliminarProductoCosto(comando.nombre)
                "Producto de costos eliminado."
            }
            is Comando.ConsultarCostoProducto -> {
                val producto = productoCostoViewModel.productosCosto.value
                    .find { it.nombre.equals(comando.nombre, ignoreCase = true) }
                    ?: return "No se encontró el producto de costos."
                "El costo total de ${producto.nombre} es ${"%.2f".format(producto.costoTotal)}"
            }

            Comando.ComandoNoReconocido -> "Lo siento, no entendí el comando."
        }
    }

    private fun extraerDato(texto: String, clave: String): String? {
        val regex = Regex("$clave:?\\s*([\\w\\sáéíóúñ]+)", RegexOption.IGNORE_CASE)
        return regex.find(texto)?.groupValues?.get(1)?.trim()
    }

    private fun extraerDouble(texto: String, clave: String): Double? {
        val regex = Regex("$clave:?\\s*([\\d.]+)", RegexOption.IGNORE_CASE)
        return regex.find(texto)?.groupValues?.get(1)?.toDoubleOrNull()
    }

    private fun extraerLista(texto: String, clave: String): List<String> {
        val regex = Regex("$clave:?\\s*([\\w\\s,áéíóúñ]+)", RegexOption.IGNORE_CASE)
        val valores = regex.find(texto)?.groupValues?.get(1)?.split(",") ?: return emptyList()
        return valores.map { it.trim() }.filter { it.isNotBlank() }
    }
}