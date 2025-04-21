package com.example.paneldecontrolreposteria.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.paneldecontrolreposteria.model.Pedido
import com.example.paneldecontrolreposteria.viewmodel.PedidoViewModel
import kotlinx.coroutines.launch
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.DropdownMenuItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgregarPedidoScreen(viewModel: PedidoViewModel, onPedidoAgregado: () -> Unit) {
    var cliente by remember { mutableStateOf("") }
    var fechaLimite by remember { mutableStateOf("") }
    var errorMensaje by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    data class ProductoSeleccionado(
        val nombre: String,
        var cantidad: String,
        var tamano: String
    )

    var productosSeleccionados by remember { mutableStateOf(mutableListOf<ProductoSeleccionado>()) }

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
        topBar = { TopAppBar(title = { Text("Agregar Pedido") }) }
    ) { padding ->
        Column(modifier = Modifier
            .padding(padding)
            .padding(16.dp)
            .fillMaxSize()) {

            OutlinedTextField(
                value = cliente,
                onValueChange = { cliente = it },
                label = { Text("Cliente") },
                modifier = Modifier.fillMaxWidth(),
                isError = errorMensaje != null && cliente.isBlank()
            )

            Spacer(modifier = Modifier.height(16.dp))

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
                        .fillMaxWidth(),
                    isError = errorMensaje != null && productoActual.isBlank()
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
                modifier = Modifier.fillMaxWidth(),
                isError = cantidadActual.toIntOrNull() == null
            )

            OutlinedTextField(
                value = tamanoActual,
                onValueChange = { tamanoActual = it },
                label = { Text("Tamaño (Cantidad de Personas)") },
                modifier = Modifier.fillMaxWidth(),
                isError = tamanoActual.isBlank()
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
                Text("Productos Agregados:", style = MaterialTheme.typography.titleMedium)
                productosSeleccionados.forEach {
                    Text("- ${it.nombre} (${it.cantidad} u, ${it.tamano} personas)")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = fechaLimite,
                onValueChange = { fechaLimite = it },
                label = { Text("Fecha Límite") },
                modifier = Modifier.fillMaxWidth(),
                isError = fechaLimite.isBlank()
            )

            if (errorMensaje != null) {
                Text(errorMensaje!!, color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (cliente.isBlank() || productosSeleccionados.isEmpty() || fechaLimite.isBlank()) {
                        errorMensaje = "Por favor, complete todos los campos obligatorios."
                        return@Button
                    }
                    scope.launch {
                        try {
                            val nuevoPedido = Pedido(
                                id = "",
                                cliente = cliente,
                                productos = productosSeleccionados.map {
                                    "${it.nombre} - ${it.cantidad} u - ${it.tamano} personas"
                                },
                                cantidad = productosSeleccionados.sumOf { it.cantidad.toIntOrNull() ?: 0 },
                                estado = "Pendiente",
                                fechaLimite = fechaLimite
                            )
                            viewModel.agregarPedido(nuevoPedido)
                            onPedidoAgregado()
                        } catch (e: Exception) {
                            errorMensaje = "Error al agregar pedido: ${e.message}"
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Agregar Pedido")
            }
        }
    }
}
