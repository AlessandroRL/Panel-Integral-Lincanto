package com.example.paneldecontrolreposteria.screens

import android.content.pm.PackageManager
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
import android.Manifest
import androidx.compose.material3.HorizontalDivider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionPedidoScreen(navController: NavHostController, viewModel: PedidoViewModel) {
    val pedidos by viewModel.pedidos.collectAsState()
    val scope = rememberCoroutineScope()

    var pedidoAEliminar by remember { mutableStateOf<Pedido?>(null) }
    var mostrarDialogo by remember { mutableStateOf(false) }

    val context = LocalContext.current

    var filtroMenuExpandido by remember { mutableStateOf(false) }
    val filtrosSeleccionados = remember { mutableStateListOf<String>() }
    var ordenFecha by remember { mutableStateOf("Ninguno") }
    var ordenAscendente by remember { mutableStateOf(true) }

    val pedidosFiltrados = pedidos
        .filter {
            if (filtrosSeleccionados.isEmpty() || "Todos" in filtrosSeleccionados) {
                true
            } else {
                it.estado in filtrosSeleccionados
            }
        }
        .let {
            when (ordenFecha) {
                "Fecha de registro" -> if (ordenAscendente) it.sortedBy { pedido -> pedido.fechaRegistro } else it.sortedByDescending { pedido -> pedido.fechaRegistro }
                "Fecha límite" -> if (ordenAscendente) it.sortedBy { pedido -> pedido.fechaLimite } else it.sortedByDescending { pedido -> pedido.fechaLimite }
                else -> it
            }
        }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Pedidos") },
                actions = {
                    Box {
                        IconButton(onClick = { filtroMenuExpandido = true }) {
                            Icon(Icons.Default.FilterList, contentDescription = "Filtrar")
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
                                            if (estado in filtrosSeleccionados) {
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Icon(Icons.Default.Check, contentDescription = "Seleccionado")
                                            }
                                        }
                                    },
                                    onClick = {
                                        if (estado == "Todos") {
                                            filtrosSeleccionados.clear()
                                            filtrosSeleccionados.add("Todos")
                                        } else {
                                            if ("Todos" in filtrosSeleccionados) {
                                                filtrosSeleccionados.remove("Todos")
                                            }
                                            if (estado == "Pendiente" && "Listo para entrega" in filtrosSeleccionados) {
                                                filtrosSeleccionados.remove("Listo para entrega")
                                            } else if (estado == "Listo para entrega" && "Pendiente" in filtrosSeleccionados) {
                                                filtrosSeleccionados.remove("Pendiente")
                                            }
                                            if (estado in filtrosSeleccionados) {
                                                filtrosSeleccionados.remove(estado)
                                            } else {
                                                filtrosSeleccionados.add(estado)
                                            }
                                        }
                                        filtroMenuExpandido = false
                                    }
                                )
                            }
                            HorizontalDivider(modifier = Modifier.fillMaxWidth())
                            listOf("Ninguno", "Fecha de registro", "Fecha límite").forEach { orden ->
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            horizontalArrangement = Arrangement.Start,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("$orden${if (orden != "Ninguno") " (${if (ordenAscendente) "Ascendente" else "Descendente"})" else ""}")
                                            if (ordenFecha == orden) {
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Icon(Icons.Default.Check, contentDescription = "Seleccionado")
                                            }
                                        }
                                    },
                                    onClick = {
                                        if (orden == "Ninguno") {
                                            ordenFecha = "Ninguno"
                                        } else {
                                            if (ordenFecha == orden) {
                                                ordenAscendente = !ordenAscendente
                                            } else {
                                                ordenFecha = orden
                                                ordenAscendente = true
                                            }
                                        }
                                        filtroMenuExpandido = false
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
                            navController.navigate("asistenteVirtual?activarEscuchaInicial=true")

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
                        Text("Productos:")
                        pedido.productos.forEach { producto ->
                            Text("- ${producto.nombre} (${producto.cantidad} u, ${producto.tamano} personas)")
                        }
                        Text("Estado: ${pedido.estado}")
                        Text("Fecha de Registro: ${pedido.fechaRegistro}")
                        Text("Fecha Límite: ${pedido.fechaLimite}")

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Button(
                                onClick = {
                                    scope.launch {
                                        val nuevoEstado = if (pedido.estado == "Listo para entrega") "Pendiente" else "Listo para entrega"
                                        viewModel.editarPedido(
                                            pedido.copy(estado = nuevoEstado)
                                        ) { result ->
                                            if (result) {
                                                Toast.makeText(context, "Pedido actualizado", Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(context, "Error al actualizar el pedido", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (pedido.estado == "Listo para entrega") Color.Gray else MaterialTheme.colorScheme.primary,
                                    contentColor = if (pedido.estado == "Listo para entrega") Color.LightGray else MaterialTheme.colorScheme.onPrimary
                                )
                            ) {
                                Text(if (pedido.estado == "Listo para entrega") "Revertir estado" else "Marcar como Entregado")
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
                            Toast.makeText(context, "Pedido eliminado", Toast.LENGTH_SHORT).show()
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