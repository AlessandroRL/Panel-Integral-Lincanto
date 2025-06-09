package com.example.paneldecontrolreposteria.ui.productos

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.paneldecontrolreposteria.model.IngredienteDetalle
import com.example.paneldecontrolreposteria.model.Producto
import com.example.paneldecontrolreposteria.ui.components.BusquedaIngredientesConLista
import com.example.paneldecontrolreposteria.viewmodel.IngredienteViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogEditarProducto(
    producto: Producto,
    onDismiss: () -> Unit,
    onGuardar: (Producto) -> Unit
) {
    val ingredienteViewModel: IngredienteViewModel = viewModel()
    val ingredientesDisponibles by ingredienteViewModel.ingredientes.collectAsState(emptyList())

    var nombre by remember { mutableStateOf(producto.nombre) }
    val ingredientes = remember { mutableStateListOf<IngredienteDetalle>().apply { addAll(producto.ingredientes) } }
    var preparacion by remember { mutableStateOf(producto.preparacion ?: "") }
    var utensiliosTexto by remember {
        mutableStateOf(producto.utensilios?.joinToString("\n") ?: "")
    }
    var tips by remember { mutableStateOf(producto.tips ?: "") }
    var mostrarEquivalencias by remember { mutableStateOf(false) }
    var ingredienteAEliminar by remember { mutableStateOf<IngredienteDetalle?>(null) }

    val isDarkTheme = isSystemInDarkTheme()
    val textColor = if (isDarkTheme) Color.White else Color.DarkGray
    val cardColor = if (isDarkTheme) Color.DarkGray else Color.White
    val gold = Color(0xFFC7A449)

    val context = LocalContext.current

    if (mostrarEquivalencias) {
        AlertDialog(
            onDismissRequest = { mostrarEquivalencias = false },
            confirmButton = {
                TextButton(onClick = { mostrarEquivalencias = false }) {
                    Text("Cerrar", color = gold)
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
            containerColor = cardColor
        )
    }

    if (ingredienteAEliminar != null) {
        AlertDialog(
            onDismissRequest = { ingredienteAEliminar = null },
            title = { Text("Eliminar Ingrediente", color = textColor, style = MaterialTheme.typography.titleLarge) },
            text = { Text("¿Estás seguro de que quieres eliminar este ingrediente?", color = textColor, style = MaterialTheme.typography.bodyLarge) },
            confirmButton = {
                TextButton(
                    onClick = {
                        ingredientes.remove(ingredienteAEliminar)
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

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    val utensilios = utensiliosTexto
                        .split("\n")
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }

                    val productoEditado = Producto(
                        nombre = nombre.trim(),
                        ingredientes = ingredientes.toList(),
                        preparacion = preparacion.trim().ifBlank { null },
                        utensilios = if (utensilios.isNotEmpty()) utensilios else null,
                        tips = tips.trim().ifBlank { null }
                    )
                    onGuardar(productoEditado)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = gold),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Guardar", color = textColor, style = MaterialTheme.typography.bodyLarge)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                border = BorderStroke(1.dp, Color.Red),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Cancelar", color = Color.Red, style = MaterialTheme.typography.bodyLarge)
            }
        },
        title = { Text("Editar Producto", color = textColor, style = MaterialTheme.typography.titleLarge) },
        text = {
            Box {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it },
                        label = { Text("Nombre", color = textColor) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(Modifier.height(16.dp))

                    Text("Ingredientes del Producto", style = MaterialTheme.typography.titleMedium, color = textColor)

                    Spacer(Modifier.height(8.dp))

                    ingredientes.forEachIndexed { index, ingrediente ->
                        BusquedaIngredientesConLista(
                            ingredientes = ingredientesDisponibles.map { it.nombre },
                            ingredienteSeleccionado = ingrediente.nombre,
                            onSeleccionarIngrediente = { nuevoNombre ->
                                val unidadPorDefecto = ingredientesDisponibles
                                    .find { it.nombre == nuevoNombre }
                                    ?.unidad ?: ""

                                ingredientes[index] = ingrediente.copy(
                                    nombre = nuevoNombre,
                                    unidad = unidadPorDefecto
                                )
                            }
                        )

                        Spacer(Modifier.height(8.dp))

                        OutlinedTextField(
                            value = ingrediente.cantidad.toString(),
                            onValueChange = {
                                val cantidad = it.toDoubleOrNull() ?: 0.0
                                ingredientes[index] = ingredientes[index].copy(cantidad = cantidad)
                            },
                            label = { Text("Cantidad", color = textColor) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(Modifier.height(8.dp))

                        var unidadExpanded by remember { mutableStateOf(false) }
                        val unidades = listOf("gr", "ml", "unidad")

                        ExposedDropdownMenuBox(
                            expanded = unidadExpanded,
                            onExpandedChange = { unidadExpanded = !unidadExpanded }
                        ) {
                            OutlinedTextField(
                                value = ingrediente.unidad,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Unidad", color = textColor) },
                                modifier = Modifier.fillMaxWidth().menuAnchor(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = unidadExpanded,
                                onDismissRequest = { unidadExpanded = false }
                            ) {
                                unidades.forEach { unidad ->
                                    DropdownMenuItem(
                                        text = { Text(unidad, color = textColor, style = MaterialTheme.typography.bodyLarge) },
                                        onClick = {
                                            ingredientes[index] = ingredientes[index].copy(unidad = unidad)
                                            unidadExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        OutlinedTextField(
                            value = ingrediente.observacion ?: "",
                            onValueChange = {
                                ingredientes[index] = ingredientes[index].copy(observacion = it.ifBlank { null })
                            },
                            label = { Text("Observación (opcional)", color = textColor) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(Modifier.height(8.dp))

                        IconButton(
                            onClick = { ingredienteAEliminar = ingrediente },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.Red)
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    Button(
                        onClick = {
                            ingredientes.add(
                                IngredienteDetalle(
                                    nombre = "",
                                    unidad = "gr",
                                    cantidad = 0.0,
                                    observacion = null
                                )
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = gold),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Agregar Ingrediente", color = textColor, style = MaterialTheme.typography.bodyLarge)
                    }

                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = preparacion,
                        onValueChange = { preparacion = it },
                        label = { Text("Preparación (opcional)", color = textColor) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = utensiliosTexto,
                        onValueChange = { utensiliosTexto = it },
                        label = { Text("Utensilios (uno por línea, opcional)", color = textColor) },
                        modifier = Modifier.height(100.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = tips,
                        onValueChange = { tips = it },
                        label = { Text("Tips (opcional)", color = textColor) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(Modifier.height(32.dp))
                }

                FloatingActionButton(
                    onClick = { mostrarEquivalencias = true },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp),
                    containerColor = gold
                ) {
                    Icon(Icons.Default.Info, contentDescription = "Equivalencias", tint = textColor)
                }
            }
        },
        containerColor = cardColor
    )
}