package com.example.paneldecontrolreposteria.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.paneldecontrolreposteria.model.Ingrediente
import com.example.paneldecontrolreposteria.ui.costos.GestionarCostos
import com.example.paneldecontrolreposteria.viewmodel.IngredienteViewModel
import com.example.paneldecontrolreposteria.ui.productos.GestionarProductos
import com.example.paneldecontrolreposteria.viewmodel.ProductoCostoViewModel
import com.example.paneldecontrolreposteria.viewmodel.ProductoViewModel
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun GestionIngredientesScreen() {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Ingredientes", "Productos", "Costos")

    val productoViewModel: ProductoViewModel = viewModel()
    val costoViewModel: ProductoCostoViewModel = viewModel()

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTabIndex) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(title) }
                )
            }
        }

        when (selectedTabIndex) {
            0 -> GestionarIngredientes()
            1 -> GestionarProductos(productoViewModel)
            2 -> GestionarCostos(costoViewModel)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionarIngredientes() {
    val viewModel: IngredienteViewModel = viewModel()
    val ingredientes by viewModel.ingredientes.collectAsState()
    var nombre by remember { mutableStateOf("") }
    var unidad by remember { mutableStateOf("") }
    var costoUnidad by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    var unidadExpanded by remember { mutableStateOf(false) }
    val unidades = listOf("gr", "ml", "unidad")

    var ingredienteAEditar by remember { mutableStateOf<Ingrediente?>(null) }
    var mostrarDialogoEdicion by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Registrar Ingrediente", style = MaterialTheme.typography.titleLarge)

        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre") },
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
        )

        ExposedDropdownMenuBox(
            expanded = unidadExpanded,
            onExpandedChange = { unidadExpanded = !unidadExpanded }
        ) {
            OutlinedTextField(
                value = unidad,
                onValueChange = {},
                label = { Text("Unidad") },
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unidadExpanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = unidadExpanded,
                onDismissRequest = { unidadExpanded = false }
            ) {
                unidades.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            unidad = option
                            unidadExpanded = false
                        }
                    )
                }
            }
        }

        OutlinedTextField(
            value = costoUnidad,
            onValueChange = { costoUnidad = it },
            label = { Text("Costo por unidad") },
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
        )

        Button(
            onClick = {
                if (nombre.isNotBlank() && unidad.isNotBlank() && costoUnidad.toDoubleOrNull() != null) {
                    val nuevo = Ingrediente(
                        nombre = nombre,
                        unidad = unidad,
                        costoUnidad = costoUnidad.toDouble()
                    )
                    viewModel.agregarIngrediente(nuevo)
                    nombre = ""
                    unidad = ""
                    costoUnidad = ""
                }
            },
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text("Guardar Ingrediente")
        }

        Divider(modifier = Modifier.padding(vertical = 16.dp))

        Text("Ingredientes Registrados", style = MaterialTheme.typography.titleMedium)

        LazyColumn {
            items(ingredientes) { ingrediente ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Nombre: ${ingrediente.nombre}")
                            Text("Unidad: ${ingrediente.unidad}")
                            Text("Costo: ${ingrediente.costoUnidad}")
                        }
                        Row {
                            IconButton(onClick = {
                                ingredienteAEditar = ingrediente
                                mostrarDialogoEdicion = true
                            }) {
                                Icon(Icons.Default.Edit, contentDescription = "Editar")
                            }
                            IconButton(onClick = {
                                scope.launch {
                                    viewModel.eliminarIngrediente(ingrediente.nombre)
                                }
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.Red)
                            }
                        }
                    }
                }
            }
        }
    }

    if (mostrarDialogoEdicion && ingredienteAEditar != null) {
        var nuevoNombre by remember { mutableStateOf(ingredienteAEditar!!.nombre) }
        var nuevaUnidad by remember { mutableStateOf(ingredienteAEditar!!.unidad) }
        var nuevoCosto by remember { mutableStateOf(ingredienteAEditar!!.costoUnidad.toString()) }
        var editUnidadExpanded by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { mostrarDialogoEdicion = false },
            title = { Text("Editar Ingrediente") },
            text = {
                Column {
                    OutlinedTextField(value = nuevoNombre, onValueChange = { nuevoNombre = it }, label = { Text("Nombre") })
                    ExposedDropdownMenuBox(
                        expanded = editUnidadExpanded,
                        onExpandedChange = { editUnidadExpanded = !editUnidadExpanded }
                    ) {
                        OutlinedTextField(
                            value = nuevaUnidad,
                            onValueChange = {},
                            label = { Text("Unidad") },
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = editUnidadExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = editUnidadExpanded,
                            onDismissRequest = { editUnidadExpanded = false }
                        ) {
                            unidades.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        nuevaUnidad = option
                                        editUnidadExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    OutlinedTextField(value = nuevoCosto, onValueChange = { nuevoCosto = it }, label = { Text("Costo por unidad") })
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val ingredienteEditado = Ingrediente(
                        nombre = nuevoNombre,
                        unidad = nuevaUnidad,
                        costoUnidad = nuevoCosto.toDoubleOrNull() ?: 0.0
                    )
                    viewModel.editarIngrediente(ingredienteEditado)
                    mostrarDialogoEdicion = false
                }) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoEdicion = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}