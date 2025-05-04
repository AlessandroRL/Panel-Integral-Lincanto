package com.example.paneldecontrolreposteria.ui.productos

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.paneldecontrolreposteria.model.Producto

@Composable
fun DialogEditarProducto(
    producto: Producto,
    onDismiss: () -> Unit,
    onGuardar: (Producto) -> Unit
) {
    var nombre by remember { mutableStateOf(producto.nombre) }
    var preparacion by remember { mutableStateOf(producto.preparacion ?: "") }
    var tips by remember { mutableStateOf(producto.tips ?: "") }

    val ingredientesState = remember {
        mutableStateListOf<String>().apply { addAll(producto.ingredientes)
        }
    }

    val utensiliosState = remember {
        mutableStateListOf<String>().apply { addAll(producto.utensilios ?: emptyList()) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = {
                val productoEditado = Producto(
                    nombre = nombre.trim(),
                    ingredientes = ingredientesState.toList(),
                    preparacion = preparacion.trim().ifEmpty { null },
                    utensilios = utensiliosState.ifEmpty { null },
                    tips = tips.trim().ifEmpty { null }
                )
                onGuardar(productoEditado)
            }) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        },
        title = { Text("Editar Producto") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("Ingredientes:")
                LazyColumn {
                    itemsIndexed(ingredientesState) { grupoIndex, grupo ->
                        Column(modifier = Modifier.padding(start = 8.dp)) {
                            Text("Grupo ${grupoIndex + 1}")
                            grupo.forEachIndexed { i, item ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    OutlinedTextField(
                                        value = ingredientesState[i],
                                        onValueChange = { new -> ingredientesState[i] = new },
                                        label = { Text("Ingrediente ${i + 1}") },
                                        modifier = Modifier.weight(1f).padding(vertical = 2.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    IconButton(onClick = { ingredientesState.removeAt(i) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Eliminar ingrediente")
                                    }
                                }
                            }

                            TextButton(onClick = { ingredientesState.add("") }) {
                                Icon(Icons.Default.Add, contentDescription = "Agregar ingrediente")
                                Spacer(Modifier.width(4.dp))
                                Text("Agregar Ingrediente")
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    item {
                        TextButton(
                            onClick = {
                                ingredientesState.add(mutableListOf("").toString())
                            },
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Icon(Icons.Default.AddCircle, contentDescription = "Agregar grupo")
                            Spacer(Modifier.width(4.dp))
                            Text("Agregar Grupo de Ingredientes")
                        }
                    }
                }

                OutlinedTextField(
                    value = preparacion,
                    onValueChange = { preparacion = it },
                    label = { Text("Preparación") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = utensiliosState.joinToString(", "),
                    onValueChange = { input ->
                        utensiliosState.clear()
                        utensiliosState.addAll(input.split(",").map { it.trim() }.filter { it.isNotEmpty() })
                    },
                    label = { Text("Utensilios") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = tips,
                    onValueChange = { tips = it },
                    label = { Text("Tips o notas") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    )
}