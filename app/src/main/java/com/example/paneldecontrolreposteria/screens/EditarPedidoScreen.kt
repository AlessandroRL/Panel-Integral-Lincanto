package com.example.paneldecontrolreposteria.screens

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.*
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.paneldecontrolreposteria.model.Pedido
import com.example.paneldecontrolreposteria.model.ProductoPedido
import com.example.paneldecontrolreposteria.viewmodel.PedidoViewModel
import kotlinx.coroutines.launch
import java.util.Calendar

@SuppressLint("DefaultLocale")
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

    val productos = remember { mutableStateListOf<ProductoPedido>().apply { addAll(pedido.productos) } }
    var productosDisponibles by remember { mutableStateOf<List<String>>(emptyList()) }
    var productoAEliminar by remember { mutableStateOf<ProductoPedido?>(null) }

    LaunchedEffect(Unit) {
        viewModel.obtenerNombresProductos { productos ->
            productosDisponibles = productos
        }
    }

    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    Scaffold(
        topBar = { TopAppBar(title = { Text("Editar Pedido") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            OutlinedTextField(
                value = cliente,
                onValueChange = { cliente = it },
                label = { Text("Cliente") },
                modifier = Modifier.fillMaxWidth()
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable {
                        val datePickerDialog = android.app.DatePickerDialog(
                            context,
                            { _, selectedYear, selectedMonth, selectedDay ->
                                fechaLimite = String.format(
                                    "%04d-%02d-%02d",
                                    selectedYear,
                                    selectedMonth + 1,
                                    selectedDay
                                )
                            },
                            year,
                            month,
                            day
                        )
                        datePickerDialog.show()
                    }
            ) {
                OutlinedTextField(
                    value = fechaLimite,
                    onValueChange = {},
                    label = { Text("Fecha Límite") },
                    readOnly = true,
                    enabled = false,
                    isError = errorMensaje != null && fechaLimite.isBlank(),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Text(
                "Productos",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 16.dp)
            )

            productos.forEachIndexed { index, producto ->
                var expanded by remember { mutableStateOf(false) }

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        readOnly = true,
                        value = producto.nombre,
                        onValueChange = {},
                        label = { Text("Producto ${index + 1}") },
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
                                    productos[index] = producto.copy(nombre = nombre)
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = producto.cantidad.toString(),
                    onValueChange = {
                        productos[index] =
                            producto.copy(cantidad = it.toIntOrNull() ?: producto.cantidad)
                    },
                    label = { Text("Cantidad") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
                )

                OutlinedTextField(
                    value = producto.tamano.toString(),
                    onValueChange = {
                        productos[index] =
                            producto.copy(tamano = it.toIntOrNull() ?: producto.tamano)
                    },
                    label = { Text("Tamaño (personas)") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )

                IconButton(
                    onClick = { productoAEliminar = producto },
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Eliminar Producto",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            Button(
                onClick = {
                    productos.add(ProductoPedido(nombre = "", cantidad = 1, tamano = 1))
                },
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Text("Agregar otro producto")
            }

            if (errorMensaje != null) {
                Text(errorMensaje!!, color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = {
                        if (cliente.isBlank() || productos.any { it.nombre.isBlank() || it.cantidad <= 0 || it.tamano <= 0 }) {
                            errorMensaje = "Por favor, complete todos los campos correctamente."
                            return@Button
                        }

                        val pedidoActualizado = pedido.copy(
                            cliente = cliente,
                            productos = productos,
                            fechaLimite = fechaLimite
                        )

                        scope.launch {
                            viewModel.editarPedido(pedidoActualizado) { success ->
                                if (success) {
                                    Toast.makeText(context, "Pedido editado", Toast.LENGTH_SHORT)
                                        .show()
                                    onPedidoEditado()
                                } else {
                                    errorMensaje = "Error al editar el pedido."
                                }
                            }
                        }
                    }
                ) {
                    Text("Guardar Cambios")
                }

                OutlinedButton(
                    onClick = { onPedidoEditado() }
                ) {
                    Text("Cancelar")
                }
            }
        }
    }

    if (productoAEliminar != null) {
        AlertDialog(
            onDismissRequest = { productoAEliminar = null },
            title = { Text("Eliminar Producto") },
            text = { Text("¿Estás seguro de que quieres eliminar este producto?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        productos.remove(productoAEliminar)
                        productoAEliminar = null
                        Toast.makeText(context, "Producto eliminado", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Eliminar", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { productoAEliminar = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}