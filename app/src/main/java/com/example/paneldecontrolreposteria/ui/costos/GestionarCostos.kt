package com.example.paneldecontrolreposteria.ui.costos

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.paneldecontrolreposteria.model.ProductoCosto
import com.example.paneldecontrolreposteria.ui.asistente.AsistenteButtonFloating
import com.example.paneldecontrolreposteria.viewmodel.ProductoCostoViewModel

@SuppressLint("DefaultLocale")
@Composable
fun GestionarCostos(
    viewModel: ProductoCostoViewModel = viewModel()
) {
    var mostrarDialogoPlantilla by remember { mutableStateOf(false) }
    var mostrarDialogoEditar by remember { mutableStateOf(false) }
    var productoEditando by remember { mutableStateOf<ProductoCosto?>(null) }
    var productoSeleccionado by remember { mutableStateOf<ProductoCosto?>(null) }

    val productosCosto by viewModel.productosCosto.collectAsState()
    val cargando by viewModel.cargando.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.cargarProductosCosto()
    }

    Scaffold { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxSize()
            ) {
                Text("Gestión de Costos", style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(16.dp))

                if (cargando) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                } else if (productosCosto.isEmpty()) {
                    Text("No hay productos registrados.")
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f)
                    ) {
                        items(productosCosto) { producto ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable {
                                        productoSeleccionado = producto
                                    },
                                shape = RoundedCornerShape(12.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(producto.nombre, style = MaterialTheme.typography.titleMedium)
                                        Text("Costo total: $${String.format("%.2f", producto.costoTotal)}")
                                    }
                                    IconButton(onClick = {
                                        productoEditando = producto
                                        mostrarDialogoEditar = true
                                    }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Editar")
                                    }
                                    IconButton(onClick = {
                                        viewModel.eliminarProductoCosto(producto.nombre)
                                    }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            productoEditando = ProductoCosto(nombre = "", ingredientes = mutableMapOf())
                            mostrarDialogoEditar = true
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Nuevo", modifier = Modifier.padding(end = 8.dp))
                        Text("Desde cero")
                    }

                    Button(
                        onClick = { mostrarDialogoPlantilla = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Plantilla", modifier = Modifier.padding(end = 8.dp))
                        Text("Desde plantilla")
                    }
                }
            }

            AsistenteButtonFloating(
                currentTabIndex = 1,
                onMicClick = { /* Acción al hacer clic en el micrófono */ },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 88.dp)
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
}