package com.example.paneldecontrolreposteria.ui.costos

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.paneldecontrolreposteria.model.Ingrediente
import com.example.paneldecontrolreposteria.model.IngredienteCosto
import com.example.paneldecontrolreposteria.model.ProductoCosto
import com.example.paneldecontrolreposteria.viewmodel.ProductoCostoViewModel
import java.text.SimpleDateFormat
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun GestionarCostos(costoViewModel: ProductoCostoViewModel) {
    val viewModel: ProductoCostoViewModel = viewModel()
    val productos by viewModel.productosCosto.collectAsState()
    val context = LocalContext.current
    var mostrarDialogo by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.cargarProductosCosto()
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Productos con Costo", style = MaterialTheme.typography.titleLarge)
            Button(onClick = { mostrarDialogo = true }) {
                Text("Agregar")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(productos) { producto ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Nombre: ${producto.nombre}", style = MaterialTheme.typography.titleMedium)
                        Text("Costo total: $${producto.costoTotal}")
                        Text("Fecha: ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(producto.fechaCreacion)}")
                    }
                }
            }
        }
    }

    val ingredientesDisponibles by viewModel.ingredientes.collectAsState()

    if (mostrarDialogo) {
        DialogAgregarProductoCosto(
            ingredientesDisponibles = ingredientesDisponibles,
            onDismiss = { mostrarDialogo = false },
            onGuardar = { nuevoProducto ->
                viewModel.agregarProductoCosto(nuevoProducto) { exito ->
                    mostrarDialogo = false
                }
            }
        )
    }
}

@Composable
fun DialogAgregarProductoCosto(
    onDismiss: () -> Unit,
    onGuardar: (ProductoCosto) -> Unit,
    ingredientesDisponibles: List<Ingrediente>,
) {
    var nombreProducto by remember { mutableStateOf("") }
    val ingredientesSeleccionados = remember { mutableStateMapOf<String, IngredienteCosto>() }

    val totalCosto = ingredientesSeleccionados.values.sumOf { it.costoTotal }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = {
                if (nombreProducto.isNotBlank()) {
                    onGuardar(
                        ProductoCosto(
                            nombre = nombreProducto,
                            ingredientes = ingredientesSeleccionados.toMap(),
                            costoTotal = totalCosto,
                            fechaCreacion = Date()
                        )
                    )
                }
            }) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        },
        title = { Text("Nuevo Producto de Costo") },
        text = {
            Column {
                OutlinedTextField(
                    value = nombreProducto,
                    onValueChange = { nombreProducto = it },
                    label = { Text("Nombre del producto") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))

                LazyColumn {
                    items(ingredientesDisponibles) { ingrediente ->
                        var cantidad by remember { mutableStateOf("") }

                        Column(modifier = Modifier.padding(8.dp)) {
                            Text("${ingrediente.nombre} (${ingrediente.unidad}) - ${ingrediente.costoUnidad} c/u")

                            OutlinedTextField(
                                value = cantidad,
                                onValueChange = {
                                    cantidad = it
                                    val cantidadNum = it.toDoubleOrNull() ?: 0.0
                                    val costoTotal = cantidadNum * ingrediente.costoUnidad

                                    if (cantidadNum > 0) {
                                        ingredientesSeleccionados[ingrediente.nombre] = IngredienteCosto(
                                            nombre = ingrediente.nombre,
                                            unidad = ingrediente.unidad,
                                            cantidad = cantidadNum,
                                            costoUnidad = ingrediente.costoUnidad,
                                            costoTotal = costoTotal
                                        )
                                    } else {
                                        ingredientesSeleccionados.remove(ingrediente.nombre)
                                    }
                                },
                                label = { Text("Cantidad") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
                Text("Costo total estimado: $totalCosto", fontWeight = FontWeight.Bold)
            }
        }
    )
}