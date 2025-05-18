package com.example.paneldecontrolreposteria.screens

import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
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
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.Alignment
import androidx.navigation.NavHostController
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.example.paneldecontrolreposteria.model.Pedido
import com.example.paneldecontrolreposteria.ui.asistente.AsistenteButtonFloating
import com.example.paneldecontrolreposteria.ui.asistente.voice.SpeechRecognizerManager
import android.Manifest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionPedidoScreen(navController: NavHostController, viewModel: PedidoViewModel, speechRecognizerManager: SpeechRecognizerManager) {
    val pedidos by viewModel.pedidos.collectAsState()
    val scope = rememberCoroutineScope()

    var pedidoAEliminar by remember { mutableStateOf<Pedido?>(null) }
    var mostrarDialogo by remember { mutableStateOf(false) }

    var filtroEstado by remember { mutableStateOf("Todos") }
    var ordenFecha by remember { mutableStateOf("Ninguno") }

    var filtroMenuExpandido by remember { mutableStateOf(false) }
    var ordenMenuExpandido by remember { mutableStateOf(false) }

    val pedidosFiltrados = pedidos
        .filter {
            when (filtroEstado) {
                "Pendiente" -> it.estado == "Pendiente"
                "Listo para entrega" -> it.estado == "Listo para entrega"
                else -> true
            }
        }
        .let {
            when (ordenFecha) {
                "Fecha de registro" -> it.sortedByDescending { pedido -> pedido.fechaRegistro }
                "Fecha límite" -> it.sortedByDescending { pedido -> pedido.fechaLimite }
                else -> it
            }
        }

    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Pedidos") },
                actions = {
                    Box {
                        IconButton(onClick = { filtroMenuExpandido = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Filtrar por estado")
                        }
                        DropdownMenu(
                            expanded = filtroMenuExpandido,
                            onDismissRequest = { filtroMenuExpandido = false }
                        ) {
                            listOf("Todos", "Pendiente", "Listo para entrega").forEach { estado ->
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            horizontalArrangement = Arrangement.Start,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(estado)
                                            if (filtroEstado == estado) {
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Icon(Icons.Default.Check, contentDescription = "Seleccionado")
                                            }
                                        }
                                    },
                                    onClick = {
                                        filtroEstado = estado
                                        filtroMenuExpandido = false
                                    }
                                )
                            }
                        }
                    }

                    Box {
                        IconButton(onClick = { ordenMenuExpandido = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Ordenar por fecha")
                        }
                        DropdownMenu(
                            expanded = ordenMenuExpandido,
                            onDismissRequest = { ordenMenuExpandido = false }
                        ) {
                            listOf("Ninguno", "Fecha de registro", "Fecha límite").forEach { orden ->
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            horizontalArrangement = Arrangement.Start,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(orden)
                                            if (ordenFecha == orden) {
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Icon(Icons.Default.Check, contentDescription = "Seleccionado")
                                            }
                                        }
                                    },
                                    onClick = {
                                        ordenFecha = orden
                                        ordenMenuExpandido = false
                                    }
                                )
                            }
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.End,
                modifier = Modifier.padding(end = 16.dp, bottom = 16.dp)
            ) {
                FloatingActionButton(onClick = { navController.navigate("agregarPedido") }) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Agregar Pedido")
                }
                AsistenteButtonFloating(
                    currentTabIndex = 0,
                    onMicClick = {
                        if (ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.RECORD_AUDIO
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            Toast.makeText(context, "🎤 Escuchando...", Toast.LENGTH_SHORT).show()
                            speechRecognizerManager.startListening()
                        } else {
                            Toast.makeText(context, "Permiso de grabación no concedido", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                )
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            items(pedidosFiltrados) { pedido ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Cliente: ${pedido.cliente}", style = MaterialTheme.typography.titleMedium)
                        Text("Productos: ${pedido.productos.joinToString(", ")}")
                        Text("Cantidad: ${pedido.cantidad}")
                        Text("Tamaño: ${pedido.tamano} personas")
                        Text("Estado: ${pedido.estado}")
                        Text("Fecha de Registro: ${pedido.fechaRegistro}")
                        Text("Fecha Límite: ${pedido.fechaLimite}")

                        var botonDeshabilitado by remember { mutableStateOf(pedido.estado == "Listo para entrega") }

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
                                        if (!success) botonDeshabilitado = false
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
                            IconButton(onClick = {
                                pedidoAEliminar = pedido
                                mostrarDialogo = true
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.Red)
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
            text = { Text("¿Estás seguro de que quieres eliminar este pedido?") },
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