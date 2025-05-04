package com.example.paneldecontrolreposteria.ui.productos

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
    var utensilios by remember { mutableStateOf(producto.utensilios ?: "") }
    var tips by remember { mutableStateOf(producto.tips ?: "") }

    val ingredientesState = remember {
        mutableStateListOf<MutableList<String>>().apply {
            addAll(producto.ingredientes.map { it.toMutableList() })
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = {
                val productoEditado = Producto(
                    nombre = nombre.trim(),
                    ingredientes = ingredientesState.map { it.toList() },
                    preparacion = preparacion.trim().ifEmpty { null },
                    utensilios = utensilios.trim().ifEmpty { null },
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
                                OutlinedTextField(
                                    value = item,
                                    onValueChange = { new ->
                                        ingredientesState[grupoIndex][i] = new
                                    },
                                    label = { Text("Ingrediente ${i + 1}") },
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = preparacion,
                    onValueChange = { preparacion = it },
                    label = { Text("Preparación") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = utensilios,
                    onValueChange = { utensilios = it },
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