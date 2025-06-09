package com.example.paneldecontrolreposteria.ui.costos

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.paneldecontrolreposteria.model.ProductoCosto
import com.example.paneldecontrolreposteria.ui.asistente.AsistenteButtonFloating
import com.example.paneldecontrolreposteria.viewmodel.ProductoCostoViewModel

@SuppressLint("DefaultLocale")
@Composable
fun GestionarCostos(
    viewModel: ProductoCostoViewModel = viewModel(),
    navController: NavController,
    context: Context
) {
    val isDarkTheme = isSystemInDarkTheme()
    var mostrarDialogoPlantilla by remember { mutableStateOf(false) }
    var mostrarDialogoEditar by remember { mutableStateOf(false) }
    var productoEditando by remember { mutableStateOf<ProductoCosto?>(null) }
    var productoSeleccionado by remember { mutableStateOf<ProductoCosto?>(null) }
    var productoAEliminar by remember { mutableStateOf<ProductoCosto?>(null) }

    val productosCosto by viewModel.productosCosto.collectAsState()
    val cargando by viewModel.cargando.collectAsState()

    val backgroundColor = if (isDarkTheme) Color.Black else Color.White
    val textColor = if (isDarkTheme) Color.White else Color.DarkGray
    val cardColor = if (isDarkTheme) Color.DarkGray else Color.White
    val gold = Color(0xFFC7A449)
    val secondary = Color(0xFF705852)
    val secondaryVariant = if (isDarkTheme) Color.White else secondary

    LaunchedEffect(Unit) {
        viewModel.cargarProductosCosto()
    }

    Scaffold { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(backgroundColor)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxSize()
            ) {
                Text(
                    "Gestión de Costos",
                    style = MaterialTheme.typography.titleLarge,
                    color = textColor
                )
                Spacer(modifier = Modifier.height(16.dp))

                if (cargando) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                } else if (productosCosto.isEmpty()) {
                    Text(
                        "No hay productos registrados.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = textColor
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f)
                    ) {
                        items(productosCosto) { producto ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                                    .border(
                                        width = 1.dp,
                                        color = gold,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        productoSeleccionado = producto
                                    },
                                colors = CardDefaults.cardColors(containerColor = cardColor),
                                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            producto.nombre,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = textColor
                                        )
                                        Text(
                                            "Costo total: $${String.format("%.2f", producto.costoTotal)}",
                                            color = textColor
                                        )
                                    }
                                    IconButton(onClick = {
                                        productoEditando = producto
                                        mostrarDialogoEditar = true
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Editar",
                                            tint = gold
                                        )
                                    }
                                    IconButton(onClick = {
                                        productoAEliminar = producto
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Eliminar",
                                            tint = Color.Red
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = {
                            productoEditando = ProductoCosto(nombre = "", ingredientes = mutableMapOf())
                            mostrarDialogoEditar = true
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(20.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = secondaryVariant,
                            contentColor = gold
                        )
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Nuevo",
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text("Desde cero", style = MaterialTheme.typography.bodyLarge)
                    }

                    Button(
                        onClick = { mostrarDialogoPlantilla = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(20.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = gold,
                            contentColor = textColor
                        )
                    ) {
                        Icon(
                            Icons.Default.ContentCopy,
                            contentDescription = "Plantilla",
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text("Desde plantilla", style = MaterialTheme.typography.bodyLarge)
                    }
                }
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
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 80.dp)
            )
        }
    }

    productoAEliminar?.let { producto ->
        AlertDialog(
            onDismissRequest = { productoAEliminar = null },
            title = {
                Text(
                    "Eliminar Producto",
                    style = MaterialTheme.typography.titleLarge,
                    color = textColor
                )
            },
            text = {
                Text(
                    "¿Estás seguro de que quieres eliminar el producto \"${producto.nombre}\"?",
                    style = MaterialTheme.typography.bodyLarge,
                    color = textColor
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.eliminarProductoCosto(producto.nombre)
                        Toast.makeText(context, "Producto eliminado", Toast.LENGTH_SHORT).show()
                        productoAEliminar = null
                    }
                ) {
                    Text("Eliminar",style = MaterialTheme.typography.bodyLarge, color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { productoAEliminar = null }) {
                    Text("Cancelar", style = MaterialTheme.typography.bodyLarge, color = gold)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    if (mostrarDialogoPlantilla) {
        DialogSeleccionarPlantillaProducto(
            onDismiss = { mostrarDialogoPlantilla = false },
            onSeleccionar = {
                productoEditando = it
                mostrarDialogoEditar = true
                mostrarDialogoPlantilla = false
            },
            viewModel = viewModel
        )
    }

    productoEditando?.let { producto ->
        if (mostrarDialogoEditar) {
            DialogEditarProductoCostos(
                productoOriginal = producto,
                onDismiss = {
                    mostrarDialogoEditar = false
                    productoEditando = null
                },
                onGuardar = { actualizado ->
                    if (productosCosto.any { it.nombre == actualizado.nombre }) {
                        viewModel.actualizarProductoCosto(actualizado)
                    } else {
                        viewModel.guardarProductoCosto(actualizado)
                    }
                    mostrarDialogoEditar = false
                    productoEditando = null
                },
                viewModel = viewModel
            )
        }
    }

    productoSeleccionado?.let {
        DialogDetalleProductoCosto(
            producto = it,
            onDismiss = { productoSeleccionado = null }
        )
    }
}