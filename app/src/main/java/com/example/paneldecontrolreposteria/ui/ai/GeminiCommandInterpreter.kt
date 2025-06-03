package com.example.paneldecontrolreposteria.ui.ai

import android.util.Log
import com.example.paneldecontrolreposteria.model.Ingrediente
import com.example.paneldecontrolreposteria.model.Pedido
import com.example.paneldecontrolreposteria.model.Producto
import com.example.paneldecontrolreposteria.model.ProductoPedido
import com.example.paneldecontrolreposteria.viewmodel.PedidoViewModel
import com.example.paneldecontrolreposteria.viewmodel.IngredienteViewModel
import com.example.paneldecontrolreposteria.viewmodel.ProductoViewModel
import com.example.paneldecontrolreposteria.viewmodel.ProductoCostoViewModel
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class GeminiCommandInterpreter(
    private val pedidoViewModel: PedidoViewModel,
    private val ingredienteViewModel: IngredienteViewModel,
    private val productoViewModel: ProductoViewModel,
    private val productoCostoViewModel: ProductoCostoViewModel
) {

    fun interpretar(respuestaGemini: String): Comando {
        return try {
            val limpio = respuestaGemini
                .replace(Regex("```json\\s*"), "")
                .replace(Regex("```\\s*"), "")
                .trim()

            Log.d("GeminiInterpreter", "JSON limpio:\n$limpio")

            val json = JSONObject(limpio)
            val tipo = json.optString("tipo", "pedido").lowercase()

            return when (tipo) {
                "ingrediente" -> interpretarIngrediente(json)
                "pedido" -> interpretarPedido(json)
                else -> {
                    Log.w("GeminiInterpreter", "Tipo no reconocido: $tipo")
                    Comando.ComandoNoReconocido
                }
            }

        } catch (e: Exception) {
            Log.e("GeminiInterpreter", "Error interpretando JSON: ${e.message}", e)
            Log.d("GeminiInterpreter", "Respuesta original:\n$respuestaGemini")
            Comando.ComandoNoReconocido
        }
    }

    private fun interpretarPedido(json: JSONObject): Comando {
        val intencion = json.getString("intencion").lowercase()
        val cliente = json.getString("cliente")

        return when (intencion) {
            "agregar" -> {
                val fechaLimite = json.optString("fechaLimite", "").trim()
                if (!fechaLimite.matches(Regex("""\d{4}-\d{2}-\d{2}"""))) {
                    Log.w("GeminiInterpreter", "Formato de fecha inválido o ausente: $fechaLimite")
                    return Comando.ComandoNoReconocido
                }

                val productos = leerProductos(json)
                Comando.AgregarPedido(cliente, productos, fechaLimite)
            }

            "editar" -> {
                val fechaLimite = if (json.has("fechaLimite")) {
                    val fecha = json.getString("fechaLimite").trim()
                    if (!fecha.matches(Regex("""\d{4}-\d{2}-\d{2}"""))) {
                        Log.w("GeminiInterpreter", "Formato de fecha inválido: $fecha")
                        return Comando.ComandoNoReconocido
                    }
                    fecha
                } else null

                val productos = if (json.has("productos")) leerProductos(json) else null

                Comando.EditarPedido(cliente, productos, fechaLimite)
            }

            "eliminar" -> Comando.EliminarPedido(cliente)

            else -> {
                Log.w("GeminiInterpreter", "Intención no reconocida en pedidos: $intencion")
                Comando.ComandoNoReconocido
            }
        }
    }

    private fun interpretarIngrediente(json: JSONObject): Comando {
        val intencion = json.getString("intencion").lowercase()
        val nombre = json.getString("nombre")

        return when (intencion) {
            "agregar" -> {
                val unidad = json.optString("unidad", "").trim()
                val costoUnidad = json.optDouble("costoUnidad", -1.0)
                if (unidad.isEmpty() || costoUnidad < 0) {
                    Log.w("GeminiInterpreter", "Unidad o costo por unidad inválidos al agregar")
                    Comando.ComandoNoReconocido
                } else {
                    Comando.AgregarIngrediente(nombre, unidad, costoUnidad)
                }
            }

            "editar" -> {
                val unidad: String? =
                    if (json.has("unidad") && !json.isNull("unidad")) json.getString("unidad") else null

                val costoUnidad: Double? =
                    if (json.has("costoUnidad") && !json.isNull("costoUnidad")) json.getDouble("costoUnidad") else null

                Log.d("GeminiInterpreter", "Editar ingrediente: nombre=$nombre, unidad=$unidad, costoUnidad=$costoUnidad")

                Comando.EditarIngrediente(nombre, unidad, costoUnidad)
            }

            "eliminar" -> Comando.EliminarIngrediente(nombre)

            else -> Comando.ComandoNoReconocido
        }
    }

    private fun leerProductos(json: JSONObject): List<ProductoPedido> {
        val productos = mutableListOf<ProductoPedido>()
        try {
            val productosJson = json.getJSONArray("productos")
            for (i in 0 until productosJson.length()) {
                try {
                    val p = productosJson.getJSONObject(i)
                    val nombre = p.getString("nombre")
                    val tamano = try { p.getInt("tamano") } catch (e: Exception) { p.getDouble("tamano").toInt() }
                    val cantidad = try { p.getInt("cantidad") } catch (e: Exception) { p.getDouble("cantidad").toInt() }

                    productos.add(ProductoPedido(nombre, tamano, cantidad))
                } catch (e: Exception) {
                    Log.e("GeminiInterpreter", "Error en producto[$i]: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Log.e("GeminiInterpreter", "Error leyendo lista de productos: ${e.message}")
        }
        return productos
    }

    fun ejecutar(comando: Comando): String {
        return when (comando) {
            is Comando.AgregarPedido -> {
                val pedido = Pedido(
                    cliente = comando.cliente,
                    productos = comando.productos,
                    fechaLimite = comando.fechaLimite
                )
                pedidoViewModel.agregarPedido(pedido)
                "Pedido agregado correctamente para ${comando.cliente}."
            }

            is Comando.EditarPedido -> {
                val pedidoExistente = pedidoViewModel.pedidos.value
                    ?.find { it.cliente.equals(comando.cliente, ignoreCase = true) }

                if (pedidoExistente == null) {
                    "No se encontró el pedido del cliente ${comando.cliente}."
                } else {
                    val productosActuales = pedidoExistente.productos.toMutableList()

                    comando.productos?.forEach { nuevoProducto ->
                        val index = productosActuales.indexOfFirst {
                            it.nombre.equals(nuevoProducto.nombre, ignoreCase = true)
                        }

                        if (nuevoProducto.cantidad == 0) {
                            if (index != -1) productosActuales.removeAt(index)
                        } else {
                            if (index != -1) {
                                productosActuales[index] = nuevoProducto // Actualizar
                            } else {
                                productosActuales.add(nuevoProducto) // Agregar
                            }
                        }
                    }

                    val actualizado = pedidoExistente.copy(
                        productos = productosActuales,
                        fechaLimite = comando.fechaLimite ?: pedidoExistente.fechaLimite
                    )

                    pedidoViewModel.editarPedido(actualizado) {}
                    "Pedido de ${comando.cliente} actualizado correctamente."
                }
            }

            is Comando.EliminarPedido -> {
                val pedidoExistente = pedidoViewModel.pedidos.value
                    ?.find { it.cliente.equals(comando.cliente, ignoreCase = true) }

                if (pedidoExistente == null) {
                    "No se encontró el pedido del cliente ${comando.cliente}."
                } else {
                    pedidoViewModel.eliminarPedido(pedidoExistente.id)
                    "Pedido de ${comando.cliente} eliminado correctamente."
                }
            }

            is Comando.AgregarIngrediente -> {
                ingredienteViewModel.agregarIngrediente(
                    Ingrediente(nombre = comando.nombre, unidad = comando.unidad, costoUnidad = comando.costoUnidad)
                )
                "Ingrediente ${comando.nombre} agregado correctamente."
            }

            is Comando.EditarIngrediente -> {
                val existente = ingredienteViewModel.ingredientes.value
                    ?.find { it.nombre.equals(comando.nombre, ignoreCase = true) }

                if (existente == null) {
                    "No se encontró el ingrediente ${comando.nombre}."
                } else {
                    val actualizado = existente.copy(
                        unidad = comando.unidad ?: existente.unidad,
                        costoUnidad = comando.costoUnidad ?: existente.costoUnidad
                    )
                    ingredienteViewModel.editarIngrediente(actualizado)
                    "Ingrediente ${comando.nombre} actualizado correctamente."
                }
            }

            is Comando.EliminarIngrediente -> {
                val existente = ingredienteViewModel.ingredientes.value
                    ?.find { it.nombre.equals(comando.nombre, ignoreCase = true) }

                if (existente == null) {
                    "No se encontró el ingrediente ${comando.nombre}."
                } else {
                    ingredienteViewModel.eliminarIngrediente(existente.id)
                    "Ingrediente ${comando.nombre} eliminado correctamente."
                }
            }

            Comando.ComandoNoReconocido -> {
                "Lo siento, no pude entender tu solicitud. Intenta reformularla."
            }
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