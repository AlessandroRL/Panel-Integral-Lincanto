package com.example.paneldecontrolreposteria.ui.productos

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.paneldecontrolreposteria.model.IngredienteDetalle
import com.example.paneldecontrolreposteria.model.Producto
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

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    val utensilios = utensiliosTexto.split("\n").map { it.trim() }.filter { it.isNotEmpty() }
                    val productoEditado = Producto(
                        nombre = nombre.trim(),
                        ingredientes = ingredientes,
                        preparacion = preparacion.trim().ifBlank { null },
                        utensilios = if (utensilios.isNotEmpty()) utensilios else null,
                        tips = tips.trim().ifBlank { null }
                    )
                    onGuardar(productoEditado)
                    onDismiss()
                }
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        },
        title = { Text("Editar Producto") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre") },
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))

                Text("Ingredientes:")
                ingredientes.forEachIndexed { index, ingrediente ->
                    Spacer(Modifier.height(8.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp)
                    ) {
                        var expanded by remember { mutableStateOf(false) }
                        var selectedNombre by remember { mutableStateOf(ingrediente.nombre) }

                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded }
                        ) {
                            OutlinedTextField(
                                value = selectedNombre,
                                onValueChange = { selectedNombre = it },
                                label = { Text("Ingrediente") },
                                readOnly = true,
                                modifier = Modifier.fillMaxWidth().menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                ingredientesDisponibles.forEach { ing ->
                                    DropdownMenuItem(
                                        text = { Text(ing.nombre) },
                                        onClick = {
                                            selectedNombre = ing.nombre
                                            ingredientes[index] = ingredientes[index].copy(nombre = ing.nombre)
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(4.dp))

                        OutlinedTextField(
                            value = ingrediente.cantidad.toString(),
                            onValueChange = {
                                val cantidad = it.toDoubleOrNull() ?: 0.0
                                ingredientes[index] = ingredientes[index].copy(cantidad = cantidad)
                            },
                            label = { Text("Cantidad") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(4.dp))

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
                                label = { Text("Unidad") },
                                modifier = Modifier.fillMaxWidth().menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = unidadExpanded,
                                onDismissRequest = { unidadExpanded = false }
                            ) {
                                unidades.forEach { unidad ->
                                    DropdownMenuItem(
                                        text = { Text(unidad) },
                                        onClick = {
                                            ingredientes[index] = ingredientes[index].copy(unidad = unidad)
                                            unidadExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(4.dp))

                        OutlinedTextField(
                            value = ingrediente.observacion ?: "",
                            onValueChange = {
                                ingredientes[index] = ingredientes[index].copy(observacion = it.ifBlank { null })
                            },
                            label = { Text("Observación") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(4.dp))

                        IconButton(
                            onClick = { ingredientes.removeAt(index) },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

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
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Agregar Ingrediente")
                }

                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = preparacion,
                    onValueChange = { preparacion = it },
                    label = { Text("Preparación (opcional)") },
                    modifier = Modifier.height(120.dp)
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = utensiliosTexto,
                    onValueChange = { utensiliosTexto = it },
                    label = { Text("Utensilios (uno por línea, opcional)") },
                    modifier = Modifier.height(100.dp)
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = tips,
                    onValueChange = { tips = it },
                    label = { Text("Tips (opcional)") },
                    modifier = Modifier.height(80.dp)
                )
            }
        }
    )
}