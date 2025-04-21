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
import androidx.compose.material.icons.filled.Delete
import androidx.navigation.NavHostController
import androidx.compose.ui.graphics.Color
import com.example.paneldecontrolreposteria.model.Pedido
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionPedidoScreen(navController: NavHostController, viewModel: PedidoViewModel) {
    val pedidos by viewModel.pedidos.collectAsState()
    val scope = rememberCoroutineScope()

    var pedidoAEliminar by remember { mutableStateOf<Pedido?>(null) }
    var mostrarDialogo by remember { mutableStateOf(false) }

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
                        Text("Tamaño (Cantidad de personas): ${pedido.tamano}")

                        val formatoFecha = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }
                        val fechaFormateada = remember(pedido.fechaRegistro) { formatoFecha.format(pedido.fechaRegistro) }

                        Text("Fecha de Registro: $fechaFormateada", style = MaterialTheme.typography.bodyMedium)
                        Text("Fecha Limite: ${pedido.fechaLimite}")

                        var botonDeshabilitado by remember { mutableStateOf(pedido.estado == "Listo para entrega") }
                        val scope = rememberCoroutineScope()

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
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

                            Button(
                                onClick = {
                                    navController.navigate("editarPedido/${pedido.id}")
                                }
                            ) {
                                Text("Editar")
                            }

                            IconButton(
                                onClick = {
                                    pedidoAEliminar = pedido
                                    mostrarDialogo = true
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Eliminar Pedido",
                                    tint = Color.Red
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (mostrarDialogo && pedidoAEliminar != null) {
        AlertDialog(
            onDismissRequest = { mostrarDialogo = false },
            title = { Text("Eliminar Pedido") },
            text = { Text("¿Estás seguro de que quieres eliminar este pedido? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            viewModel.eliminarPedido(pedidoAEliminar!!.id)
                            mostrarDialogo = false
                        }
                    }
                ) {
                    Text("Eliminar", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogo = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
