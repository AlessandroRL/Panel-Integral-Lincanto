package com.example.paneldecontrolreposteria.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.paneldecontrolreposteria.viewmodel.PedidoViewModel
import kotlinx.coroutines.launch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.navigation.NavHostController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionPedidoScreen(navController: NavHostController, viewModel: PedidoViewModel) {
    val pedidos by viewModel.pedidos.collectAsState()
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Gestión de Pedidos") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("agregarPedido") }) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Agregar Pedido")
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            items(pedidos) { pedido ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Cliente: ${pedido.cliente}", style = MaterialTheme.typography.titleMedium)
                        Text("Productos: ${pedido.productos.joinToString(", ")}")
                        Text("Estado: ${pedido.estado}")
                        Text("Fecha de Registro: ${pedido.fecha}", style = MaterialTheme.typography.bodyMedium)

                        var botonDeshabilitado by remember { mutableStateOf(pedido.estado == "Listo para entrega") }
                        val scope = rememberCoroutineScope()

                        Button(
                            onClick = {
                                Log.d("Boton", "Se presionó el botón para cambiar estado")
                                botonDeshabilitado = true
                                scope.launch {
                                    val success = viewModel.actualizarEstadoPedido(pedido.id, "Listo para entrega")
                                    if (!success) {
                                        botonDeshabilitado = false
                                    }
                                }
                            },
                            enabled = !botonDeshabilitado
                        ) {
                            Text(if (botonDeshabilitado) "Listo para entrega" else "Marcar como Entregado")
                        }
                    }
                }
            }
        }
    }
}
