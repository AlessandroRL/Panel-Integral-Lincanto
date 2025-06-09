package com.example.paneldecontrolreposteria.ui.costos

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
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

    val isDarkTheme = isSystemInDarkTheme()
    val backgroundColor = if (isDarkTheme) Color.Black else Color.White
    val textColor = if (isDarkTheme) Color.White else Color.DarkGray
    val cardColor = if (isDarkTheme) Color.DarkGray else Color.White
    val gold = Color(0xFFC7A449)

    val costoTotal by remember {
        derivedStateOf {
            ingredientes.values.sumOf { it.costoTotal }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(16.dp),
        containerColor = backgroundColor,
        title = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Editar costos de producto",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = textColor
                    )
                )
                IconButton(onClick = { mostrarEquivalencias = true }) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Mostrar equivalencias",
                        tint = gold
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
                            .padding(bottom = 12.dp),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                item {
                    Button(
                        onClick = { mostrarAgregarIngrediente = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = gold,
                            contentColor = textColor
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Agregar ingrediente personalizado", style = MaterialTheme.typography.bodyLarge)
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
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Ingrediente: ${ingrediente.nombre}",
                                style = MaterialTheme.typography.bodyMedium
                            )
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
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            Text(
                                text = "Costo estimado: $${String.format("%.2f", ingrediente.costoTotal)}",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(top = 6.dp)
                            )
                            IconButton(
                                onClick = { ingredienteAEliminar = id to ingrediente },
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Eliminar",
                                    tint = Color.Red
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Costo total estimado: $${String.format("%.2f", costoTotal)}",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = textColor
                        ),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val productoEditado = ProductoCosto(
                        nombre = nombreProducto,
                        fechaCreacion = productoOriginal.fechaCreacion,
                        ingredientes = ingredientes,
                        costoTotal = costoTotal
                    )
                    onGuardar(productoEditado)
                    Toast
                        .makeText(context, "Producto editado satisfactoriamente", Toast.LENGTH_SHORT)
                        .show()
                    onDismiss()
                },
                colors = ButtonDefaults.textButtonColors(contentColor = gold)
            ) {
                Text("Guardar", style = MaterialTheme.typography.bodyLarge)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
            ) {
                Text("Cancelar", style = MaterialTheme.typography.bodyLarge)
            }
        }
    )

    ingredienteAEliminar?.let { (id, ingrediente) ->
        AlertDialog(
            onDismissRequest = { ingredienteAEliminar = null },
            title = { Text("Eliminar Ingrediente", color = textColor, style = MaterialTheme.typography.titleLarge) },
            text = {
                Text("¿Estás seguro de que quieres eliminar el ingrediente \"${ingrediente.nombre}\"?", color = textColor, style = MaterialTheme.typography.bodyLarge)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        ingredientes.remove(id)
                        ingredienteAEliminar = null
                        Toast.makeText(context, "Ingrediente eliminado", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Eliminar", color = Color.Red, style = MaterialTheme.typography.bodyLarge)
                }
            },
            dismissButton = {
                TextButton(onClick = { ingredienteAEliminar = null }) {
                    Text("Cancelar", color = gold, style = MaterialTheme.typography.bodyLarge)
                }
            },
            containerColor = cardColor
        )
    }

    if (mostrarEquivalencias) {
        AlertDialog(
            onDismissRequest = { mostrarEquivalencias = false },
            confirmButton = {
                TextButton(onClick = { mostrarEquivalencias = false }) {
                    Text("Cerrar", color = gold, style = MaterialTheme.typography.bodyLarge)
                }
            },
            title = { Text("Equivalencias comunes", color = textColor, style = MaterialTheme.typography.titleLarge) },
            text = {
                Column {
                    Text("🥄 1 cucharada = 15 ml (líquido) / 10-12 gr (sólido)", color = textColor, style = MaterialTheme.typography.bodyLarge)
                    Text("🧂 1 cucharadita = 5 ml (líquido) / 3-5 gr (sólido)", color = textColor, style = MaterialTheme.typography.bodyLarge)
                    Text("🍵 1 taza = 240 ml (líquido) / 120-130 gr (harina/azúcar)", color = textColor, style = MaterialTheme.typography.bodyLarge)
                    Text("🫶 1 pizca ≈ 0.3 gramos (solo sólidos)", color = textColor, style = MaterialTheme.typography.bodyLarge)
                }
            },
            containerColor = cardColor,
            shape = RoundedCornerShape(16.dp)
        )
    }
}