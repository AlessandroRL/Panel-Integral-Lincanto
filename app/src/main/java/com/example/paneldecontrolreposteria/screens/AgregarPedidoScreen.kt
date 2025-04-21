package com.example.paneldecontrolreposteria.screens

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
fun AgregarPedidoScreen(viewModel: PedidoViewModel, onPedidoAgregado: () -> Unit) {
    var cliente by remember { mutableStateOf("") }
    var producto by remember { mutableStateOf("") }
    var cantidad by remember { mutableStateOf("") }
    var fechaLimite by remember { mutableStateOf("") }
    var tamano by remember { mutableStateOf("") }
    var errorMensaje by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Agregar Pedido") }) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            OutlinedTextField(
                value = cliente,
                onValueChange = { cliente = it },
                label = { Text("Cliente") },
                isError = errorMensaje != null && cliente.isBlank()
            )
            var productosDisponibles by remember { mutableStateOf<List<String>>(emptyList()) }
            var expanded by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                viewModel.obtenerNombresProductos { productos ->
                    productosDisponibles = productos
                }
            }

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    readOnly = true,
                    value = producto,
                    onValueChange = {},
                    label = { Text("Producto") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor(),
                    isError = errorMensaje != null && producto.isBlank()
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    productosDisponibles.forEach { nombre ->
                        DropdownMenuItem(
                            text = { Text(nombre) },
                            onClick = {
                                producto = nombre
                                expanded = false
                            }
                        )
                    }
                }
            }
            OutlinedTextField(
                value = cantidad,
                onValueChange = { cantidad = it },
                label = { Text("Cantidad") },
                isError = errorMensaje != null && cantidad.toIntOrNull() == null
            )
            OutlinedTextField(
                value = tamano,
                onValueChange = { tamano = it },
                label = { Text("Tamaño (Cantidad de Personas)") },
                isError = errorMensaje != null && tamano.isBlank()
            )
            OutlinedTextField(
                value = fechaLimite,
                onValueChange = { fechaLimite = it },
                label = { Text("Fecha Limite") },
                isError = errorMensaje != null && fechaLimite.isBlank()
            )
            if (errorMensaje != null) {
                Text(errorMensaje!!, color = MaterialTheme.colorScheme.error)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    if (cliente.isBlank() || producto.isBlank() || cantidad.toIntOrNull() == null) {
                        errorMensaje = "Por favor, complete todos los campos correctamente."
                        return@Button
                    }
                    scope.launch {
                        try {
                            val nuevoPedido = Pedido(
                                id = "",
                                cliente = cliente,
                                productos = listOf(producto),
                                cantidad = cantidad.toInt(),
                                estado = "Pendiente"
                            )
                            viewModel.agregarPedido(nuevoPedido)
                            onPedidoAgregado()
                        } catch (e: Exception) {
                            errorMensaje = "Error al agregar pedido: ${e.message}"
                        }
                    }
                }
            ) {
                Text("Agregar Pedido")
            }
        }
    }
}