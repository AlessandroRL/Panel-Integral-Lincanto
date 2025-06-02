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
            val intencion = json.getString("intencion").lowercase()

            val cliente = json.getString("cliente")

            when (intencion) {
                "agregar", "editar" -> {
                    val fechaLimite = json.optString("fechaLimite", "").trim()

                    if (!fechaLimite.matches(Regex("""\d{4}-\d{2}-\d{2}"""))) {
                        Log.w("GeminiInterpreter", "Formato de fecha inválido o ausente: $fechaLimite")
                        return Comando.ComandoNoReconocido
                    }

                    val productos = leerProductos(json)
                    return if (intencion == "agregar") {
                        Comando.AgregarPedido(cliente, productos, fechaLimite)
                    } else {
                        Comando.EditarPedido(cliente, productos, fechaLimite)
                    }
                }

                "eliminar" -> {
                    return Comando.EliminarPedido(cliente)
                }

                else -> {
                    Log.w("GeminiInterpreter", "Intención no reconocida: $intencion")
                    Comando.ComandoNoReconocido
                }
            }

        } catch (e: Exception) {
            Log.e("GeminiInterpreter", "Error interpretando JSON: ${e.message}", e)
            Log.d("GeminiInterpreter", "Respuesta original:\n$respuestaGemini")
            Comando.ComandoNoReconocido
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
                    val actualizado = pedidoExistente.copy(
                        productos = comando.productos,
                        fechaLimite = comando.fechaLimite
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