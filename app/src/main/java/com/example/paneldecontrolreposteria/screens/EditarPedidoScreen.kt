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
    var producto by remember { mutableStateOf(pedido.productos.joinToString(", ")) }
    var cantidad by remember { mutableStateOf(pedido.cantidad.toString()) }
    var fechaLimite by remember { mutableStateOf(pedido.fechaLimite) }
    var tamano by remember { mutableStateOf(pedido.tamano.toString()) }
    var errorMensaje by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Editar Pedido") }) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            OutlinedTextField(value = cliente, onValueChange = { cliente = it }, label = { Text("Cliente") })
            OutlinedTextField(value = producto, onValueChange = { producto = it }, label = { Text("Producto") })
            OutlinedTextField(value = cantidad, onValueChange = { cantidad = it }, label = { Text("Cantidad") })
            OutlinedTextField(value = fechaLimite, onValueChange = { fechaLimite = it }, label = { Text("Fecha Límite") })
            OutlinedTextField(value = tamano, onValueChange = { tamano = it }, label = { Text("Tamaño (personas)") })

            if (errorMensaje != null) {
                Text(errorMensaje!!, color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                if (cliente.isBlank() || producto.isBlank() || cantidad.toIntOrNull() == null || tamano.toIntOrNull() == null) {
                    errorMensaje = "Por favor, complete todos los campos correctamente."
                    return@Button
                }

                val pedidoActualizado = pedido.copy(
                    cliente = cliente,
                    productos = listOf(producto),
                    cantidad = cantidad.toInt(),
                    fechaLimite = fechaLimite,
                    tamano = tamano.toInt()
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