package com.example.paneldecontrolreposteria.ui.costos

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.paneldecontrolreposteria.model.ProductoCosto
import com.example.paneldecontrolreposteria.viewmodel.ProductoCostoViewModel
import java.util.*
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import com.example.paneldecontrolreposteria.model.IngredienteCosto

@SuppressLint("MutableCollectionMutableState", "DefaultLocale")
@Composable
fun DialogEditarProductoCostos(
    productoOriginal: ProductoCosto,
    onDismiss: () -> Unit,
    onGuardar: (ProductoCosto) -> Unit,
    viewModel: ProductoCostoViewModel
) {
    LaunchedEffect(Unit) {
        viewModel.cargarIngredientesBase()
    }

    var nombreProducto by remember { mutableStateOf(productoOriginal.nombre) }
    val ingredientes = remember {
        mutableStateMapOf<String, IngredienteCosto>().apply { putAll(productoOriginal.ingredientes) }
    }
    var mostrarAgregarIngrediente by remember { mutableStateOf(false) }
    var ingredienteAEliminar by remember { mutableStateOf<Pair<String, IngredienteCosto>?>(null) }
    var mostrarEquivalencias by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val costoTotal by remember {
        derivedStateOf {
            ingredientes.values.sumOf { it.costoTotal }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Editar costos de producto",
                    style = MaterialTheme.typography.titleMedium
                )
                IconButton(onClick = { mostrarEquivalencias = true }) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Mostrar equivalencias"
                    )
                }
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = nombreProducto,
                        onValueChange = { nombreProducto = it },
                        label = { Text("Nombre del producto") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )
                }

                item {
                    Button(
                        onClick = { mostrarAgregarIngrediente = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Agregar ingrediente personalizado")
                    }

                    if (mostrarAgregarIngrediente) {
                        AgregarIngredienteCostoComposable(
                            viewModel = viewModel,
                            onAgregar = { nuevoIngrediente ->
                                val nuevoId = UUID.randomUUID().toString()
                                ingredientes[nuevoId] = nuevoIngrediente
                                mostrarAgregarIngrediente = false
                            },
                            onCancelar = {
                                mostrarAgregarIngrediente = false
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }

                items(items = ingredientes.toList(), key = { it.first }) { (id, ingrediente) ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    ) {
                        Text("Ingrediente: ${ingrediente.nombre}")
                        OutlinedTextField(
                            value = ingrediente.cantidad.toString(),
                            onValueChange = { nuevaCantidad ->
                                nuevaCantidad.toDoubleOrNull()?.let { cantidad ->
                                    val actualizado = ingrediente.copy(
                                        cantidad = cantidad,
                                        costoTotal = cantidad * ingrediente.costoUnidad
                                    )
                                    ingredientes[id] = actualizado
                                }
                            },
                            label = { Text("Cantidad (${ingrediente.unidad})") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = "Costo estimado: $${String.format("%.2f", ingrediente.costoTotal)}",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        IconButton(onClick = { ingredienteAEliminar = id to ingrediente }) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Eliminar")
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Costo total estimado: $${String.format("%.2f", costoTotal)}")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val productoEditado = ProductoCosto(
                    nombre = nombreProducto,
                    fechaCreacion = productoOriginal.fechaCreacion,
                    ingredientes = ingredientes,
                    costoTotal = costoTotal
                )
                onGuardar(productoEditado)
                Toast.makeText(
                    context,
                    "Producto editado satisfactoriamente",
                    Toast.LENGTH_SHORT
                ).show()
                onDismiss()
            }) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )

    ingredienteAEliminar?.let { (id, ingrediente) ->
        AlertDialog(
            onDismissRequest = { ingredienteAEliminar = null },
            title = { Text("Eliminar Ingrediente") },
            text = { Text("¿Estás seguro de que quieres eliminar el ingrediente \"${ingrediente.nombre}\"?") },
            confirmButton = {
                TextButton(onClick = {
                    ingredientes.remove(id)
                    ingredienteAEliminar = null
                }) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { ingredienteAEliminar = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (mostrarEquivalencias) {
        AlertDialog(
            onDismissRequest = { mostrarEquivalencias = false },
            confirmButton = {
                TextButton(onClick = { mostrarEquivalencias = false }) {
                    Text("Cerrar")
                }
            },
            title = { Text("Equivalencias comunes") },
            text = {
                Column {
                    Text("🥄 1 cucharada = 15 ml (líquido) / 10-12 gr (sólido)")
                    Text("🧂 1 cucharadita = 5 ml (líquido) / 3-5 gr (sólido)")
                    Text("🍵 1 taza = 240 ml (líquido) / 120-130 gr (harina/azúcar)")
                    Text("🫶 1 pizca ≈ 0.3 gramos (solo sólidos)")
                }
            }
        )
    }
}