package com.example.paneldecontrolreposteria.ui.ai

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.paneldecontrolreposteria.model.Ingrediente
import com.example.paneldecontrolreposteria.model.IngredienteDetalle
import com.example.paneldecontrolreposteria.model.Pedido
import com.example.paneldecontrolreposteria.model.Producto
import com.example.paneldecontrolreposteria.model.ProductoPedido
import com.example.paneldecontrolreposteria.viewmodel.PedidoViewModel
import com.example.paneldecontrolreposteria.viewmodel.IngredienteViewModel
import com.example.paneldecontrolreposteria.viewmodel.ProductoViewModel
import org.json.JSONObject
import org.apache.commons.text.similarity.LevenshteinDistance
import java.time.LocalDate
import java.time.Month
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoField

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class GeminiCommandInterpreter(
    private val pedidoViewModel: PedidoViewModel,
    private val ingredienteViewModel: IngredienteViewModel,
    private val productoViewModel: ProductoViewModel,
) {
    private var comandoPendiente: Pair<Comando, String>? = null

    fun interpretar(respuestaGemini: String): Comando {
        return try {
            val limpio = respuestaGemini
                .replace(Regex("```json\\s*"), "")
                .replace(Regex("```\\s*"), "")
                .trim()

            Log.d("GeminiInterpreter", "JSON limpio:\n$limpio")

            val json = JSONObject(limpio)
            val intencion = json.optString("intencion", "").lowercase()

            val tipo = when {
                json.has("tipo") -> json.optString("tipo", "").lowercase()
                intencion.startsWith("consultar") -> "consulta"
                json.has("nombre") && json.has("unidad") -> "ingrediente"
                json.has("nombre") && json.has("ingredientes") -> "producto"
                json.has("cliente") && json.has("productos") -> "pedido"

                intencion in listOf("agregar", "editar", "eliminar") -> "pedido"
                else -> "desconocido"
            }

            return when (tipo) {
                "ingrediente" -> interpretarIngrediente(json)
                "producto" -> interpretarProducto(json)
                "pedido" -> interpretarPedido(json)
                "consulta" -> interpretarConsulta(json)
                else -> {
                    Log.w("GeminiInterpreter", "Tipo no reconocido: \"$tipo\" en respuesta: $limpio")
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

    private fun interpretarProducto(json: JSONObject): Comando {
        val intencion = json.optString("intencion").lowercase()
        val nombre = json.optString("nombre", "")

        val preparacion = json.optString("preparacion", null)
        val tips = json.optString("tips", null)

        val ingredientesJson = json.optJSONArray("ingredientes")
        val ingredientes = ingredientesJson?.let {
            (0 until it.length()).mapNotNull { i ->
                it.optJSONObject(i)?.let { ingr ->
                    IngredienteDetalle(
                        nombre = ingr.optString("nombre", ""),
                        unidad = ingr.optString("unidad", ""),
                        cantidad = ingr.optDouble("cantidad", 0.0),
                        observacion = ingr.optString("observacion", null)
                    )
                }
            }
        }

        val utensiliosJson = json.optJSONArray("utensilios")
        val utensilios = utensiliosJson?.let {
            (0 until it.length()).mapNotNull { i -> it.optString(i, null) }
        }

        return when (intencion) {
            "agregar" -> Comando.AgregarProducto(
                nombre = nombre,
                ingredientes = ingredientes ?: emptyList(),
                preparacion = preparacion,
                utensilios = utensilios,
                tips = tips
            )

            "editar" -> Comando.EditarProducto(
                nombre = nombre,
                ingredientes = ingredientes,
                preparacion = preparacion,
                utensilios = utensilios,
                tips = tips
            )

            "eliminar" -> Comando.EliminarProducto(nombre)

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
                    val tamano = try { p.getInt("tamano") } catch (_: Exception) { p.getDouble("tamano").toInt() }
                    val cantidad = try { p.getInt("cantidad") } catch (_: Exception) { p.getDouble("cantidad").toInt() }

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

    private fun interpretarConsulta(json: JSONObject): Comando {
        val intencion = json.optString("intencion", "").lowercase()

        if (intencion.isBlank()) {
            Log.w("GeminiInterpreter", "Consulta sin intención especificada")
            return Comando.ComandoNoReconocido
        }

        return when (intencion) {
            "consultar_pedidos" -> {
                val rango = json.optString("rango", "todos")
                val mesNombre = json.optString("mes", "")
                val anio = json.optInt("anio", LocalDate.now().year)

                Comando.ConsultarPedidos(rango, mesNombre, anio)
            }

            "consultar_ingredientes" -> {
                when {
                    json.has("nombreIngrediente") -> {
                        val nombre = json.optString("nombreIngrediente", "").trim()
                        if (nombre.isBlank()) {
                            Comando.ComandoNoReconocido
                        } else {
                            Comando.ConsultarSiIngredienteExiste(nombre)
                        }
                    }
                    json.optBoolean("cantidadTotal", false) -> {
                        Comando.ConsultarIngredientesTotales()
                    }
                    else -> {
                        Comando.ConsultarListaIngredientes()
                    }
                }
            }

            "consultar_productos" -> {
                if (json.has("nombreProducto")) {
                    val nombre = json.optString("nombreProducto", "").trim()
                    val campo = json.optString("campo", "todo").lowercase()

                    if (nombre.isBlank()) {
                        Comando.ComandoNoReconocido
                    } else {
                        Comando.ConsultarInfoProducto(nombre, campo)
                    }
                } else {
                    Comando.ConsultarListaProductos()
                }
            }

            else -> {
                Log.w("GeminiInterpreter", "Objeto de consulta no reconocido: $intencion")
                Comando.ComandoNoReconocido
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun ejecutar(comando: Comando): String {

        if (comando is Comando.RespuestaSimple) {
            if (comando.respuesta.equals("no", ignoreCase = true)) {
                comandoPendiente = null
                return "De acuerdo. No se realizará ninguna acción."
            }

            if (comando.respuesta.equals("sí", ignoreCase = true) || comando.respuesta.equals("si", ignoreCase = true)) {
                val (comandoOriginal, nombreSugerido) = comandoPendiente ?: return "No hay ninguna acción pendiente para confirmar."
                comandoPendiente = null

                val comandoCorregido = when (comandoOriginal) {
                    is Comando.EditarProducto -> comandoOriginal.copy(nombre = nombreSugerido)
                    is Comando.EliminarProducto -> comandoOriginal.copy(nombre = nombreSugerido)
                    is Comando.EditarIngrediente -> comandoOriginal.copy(nombre = nombreSugerido)
                    is Comando.EliminarIngrediente -> comandoOriginal.copy(nombre = nombreSugerido)
                    is Comando.AgregarIngrediente -> comandoOriginal.copy(nombre = nombreSugerido)
                    is Comando.ConsultarInfoProducto -> comandoOriginal.copy(nombre = nombreSugerido)
                    is Comando.ConsultarSiIngredienteExiste -> comandoOriginal.copy(nombre = nombreSugerido)
                    else -> return "Este comando no puede ser corregido automáticamente."
                }

                return ejecutar(comandoCorregido)
            }

            return "No entendí tu respuesta. Por favor, responde sí o no."
        }

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
                    .find { it.cliente.equals(comando.cliente, ignoreCase = true) }

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
                    .find { it.cliente.equals(comando.cliente, ignoreCase = true) }

                if (pedidoExistente == null) {
                    "No se encontró el pedido del cliente ${comando.cliente}."
                } else {
                    pedidoViewModel.eliminarPedido(pedidoExistente.id)
                    "Pedido de ${comando.cliente} eliminado correctamente."
                }
            }

            is Comando.AgregarIngrediente -> {
                val existentes = ingredienteViewModel.ingredientes.value
                val nombreOriginal = comando.nombre.trim()
                var nombreFinal = nombreOriginal

                val nombreSimilar = encontrarIngredienteSimilar(nombreFinal)
                if (nombreSimilar != null && !nombreSimilar.equals(nombreOriginal, ignoreCase = true)) {
                    comandoPendiente = comando to nombreSimilar
                    return "Ya existe un ingrediente llamado \"$nombreSimilar\". ¿Quieres usar ese nombre en lugar de \"$nombreOriginal\"? Responde sí o no."
                }

                if (existentes.any { it.nombre.equals(nombreFinal, ignoreCase = true) }) {
                    var contador = 2
                    var nuevoNombre: String
                    do {
                        nuevoNombre = "$nombreFinal $contador"
                        contador++
                    } while (existentes.any { it.nombre.equals(nuevoNombre, ignoreCase = true) })
                    nombreFinal = nuevoNombre
                }

                ingredienteViewModel.agregarIngrediente(
                    Ingrediente(
                        nombre = nombreFinal,
                        unidad = comando.unidad,
                        costoUnidad = comando.costoUnidad
                    )
                )
                "Ingrediente \"$nombreFinal\" agregado correctamente."
            }

            is Comando.EditarIngrediente -> {
                val existente = ingredienteViewModel.ingredientes.value
                    .find { it.nombre.equals(comando.nombre, ignoreCase = true) }

                if (existente == null) {
                    val nombreSimilar = encontrarIngredienteSimilar(comando.nombre)
                    return if (nombreSimilar != null) {
                        comandoPendiente = comando to nombreSimilar
                        "El ingrediente \"${comando.nombre}\" no fue encontrado. ¿Te refieres a \"$nombreSimilar\"? Responde sí o no."
                    } else {
                        "No se encontró ningún ingrediente llamado \"${comando.nombre}\"."
                    }
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
                    .find { it.nombre.equals(comando.nombre, ignoreCase = true) }

                if (existente == null) {
                    val nombreSimilar = encontrarIngredienteSimilar(comando.nombre)
                    return if (nombreSimilar != null) {
                        comandoPendiente = comando to nombreSimilar
                        "El ingrediente \"${comando.nombre}\" no fue encontrado. ¿Te refieres a \"$nombreSimilar\"? Responde sí o no."
                    } else {
                        "No se encontró ningún ingrediente llamado \"${comando.nombre}\"."
                    }
                } else {
                    ingredienteViewModel.eliminarIngrediente(existente.id)
                    "Ingrediente ${comando.nombre} eliminado correctamente."
                }
            }

            is Comando.AgregarProducto -> {
                val ingredientesInvalidos = comando.ingredientes?.filterNot { nuevo ->
                    ingredienteViewModel.ingredientes.value.any {
                        it.nombre.equals(nuevo.nombre, ignoreCase = true)
                    }
                } ?: emptyList()

                return if (ingredientesInvalidos.isNotEmpty()) {
                    val nombres = ingredientesInvalidos.joinToString(", ") { it.nombre }
                    "Los siguientes ingredientes no están registrados: $nombres. Agrégalos primero desde la sección de ingredientes."
                } else {
                    val existentes = productoViewModel.productos.value
                    var nombreFinal = comando.nombre.trim()

                    if (existentes.any { it.nombre.equals(nombreFinal, ignoreCase = true) }) {
                        var contador = 2
                        var nuevoNombre: String
                        do {
                            nuevoNombre = "$nombreFinal $contador"
                            contador++
                        } while (existentes.any { it.nombre.equals(nuevoNombre, ignoreCase = true) })
                        nombreFinal = nuevoNombre
                    }

                    val producto = Producto(
                        nombre = nombreFinal,
                        ingredientes = comando.ingredientes ?: emptyList(),
                        preparacion = comando.preparacion,
                        utensilios = comando.utensilios,
                        tips = comando.tips
                    )
                    productoViewModel.agregarProducto(producto)
                    "Producto \"$nombreFinal\" agregado correctamente."
                }
            }

            is Comando.EditarProducto -> {
                val productos = productoViewModel.productos.value
                val nombreIngresado = comando.nombre

                val existente = productos.find { it.nombre.equals(nombreIngresado, ignoreCase = true) }

                if (existente == null) {
                    val nombreSugerido = encontrarNombreMasParecidoConPalabras(nombreIngresado, productos.map { it.nombre })

                    return if (nombreSugerido != null) {
                        comandoPendiente = comando to nombreSugerido
                        "El producto \"${nombreIngresado}\" no fue encontrado. ¿Te refieres a \"$nombreSugerido\"? Responde sí o no."
                    } else {
                        "No se encontró el producto \"$nombreIngresado\" y no se encontró ningún nombre similar."
                    }
                }

                val ingredientesEditados = if (comando.ingredientes != null) {
                    val ingredientesInvalidos = comando.ingredientes.filterNot { nuevo ->
                        ingredienteViewModel.ingredientes.value.any {
                            it.nombre.equals(nuevo.nombre, ignoreCase = true)
                        }
                    }

                    if (ingredientesInvalidos.isNotEmpty()) {
                        val nombres = ingredientesInvalidos.joinToString(", ") { it.nombre }
                        return "No se puede editar el producto. Los siguientes ingredientes no están registrados: $nombres. Agrégalos primero desde la sección de ingredientes."
                    }

                    val actuales = existente.ingredientes.toMutableList()
                    comando.ingredientes.forEach { nuevo ->
                        val index = actuales.indexOfFirst {
                            it.nombre.equals(nuevo.nombre, ignoreCase = true)
                        }

                        if (nuevo.cantidad == 0.0) {
                            if (index != -1) actuales.removeAt(index) // eliminar ingrediente
                        } else {
                            if (index != -1) {
                                actuales[index] = nuevo // actualizar
                            } else {
                                actuales.add(nuevo) // agregar
                            }
                        }
                    }
                    actuales
                } else existente.ingredientes

                val actualizado = existente.copy(
                    ingredientes = ingredientesEditados,
                    preparacion = when (comando.preparacion) {
                        null -> existente.preparacion
                        "" -> null
                        else -> comando.preparacion
                    },
                    utensilios = when (comando.utensilios) {
                        null -> existente.utensilios
                        emptyList<String>() -> null
                        else -> comando.utensilios
                    },
                    tips = when (comando.tips) {
                        null -> existente.tips
                        "" -> null
                        else -> comando.tips
                    }
                )

                productoViewModel.actualizarProducto(existente.nombre, actualizado)
                "Producto \"${existente.nombre}\" actualizado correctamente."
            }

            is Comando.EliminarProducto -> {
                val productos = productoViewModel.productos.value
                val nombreIngresado = comando.nombre.trim()

                val existente = productos.find { it.nombre.equals(nombreIngresado, ignoreCase = true) }

                if (existente == null) {
                    val nombreSugerido = encontrarNombreMasParecidoConPalabras(nombreIngresado, productos.map { it.nombre })

                    return if (nombreSugerido != null) {
                        comandoPendiente = comando to nombreSugerido
                        "El producto \"$nombreIngresado\" no fue encontrado. ¿Te refieres a \"$nombreSugerido\"? Responde sí o no."
                    } else {
                        "No se encontró el producto \"$nombreIngresado\" y no se encontró ningún nombre similar."
                    }
                }

                productoViewModel.eliminarProducto(existente.id)
                "Producto \"${existente.nombre}\" eliminado correctamente."
            }

            is Comando.ConsultarPedidos -> {
                val pedidos = pedidoViewModel.pedidos.value
                val tipo = comando.tipo
                val hoy = LocalDate.now()
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

                val filtrados = pedidos.filter { pedido ->
                    try {
                        val fecha = LocalDate.parse(pedido.fechaLimite, formatter)
                        when (tipo) {
                            "hoy" -> fecha == hoy
                            "mañana" -> {
                                fecha == hoy.plusDays(1)
                            }
                            "semana" -> {
                                val semanaHoy = hoy.get(ChronoField.ALIGNED_WEEK_OF_YEAR)
                                val semanaPedido = fecha.get(ChronoField.ALIGNED_WEEK_OF_YEAR)
                                semanaHoy == semanaPedido && hoy.year == fecha.year
                            }
                            "proxima_semana" -> {
                                val semanaHoy = hoy.get(ChronoField.ALIGNED_WEEK_OF_YEAR)
                                val semanaPedido = fecha.get(ChronoField.ALIGNED_WEEK_OF_YEAR)
                                semanaPedido == semanaHoy + 1 && hoy.year == fecha.year
                            }
                            "mes" -> fecha.month == hoy.month && fecha.year == hoy.year
                            "mes_nombre" -> {
                                val mesInput = comando.mesNombre.lowercase().replace("é", "e")
                                val mesesMap = mapOf(
                                    "enero" to Month.JANUARY,
                                    "febrero" to Month.FEBRUARY,
                                    "marzo" to Month.MARCH,
                                    "abril" to Month.APRIL,
                                    "mayo" to Month.MAY,
                                    "junio" to Month.JUNE,
                                    "julio" to Month.JULY,
                                    "agosto" to Month.AUGUST,
                                    "septiembre" to Month.SEPTEMBER,
                                    "octubre" to Month.OCTOBER,
                                    "noviembre" to Month.NOVEMBER,
                                    "diciembre" to Month.DECEMBER
                                )

                                val mesBuscado = mesesMap[mesInput]
                                val anioBuscado = comando.anio
                                mesBuscado != null && fecha.month == mesBuscado && fecha.year == anioBuscado
                            }
                            "todos" -> true
                            else -> false
                        }
                    } catch (_: Exception) {
                        false
                    }
                }

                return if (filtrados.isEmpty()) {
                    "No hay pedidos para $tipo."
                } else {
                    "Tienes ${filtrados.size} pedido(s) para $tipo:\n\n" +
                            filtrados.joinToString("\n\n") { pedido ->
                                val productosTexto = pedido.productos.joinToString("\n") { prod ->
                                    "    - ${prod.nombre} (${prod.tamano}), Cantidad: ${prod.cantidad}"
                                }
                                "- ${pedido.cliente}: ${pedido.fechaLimite}\n$productosTexto"
                            }
                }
            }

            is Comando.ConsultarIngredientesTotales -> {
                val total = ingredienteViewModel.ingredientes.value.size
                return "Tienes $total ingredientes registrados."
            }

            is Comando.ConsultarSiIngredienteExiste -> {
                val ingredientes = ingredienteViewModel.ingredientes.value
                val nombreIngresado = comando.nombre

                val existente = ingredientes.find { it.nombre.equals(nombreIngresado, ignoreCase = true) }

                if (existente == null) {
                    val nombreSugerido = encontrarIngredienteSimilar(nombreIngresado)

                    return if (nombreSugerido != null) {
                        comandoPendiente = comando to nombreSugerido
                        "El ingrediente \"$nombreIngresado\" no fue encontrado. ¿Te refieres a \"$nombreSugerido\"? Responde sí o no."
                    } else {
                        "No se encontró el ingrediente \"$nombreIngresado\" y no se encontró ningún nombre similar."
                    }
                }

                return "Sí, el ingrediente \"${nombreIngresado}\" está registrado."
            }

            is Comando.ConsultarListaIngredientes -> {
                val lista = ingredienteViewModel.ingredientes.value
                return if (lista.isEmpty()) "No tienes ingredientes aún."
                else "Ingredientes:\n" + lista.joinToString("\n") { "- ${it.nombre}" }
            }

            is Comando.ConsultarListaProductos -> {
                val lista = productoViewModel.productos.value
                return if (lista.isEmpty()) "No tienes productos aún."
                else "Productos:\n" + lista.joinToString("\n") { "- ${it.nombre}" }
            }

            is Comando.ConsultarInfoProducto -> {
                val productos = productoViewModel.productos.value
                val nombreIngresado = comando.nombre

                val existente = productos.find { it.nombre.equals(nombreIngresado, ignoreCase = true) }

                if (existente == null) {
                    val nombreSugerido = encontrarNombreMasParecidoConPalabras(nombreIngresado, productos.map { it.nombre })

                    return if (nombreSugerido != null) {
                        comandoPendiente = comando.copy(nombre = nombreSugerido) to nombreSugerido
                        "El producto \"$nombreIngresado\" no fue encontrado. ¿Te refieres a \"$nombreSugerido\"? Responde sí o no."
                    } else {
                        "No se encontró el producto \"$nombreIngresado\" y no se encontró ningún nombre similar."
                    }
                }

                val info = existente

                return when (comando.campo.lowercase().trim()) {
                    "ingredientes" -> {
                        val lista = info.ingredientes.joinToString("\n") { "- ${it.nombre} (${it.cantidad})" }
                        "🧾 Ingredientes de ${info.nombre}:\n$lista"
                    }

                    "preparacion" -> "🧑‍🍳 Preparación de ${info.nombre}:\n${info.preparacion?.trim().orEmpty()}"

                    "utensilios" -> "🧰 🧰 Utensilios necesarios para ${info.nombre}:\n${info.utensilios?.joinToString("\n")?.trim().orEmpty()}"

                    "tips" -> "💡 Tips para ${info.nombre}:\n${info.tips?.trim().orEmpty()}"

                    "todo" -> {
                        val ingredientesStr = info.ingredientes.joinToString("\n") { "- ${it.nombre} (${it.cantidad})" }.trim()
                        val preparacionStr = info.preparacion?.trim().orEmpty()
                        val utensiliosStr = info.utensilios?.joinToString("\n")?.trim().orEmpty()
                        val tipsStr = info.tips?.trim().orEmpty()

                        """
📦 Producto: ${info.nombre}
    
🧾 Ingredientes:
$ingredientesStr

🧑‍🍳 Preparación:
$preparacionStr

🧰 Utensilios:
$utensiliosStr

💡 Tips:
$tipsStr
""".trimIndent()
                    }

                    else -> "No se reconoció la parte solicitada de ${info.nombre}. Usa ingredientes, preparación, utensilios, tips o todo."
                }
            }

            Comando.ComandoNoReconocido -> {
                "Lo siento, no pude entender tu solicitud. Intenta reformularla."
            }
            else -> "Comando no reconocido."
        }
    }

    private fun encontrarIngredienteSimilar(nombreBuscado: String): String? {
        val nombreNormalizado = normalizar(nombreBuscado)
        val ingredientes = ingredienteViewModel.ingredientes.value

        return ingredientes
            .map { it.nombre to normalizar(it.nombre) }
            .maxByOrNull { (_, normalizado) -> similaridad(nombreNormalizado, normalizado) }
            ?.let { (original, normalizado) ->
                if (similaridad(nombreNormalizado, normalizado) >= 0.8) original else null
            }
    }

    fun encontrarNombreMasParecidoConPalabras(nombreIngresado: String, listaNombres: List<String>): String? {
        val palabrasClave = nombreIngresado.lowercase().split(" ").filter { it.length > 2 }

        val coincidencias = listaNombres.mapNotNull { nombreExistente ->
            val palabrasExistente = nombreExistente.lowercase().split(" ")
            val coincidencia = palabrasClave.count { it in palabrasExistente }
            if (coincidencia > 0) {
                Triple(nombreExistente, coincidencia, similaridad(nombreIngresado.lowercase(), nombreExistente.lowercase()))
            } else null
        }

        return coincidencias
            .sortedWith(Comparator { a, b ->
                when {
                    a.second != b.second -> b.second.compareTo(a.second)
                    else -> a.third.compareTo(b.third)
                }
            })
            .firstOrNull()?.first
    }

    private fun similaridad(a: String, b: String): Double {
        val distancia = LevenshteinDistance().apply(a, b)
        return 1.0 - distancia.toDouble() / maxOf(a.length, b.length)
    }

    private fun normalizar(nombre: String): String {
        val stopWords = listOf("de", "en", "la", "el", "los", "las", "del", "un", "una")
        return nombre
            .lowercase()
            .replace(Regex("[áàäâ]"), "a")
            .replace(Regex("[éèëê]"), "e")
            .replace(Regex("[íìïî]"), "i")
            .replace(Regex("[óòöô]"), "o")
            .replace(Regex("[úùüû]"), "u")
            .replace(Regex("[^a-z0-9 ]"), "")
            .split(" ")
            .filter { it.isNotBlank() && it !in stopWords }
            .sorted()
            .joinToString(" ")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun procesarRespuestaSimple(texto: String): String? {
        if (comandoPendiente != null && (texto.equals("sí", ignoreCase = true) || texto.equals("no", ignoreCase = true))) {
            val comandoRespuesta = Comando.RespuestaSimple(texto.lowercase())
            return ejecutar(comandoRespuesta)
        }
        return null
    }
}