package com.example.paneldecontrolreposteria.ui.productos

import android.annotation.SuppressLint
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import com.example.paneldecontrolreposteria.model.Producto
import com.example.paneldecontrolreposteria.viewmodel.ProductoViewModel

@RequiresApi(Build.VERSION_CODES.O)
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
        val productosOrdenados = listaProductos.sortedBy { it.nombre.lowercase() }
        var mostrarDialogoEditar by remember { mutableStateOf(false) }
        var productoParaEditar by remember { mutableStateOf<Producto?>(null) }
        val scrollState = rememberLazyListState()

        LazyColumn(state = scrollState) {
            items(productosOrdenados) { producto ->
                Card(modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)) {

                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "🍰 ${producto.nombre}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            "Ingredientes:",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        producto.ingredientes.forEachIndexed { index, ingrediente ->
                            Text("${index + 1}. $ingrediente", style = MaterialTheme.typography.bodyMedium)
                        }

                        producto.preparacion?.let {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Preparación:",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                it,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        producto.utensilios?.let {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Utensilios:",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(it.joinToString("\n"), style = MaterialTheme.typography.bodyMedium)
                        }

                        producto.tips?.let {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Tips:",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(it, style = MaterialTheme.typography.bodyMedium)
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
                                viewModel.eliminarProducto(producto.id)
                            }) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Eliminar")
                            }
                        }
                    }
                }
            }
        }

        val context = LocalContext.current

        if (mostrarDialogoEditar && productoParaEditar != null) {
            DialogEditarProducto(
                producto = productoParaEditar!!,
                onDismiss = { mostrarDialogoEditar = false },
                onGuardar = { productoEditado ->
                    viewModel.actualizarProducto(
                        nombreOriginal = productoParaEditar!!.nombre,
                        productoEditado = productoEditado
                    ) { exito ->
                        if (exito) {
                            Toast.makeText(context, "Producto actualizado", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Error al actualizar", Toast.LENGTH_SHORT).show()
                        }
                        mostrarDialogoEditar = false
                    }
                }
            )
        }
    }

    if (showDialog) {
        DialogoAgregarProducto(
            onDismiss = { showDialog = false },
            onAgregar = { producto ->
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
    onAgregar: (Producto) -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var ingredientesTexto by remember { mutableStateOf("") }
    var preparacion by remember { mutableStateOf("") }
    var utensiliosTexto by remember { mutableStateOf("") }
    var tips by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    val ingredientes = ingredientesTexto
                        .split("\n")
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }

                    val utensilios = utensiliosTexto
                        .split("\n")
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }

                    val producto = Producto(
                        nombre = nombre.trim(),
                        ingredientes = ingredientes,
                        preparacion = preparacion.trim(),
                        utensilios = utensilios,
                        tips = tips.trim()
                    )
                    onAgregar(producto)
                    onDismiss()
                }
            ) {
                Text("Agregar")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        },
        title = { Text("Nuevo Producto") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre (usa guión si es subproducto)") },
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