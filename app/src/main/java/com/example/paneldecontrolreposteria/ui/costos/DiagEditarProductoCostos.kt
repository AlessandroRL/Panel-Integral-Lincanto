package com.example.paneldecontrolreposteria.ui.costos

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.AlertDialog
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.Checkbox
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.OutlinedTextField
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.Text
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.paneldecontrolreposteria.model.Ingrediente
import com.example.paneldecontrolreposteria.model.IngredienteCosto
import com.example.paneldecontrolreposteria.model.ProductoCosto

@Composable
fun DialogEditarProductoCosto(
    producto: ProductoCosto,
    ingredientesDisponibles: List<Ingrediente>,
    onDismiss: () -> Unit,
    onGuardar: (ProductoCosto) -> Unit
) {
    var nombre by remember { mutableStateOf(producto.nombre) }
    val ingredientesSeleccionados = remember { mutableStateMapOf<String, IngredienteCosto>() }

    // Inicializar con los valores actuales
    LaunchedEffect(Unit) {
        ingredientesSeleccionados.putAll(producto.ingredientes)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Producto de Costo") },
        text = {
            Column {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre del producto") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Ingredientes:")
                ingredientesDisponibles.forEach { ingrediente ->
                    var cantidadTexto by remember {
                        mutableStateOf(
                            ingredientesSeleccionados[ingrediente.nombre]?.cantidad?.toString() ?: ""
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = ingredientesSeleccionados.containsKey(ingrediente.nombre),
                            onCheckedChange = { isChecked ->
                                if (isChecked) {
                                    val cantidad = cantidadTexto.toDoubleOrNull() ?: 0.0
                                    val costoTotal = cantidad * ingrediente.costoUnidad
                                    ingredientesSeleccionados[ingrediente.nombre] =
                                        IngredienteCosto(
                                            nombre = ingrediente.nombre,
                                            unidad = ingrediente.unidad,
                                            cantidad = cantidad,
                                            costoUnidad = ingrediente.costoUnidad,
                                            costoTotal = costoTotal
                                        )
                                } else {
                                    ingredientesSeleccionados.remove(ingrediente.nombre)
                                }
                            }
                        )
                        Text(ingrediente.nombre)
                        Spacer(Modifier.width(8.dp))
                        OutlinedTextField(
                            value = cantidadTexto,
                            onValueChange = {
                                cantidadTexto = it
                                val cantidad = it.toDoubleOrNull() ?: 0.0
                                if (ingredientesSeleccionados.containsKey(ingrediente.nombre)) {
                                    val costoTotal = cantidad * ingrediente.costoUnidad
                                    ingredientesSeleccionados[ingrediente.nombre] =
                                        IngredienteCosto(
                                            nombre = ingrediente.nombre,
                                            unidad = ingrediente.unidad,
                                            cantidad = cantidad,
                                            costoUnidad = ingrediente.costoUnidad,
                                            costoTotal = costoTotal
                                        )
                                }
                            },
                            label = { Text("Cantidad (${ingrediente.unidad})") },
                            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                            modifier = Modifier.width(150.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val costoTotal = ingredientesSeleccionados.values.sumOf { it.costoTotal }
                onGuardar(
                    ProductoCosto(
                        nombre = nombre,
                        fechaCreacion = producto.fechaCreacion, // mantener fecha original
                        ingredientes = ingredientesSeleccionados.toMap(),
                        costoTotal = costoTotal
                    )
                )
            }) {
                Text("Guardar cambios")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}