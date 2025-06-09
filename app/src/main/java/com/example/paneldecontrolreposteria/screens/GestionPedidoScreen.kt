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
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.foundation.isSystemInDarkTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionPedidoScreen(navController: NavHostController, viewModel: PedidoViewModel) {
    val isDarkTheme = isSystemInDarkTheme()
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

    val backgroundColor = if (isDarkTheme) Color.Black else Color.White
    val textColor = if (isDarkTheme) Color.White else Color.DarkGray
    val cardColor = if (isDarkTheme) Color.DarkGray else Color.White
    val gold = Color(0xFFC7A449)

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Gestión de Pedidos",
                        color = textColor,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundColor,
                    titleContentColor = textColor,
                    actionIconContentColor = gold
                ),
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
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                estado,
                                                color = textColor,
                                                style = MaterialTheme.typography.bodyLarge
                                            )
                                            if (estado in filtrosSeleccionados) {
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Icon(Icons.Default.Check, contentDescription = "Seleccionado", tint = gold)
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
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                "$orden${if (orden != "Ninguno") " (${if (ordenAscendente) "Ascendente" else "Descendente"})" else ""}",
                                                color = textColor,
                                                style = MaterialTheme.typography.bodyLarge
                                            )
                                            if (ordenFecha == orden) {
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Icon(Icons.Default.Check, contentDescription = "Seleccionado", tint = gold)
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
                FloatingActionButton(
                    onClick = { navController.navigate("agregarPedido") },
                    containerColor = gold,
                    contentColor = textColor,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Agregar Pedido")
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .border(
                            width = 1.dp,
                            color = gold,
                            shape = RoundedCornerShape(20.dp)
                        ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = cardColor)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Cliente: ${pedido.cliente}", style = MaterialTheme.typography.titleMedium, color = textColor)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Productos:", color = textColor)
                        pedido.productos.forEach { producto ->
                            Text("- ${producto.nombre} (${producto.cantidad} u, ${producto.tamano} personas)", color = textColor)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Estado: ${pedido.estado}", color = textColor)
                        Text("Fecha de Registro: ${pedido.fechaRegistro}", color = textColor)
                        Text("Fecha Límite: ${pedido.fechaLimite}", color = textColor)

                        Spacer(modifier = Modifier.height(12.dp))

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
                                            val mensaje = if (result) "Pedido actualizado" else "Error al actualizar el pedido"
                                            Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (pedido.estado == "Listo para entrega") Color.LightGray else gold,
                                    contentColor = Color.Black
                                )
                            ) {
                                Text(
                                    text = if (pedido.estado == "Listo para entrega") "Revertir estado" else "Marcar como Entregado",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color.DarkGray
                                )
                            }

                            IconButton(
                                onClick = { navController.navigate("editarPedido/${pedido.id}") }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Editar",
                                    tint = gold
                                )
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
            title = {
                Text(
                    "Eliminar Pedido",
                    color = textColor,
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Text(
                    "¿Estás seguro de que quieres eliminar este pedido?",
                    color = textColor,
                    style = MaterialTheme.typography.bodyLarge
                )
            },
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
                    Text("Eliminar", color = Color.Red, style = MaterialTheme.typography.bodyLarge)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogo = false }) {
                    Text("Cancelar", color = gold, style = MaterialTheme.typography.bodyLarge)
                }
            },
            containerColor = cardColor
        )
    }
}