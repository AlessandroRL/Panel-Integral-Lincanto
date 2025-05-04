package com.example.paneldecontrolreposteria.ui.productos

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import com.example.paneldecontrolreposteria.model.Producto
import com.example.paneldecontrolreposteria.viewmodel.ProductoViewModel

@Composable
fun GestionarProductos(viewModel: ProductoViewModel) {
    var showDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Gestión de Productos", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = { showDialog = true }) {
            Text("Agregar Producto")
        }

        Spacer(modifier = Modifier.height(16.dp))

        val listaProductos by viewModel.productos.collectAsState()
        var mostrarDialogoEditar by remember { mutableStateOf(false) }
        var productoParaEditar by remember { mutableStateOf<Producto?>(null) }

        LazyColumn {
            items(listaProductos) { producto ->
                Card(modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)) {

                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("🍰 ${producto.nombre}", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Conjuntos de ingredientes: ${producto.ingredientes.size}")

                        producto.preparacion?.let {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Preparación: $it")
                        }

                        producto.utensilios?.let {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Utensilios: $it")
                        }

                        producto.tips?.let {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Tips: $it")
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            IconButton(onClick = {
                                productoParaEditar = producto
                                mostrarDialogoEditar = true
                            }) {
                                Icon(imageVector = Icons.Default.Edit, contentDescription = "Editar")
                            }
                            IconButton(onClick = {
                                viewModel.eliminarProducto(producto.nombre)
                            }) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Eliminar")
                            }
                        }
                    }
                }
            }
        }

        if (mostrarDialogoEditar && productoParaEditar != null) {
            DialogEditarProducto(
                producto = productoParaEditar!!,
                onDismiss = { mostrarDialogoEditar = false },
                onGuardar = { productoEditado ->
                    viewModel.actualizarProducto(productoEditado)
                }

            )
        }
    }

    if (showDialog) {
        DialogoAgregarProducto(
            onDismiss = { showDialog = false },
            onGuardar = { producto ->
                viewModel.agregarProducto(producto)
                showDialog = false
            }
        )
    }
}

@SuppressLint("MutableCollectionMutableState")
@Composable
fun DialogoAgregarProducto(
    onDismiss: () -> Unit,
    onGuardar: (Producto) -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var ingredientes1 by remember { mutableStateOf(mutableListOf<String>()) }
    var nuevoIng1 by remember { mutableStateOf("") }

    var ingredientes2 by remember { mutableStateOf(mutableListOf<String>()) }
    var nuevoIng2 by remember { mutableStateOf("") }

    var preparacion by remember { mutableStateOf("") }
    var utensilios by remember { mutableStateOf("") }
    var tips by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo Producto") },
        confirmButton = {
            Button(onClick = {
                val listaIngredientes = mutableListOf<List<String>>()
                if (ingredientes1.isNotEmpty()) listaIngredientes.add(ingredientes1)
                if (ingredientes2.isNotEmpty()) listaIngredientes.add(ingredientes2)

                onGuardar(
                    Producto(
                        nombre = nombre,
                        ingredientes = listaIngredientes.flatten(),
                        preparacion = if (preparacion.isBlank()) null else preparacion,
                        utensilios = if (utensilios.isEmpty()) null else utensilios.split(",").map { it.trim() },
                        tips = if (tips.isBlank()) null else tips
                    )
                )
            }) {
                Text("Guardar")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        },
        text = {
            Column {
                OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre") })

                Spacer(modifier = Modifier.height(8.dp))
                Text("Conjunto 1 de ingredientes:")
                ingredientes1.forEach { Text("- $it") }
                Row {
                    OutlinedTextField(
                        value = nuevoIng1,
                        onValueChange = { nuevoIng1 = it },
                        label = { Text("Ingrediente") },
                        modifier = Modifier.weight(1f)
                    )
                    Button(onClick = {
                        if (nuevoIng1.isNotBlank()) {
                            ingredientes1.add(nuevoIng1)
                            nuevoIng1 = ""
                        }
                    }) { Text("+") }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text("Conjunto 2 de ingredientes (opcional):")
                ingredientes2.forEach { Text("- $it") }
                Row {
                    OutlinedTextField(
                        value = nuevoIng2,
                        onValueChange = { nuevoIng2 = it },
                        label = { Text("Ingrediente") },
                        modifier = Modifier.weight(1f)
                    )
                    Button(onClick = {
                        if (nuevoIng2.isNotBlank()) {
                            ingredientes2.add(nuevoIng2)
                            nuevoIng2 = ""
                        }
                    }) { Text("+") }
                }

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = preparacion, onValueChange = { preparacion = it }, label = { Text("Preparación") })
                OutlinedTextField(value = utensilios, onValueChange = { utensilios = it }, label = { Text("Utensilios") })
                OutlinedTextField(value = tips, onValueChange = { tips = it }, label = { Text("Tips o notas") })
            }
        }
    )
}
