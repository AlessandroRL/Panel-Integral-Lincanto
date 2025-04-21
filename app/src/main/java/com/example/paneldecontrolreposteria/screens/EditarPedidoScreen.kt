import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.paneldecontrolreposteria.model.Pedido
import com.example.paneldecontrolreposteria.viewmodel.PedidoViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarPedidoScreen(
    viewModel: PedidoViewModel,
    pedido: Pedido,
    onPedidoEditado: () -> Unit
) {
    var cliente by remember { mutableStateOf(pedido.cliente) }
    var fechaLimite by remember { mutableStateOf(pedido.fechaLimite) }
    var errorMensaje by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    data class ProductoSeleccionado(
        val nombre: String,
        var cantidad: String,
        var tamano: String
    )

    // Convertimos productos guardados (en formato string) a objetos manipulables
    var productosSeleccionados by remember {
        mutableStateOf(
            pedido.productos.mapNotNull { producto ->
                val partes = producto.split(" - ")
                if (partes.size == 3) {
                    ProductoSeleccionado(
                        nombre = partes[0],
                        cantidad = partes[1].replace(" u", "").trim(),
                        tamano = partes[2].replace(" personas", "").trim()
                    )
                } else null
            }.toMutableList()
        )
    }

    var productosDisponibles by remember { mutableStateOf<List<String>>(emptyList()) }
    var productoActual by remember { mutableStateOf("") }
    var cantidadActual by remember { mutableStateOf("") }
    var tamanoActual by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.obtenerNombresProductos { productos ->
            productosDisponibles = productos
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Editar Pedido") }) }
    ) { padding ->
        Column(modifier = Modifier
            .padding(padding)
            .padding(16.dp)
            .fillMaxSize()) {

            OutlinedTextField(
                value = cliente,
                onValueChange = { cliente = it },
                label = { Text("Cliente") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    readOnly = true,
                    value = productoActual,
                    onValueChange = {},
                    label = { Text("Producto") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    productosDisponibles.forEach { nombre ->
                        DropdownMenuItem(
                            text = { Text(nombre) },
                            onClick = {
                                productoActual = nombre
                                expanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = cantidadActual,
                onValueChange = { cantidadActual = it },
                label = { Text("Cantidad") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = tamanoActual,
                onValueChange = { tamanoActual = it },
                label = { Text("Tamaño (Cantidad de Personas)") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    if (productoActual.isNotBlank() && cantidadActual.isNotBlank() && tamanoActual.isNotBlank()) {
                        productosSeleccionados.add(
                            ProductoSeleccionado(productoActual, cantidadActual, tamanoActual)
                        )
                        productoActual = ""
                        cantidadActual = ""
                        tamanoActual = ""
                    }
                },
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Text("Añadir Producto")
            }

            if (productosSeleccionados.isNotEmpty()) {
                Text("Productos actuales:", style = MaterialTheme.typography.titleMedium)
                productosSeleccionados.forEach {
                    Text("- ${it.nombre} (${it.cantidad} u, ${it.tamano} personas)")
                }
            }

            OutlinedTextField(
                value = fechaLimite,
                onValueChange = { fechaLimite = it },
                label = { Text("Fecha Límite") },
                modifier = Modifier.fillMaxWidth()
            )

            if (errorMensaje != null) {
                Text(errorMensaje!!, color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (cliente.isBlank() || productosSeleccionados.isEmpty() || fechaLimite.isBlank()) {
                        errorMensaje = "Por favor, complete todos los campos correctamente."
                        return@Button
                    }

                    val pedidoActualizado = pedido.copy(
                        cliente = cliente,
                        productos = productosSeleccionados.map {
                            "${it.nombre} - ${it.cantidad} u - ${it.tamano} personas"
                        },
                        cantidad = productosSeleccionados.sumOf { it.cantidad.toIntOrNull() ?: 0 },
                        tamano = productosSeleccionados.sumOf { it.tamano.toIntOrNull() ?: 0 },
                        fechaLimite = fechaLimite
                    )

                    scope.launch {
                        viewModel.editarPedido(pedidoActualizado) { success ->
                            if (success) {
                                onPedidoEditado()
                            } else {
                                errorMensaje = "Error al editar el pedido."
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar Cambios")
            }
        }
    }
}
