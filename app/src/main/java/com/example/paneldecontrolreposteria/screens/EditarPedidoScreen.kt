import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.*
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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

    val productos = remember {
        mutableStateListOf<Map<String, String>>().apply {
            addAll(
                pedido.productos.map {
                    mapOf(
                        "nombre" to it,
                        "cantidad" to pedido.cantidad.toString(),
                        "tamano" to pedido.tamano.toString()
                    )
                }
            )
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Editar Pedido") }) }
    ) { padding ->
        Column(modifier = Modifier
            .padding(padding)
            .padding(16.dp)) {

            OutlinedTextField(
                value = cliente,
                onValueChange = { cliente = it },
                label = { Text("Cliente") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = fechaLimite,
                onValueChange = { fechaLimite = it },
                label = { Text("Fecha Límite") },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            )

            Text("Productos", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 16.dp))

            productos.forEachIndexed { index, producto ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    OutlinedTextField(
                        value = producto["nombre"] ?: "",
                        onValueChange = {
                            productos[index] = producto.toMutableMap().apply { put("nombre", it) }
                        },
                        label = { Text("Producto ${index + 1}") },
                        modifier = Modifier.weight(1f).padding(end = 4.dp)
                    )

                    IconButton(
                        onClick = {
                            if (productos.size > 1) {
                                productos.removeAt(index)
                            }
                        },
                        modifier = Modifier.align(Alignment.CenterVertically)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar producto", tint = MaterialTheme.colorScheme.error)
                    }
                }

                OutlinedTextField(
                    value = producto["cantidad"] ?: "",
                    onValueChange = {
                        productos[index] = producto.toMutableMap().apply { put("cantidad", it) }
                    },
                    label = { Text("Cantidad") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
                )

                OutlinedTextField(
                    value = producto["tamano"] ?: "",
                    onValueChange = {
                        productos[index] = producto.toMutableMap().apply { put("tamano", it) }
                    },
                    label = { Text("Tamaño (personas)") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )
            }

            Button(
                onClick = {
                    productos.add(mapOf("nombre" to "", "cantidad" to "", "tamano" to ""))
                },
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Text("Agregar otro producto")
            }

            if (errorMensaje != null) {
                Text(errorMensaje!!, color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {
                if (cliente.isBlank() || productos.any { it["nombre"].isNullOrBlank() || it["cantidad"].isNullOrBlank() || it["tamano"].isNullOrBlank() }) {
                    errorMensaje = "Por favor, complete todos los campos correctamente."
                    return@Button
                }

                val productosEditados = productos.map { it["nombre"] ?: "" }
                val primeraCantidad = productos.firstOrNull()?.get("cantidad")?.toIntOrNull() ?: 1
                val primerTamano = productos.firstOrNull()?.get("tamano")?.toIntOrNull() ?: 1

                val pedidoActualizado = pedido.copy(
                    cliente = cliente,
                    productos = productosEditados,
                    cantidad = primeraCantidad,
                    tamano = primerTamano,
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
            }) {
                Text("Guardar Cambios")
            }
        }
    }
}
