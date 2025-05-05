package com.example.paneldecontrolreposteria.ui.productos

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
    var ingredientesTexto by remember {
        mutableStateOf(producto.ingredientes.joinToString("\n"))
    }
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
                    val ingredientes = ingredientesTexto.split("\n").map { it.trim() }.filter { it.isNotEmpty() }
                    val utensilios = utensiliosTexto.split("\n").map { it.trim() }.filter { it.isNotEmpty() }

                    val productoEditado = Producto(
                        nombre = nombre.trim(),
                        ingredientes = ingredientes,
                        preparacion = preparacion.trim(),
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
                OutlinedTextField(
                    value = ingredientesTexto,
                    onValueChange = { ingredientesTexto = it },
                    label = { Text("Ingredientes (uno por línea)") },
                    modifier = Modifier.height(150.dp)
                )
                Spacer(Modifier.height(8.dp))
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