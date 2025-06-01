package com.example.paneldecontrolreposteria.ui.ai

import com.example.paneldecontrolreposteria.model.Ingrediente
import com.example.paneldecontrolreposteria.model.Pedido
import com.example.paneldecontrolreposteria.model.Producto
import com.example.paneldecontrolreposteria.model.ProductoPedido
import com.example.paneldecontrolreposteria.viewmodel.PedidoViewModel
import com.example.paneldecontrolreposteria.viewmodel.IngredienteViewModel
import com.example.paneldecontrolreposteria.viewmodel.ProductoViewModel
import com.example.paneldecontrolreposteria.viewmodel.ProductoCostoViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

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
                val productos = extraerListaProductos(texto, "productos")
                val fechaLimite = extraerFecha(texto)

                Comando.AgregarPedido(cliente, productos, fechaLimite)
            }

            "editar pedido" in lower -> {
                val cliente = extraerDato(texto, "cliente") ?: return Comando.ComandoNoReconocido
                val nuevosProductos = extraerListaProductos(texto, "productos")
                val nuevoEstado = extraerDato(texto, "estado")
                val nuevaFecha = extraerFecha(texto)

                Comando.EditarPedido(cliente, nuevosProductos, nuevoEstado, nuevaFecha)
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
                val pedido = Pedido(
                    cliente = comando.cliente,
                    productos = comando.productos,
                    fechaLimite = comando.fechaLimite ?: ""
                )
                pedidoViewModel.agregarPedido(pedido)
                "Pedido agregado correctamente para el cliente ${comando.cliente}."
            }

            is Comando.EditarPedido -> {
                val pedidos = pedidoViewModel.pedidos.value
                val pedido = pedidos.find { it.cliente.equals(comando.cliente, ignoreCase = true) }
                    ?: return "No se encontró el pedido del cliente ${comando.cliente}."

                val productosActualizados = if (comando.nuevosProductos.isNotEmpty()) {
                    pedido.productos + comando.nuevosProductos
                } else {
                    pedido.productos
                }

                val nuevo = pedido.copy(
                    productos = productosActualizados,
                    fechaLimite = comando.nuevaFechaLimite ?: pedido.fechaLimite,
                    estado = comando.nuevoEstado ?: pedido.estado
                )

                pedidoViewModel.editarPedido(nuevo) {}
                "Pedido del cliente ${comando.cliente} actualizado correctamente."
            }

            is Comando.EliminarPedido -> {
                val pedidos = pedidoViewModel.pedidos.value
                val pedido = pedidos.find { it.cliente.equals(comando.cliente, ignoreCase = true) }
                    ?: return "No se encontró el pedido del cliente ${comando.cliente}."
                pedidoViewModel.eliminarPedido(pedido.id)
                "Pedido del cliente ${comando.cliente} eliminado correctamente."
            }

            is Comando.AgregarIngrediente -> {
                val ingrediente = Ingrediente(comando.nombre, comando.unidad,
                    comando.costoUnidad.toString()
                )
                ingredienteViewModel.agregarIngrediente(ingrediente)
                "Ingrediente agregado."
            }

            is Comando.EditarIngrediente -> {
                val actual = ingredienteViewModel.ingredientes.value.find {
                    it.nombre.equals(comando.nombre, ignoreCase = true)
                } ?: return "No se encontró el ingrediente."
                val nuevo = actual.copy(unidad = comando.unidad, costoUnidad = comando.costoUnidad)
                ingredienteViewModel.editarIngrediente(nuevo)
                "Ingrediente editado."
            }

            is Comando.EliminarIngrediente -> {
                val encontrado = ingredienteViewModel.ingredientes.value.find {
                    it.nombre.equals(comando.nombre, ignoreCase = true)
                } ?: return "No se encontró el ingrediente."
                ingredienteViewModel.eliminarIngrediente(encontrado.id)
                "Ingrediente eliminado."
            }

            is Comando.AgregarProducto -> {
                val producto = Producto(comando.nombre)
                productoViewModel.agregarProducto(producto)
                "Producto agregado."
            }

            is Comando.EliminarProducto -> {
                val encontrado = productoViewModel.productos.value.find {
                    it.nombre.equals(comando.nombre, ignoreCase = true)
                } ?: return "No se encontró el producto."
                productoViewModel.eliminarProducto(encontrado.id)
                "Producto eliminado."
            }

            is Comando.AgregarProductoCostoDesdePlantilla -> {
                val base = productoCostoViewModel.productosBase.value.find {
                    it.nombre.equals(comando.nombreProductoBase, ignoreCase = true)
                } ?: return "No se encontró la plantilla del producto."
                val nuevo = productoCostoViewModel.crearProductoCostoDesdePlantilla(base)
                productoCostoViewModel.guardarProductoCosto(nuevo)
                "Producto de costos creado desde plantilla."
            }

            is Comando.EliminarProductoCosto -> {
                productoCostoViewModel.eliminarProductoCosto(comando.nombre)
                "Producto de costos eliminado."
            }

            is Comando.ConsultarCostoProducto -> {
                val producto = productoCostoViewModel.productosCosto.value.find {
                    it.nombre.equals(comando.nombre, ignoreCase = true)
                } ?: return "No se encontró el producto de costos."
                "El costo total de ${producto.nombre} es ${"%.2f".format(producto.costoTotal)}"
            }

            Comando.ComandoNoReconocido -> "No entendí la instrucción. ¿Podrías reformularla?"
        }
    }

    fun extraerNumero(texto: String, campo: String): Int? {
        val regex = Regex("$campo\\s*(\\d+)", RegexOption.IGNORE_CASE)
        return regex.find(texto)?.groupValues?.getOrNull(1)?.toIntOrNull()
    }

    fun extraerFecha(texto: String): String? {
        return when {
            "mañana" in texto -> calcularFecha(1)
            "pasado mañana" in texto -> calcularFecha(2)
            else -> null
        }
    }

    fun calcularFecha(diasFuturos: Int): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, diasFuturos)
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return sdf.format(calendar.time)
    }

    private fun extraerDato(texto: String, clave: String): String? {
        val regex = Regex("$clave:?\\s*([\\w\\sáéíóúñ]+)", RegexOption.IGNORE_CASE)
        return regex.find(texto)?.groupValues?.get(1)?.trim()
    }

    private fun extraerDouble(texto: String, clave: String): Double? {
        val regex = Regex("$clave:?\\s*([\\d.]+)", RegexOption.IGNORE_CASE)
        return regex.find(texto)?.groupValues?.get(1)?.toDoubleOrNull()
    }

    private fun extraerListaProductos(texto: String, clave: String): List<ProductoPedido> {
        val regex = Regex("$clave:?\\s*([\\w\\s,áéíóúñ]+)", RegexOption.IGNORE_CASE)
        return regex.find(texto)?.groupValues?.get(1)?.split(",")?.map {
            ProductoPedido(nombre = it.trim(), cantidad = 1, tamano = 1)
        } ?: emptyList()
    }
}