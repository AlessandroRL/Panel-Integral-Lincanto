package com.example.paneldecontrolreposteria.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.paneldecontrolreposteria.model.Ingrediente
import com.example.paneldecontrolreposteria.ui.asistente.AsistenteButtonFloating
import com.example.paneldecontrolreposteria.ui.costos.GestionarCostos
import com.example.paneldecontrolreposteria.viewmodel.IngredienteViewModel
import com.example.paneldecontrolreposteria.ui.productos.GestionarProductos
import com.example.paneldecontrolreposteria.viewmodel.ProductoCostoViewModel
import com.example.paneldecontrolreposteria.viewmodel.ProductoViewModel
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun GestionIngredientesScreen(navController: NavController) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Ingredientes", "Productos", "Costos")

    val productoViewModel: ProductoViewModel = viewModel()
    val costoViewModel: ProductoCostoViewModel = viewModel()
    val context = LocalContext.current

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
            0 -> GestionarIngredientes(navController, context)
            1 -> GestionarProductos(productoViewModel, navController, context)
            2 -> GestionarCostos(costoViewModel, navController, context)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionarIngredientes(
    navController: NavController,
    context: Context
) {
    val viewModel: IngredienteViewModel = viewModel()
    val ingredientesFiltrados by viewModel.ingredientesFiltrados.collectAsState()
    val textoBusqueda by viewModel.busqueda.collectAsState()

    var nombre by remember { mutableStateOf("") }
    var unidad by remember { mutableStateOf("") }
    var costoUnidad by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    var unidadExpanded by remember { mutableStateOf(false) }
    val unidades = listOf("gr", "ml", "unidad")

    var ingredienteAEditar by remember { mutableStateOf<Ingrediente?>(null) }
    var mostrarDialogoEdicion by remember { mutableStateOf(false) }
    var ingredienteAEliminar by remember { mutableStateOf<Ingrediente?>(null) }

    val isDarkTheme = isSystemInDarkTheme()
    val textColor = if (isDarkTheme) Color.White else Color.DarkGray
    val cardColor = if (isDarkTheme) Color.DarkGray else Color.White
    val gold = Color(0xFFC7A449)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "Registrar Ingrediente",
            style = MaterialTheme.typography.titleLarge,
            color = textColor
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre", color = textColor) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        ExposedDropdownMenuBox(
            expanded = unidadExpanded,
            onExpandedChange = { unidadExpanded = !unidadExpanded }
        ) {
            OutlinedTextField(
                value = unidad,
                onValueChange = {},
                label = { Text("Unidad", color = textColor) },
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unidadExpanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            ExposedDropdownMenu(
                expanded = unidadExpanded,
                onDismissRequest = { unidadExpanded = false }
            ) {
                unidades.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option, color = textColor, style = MaterialTheme.typography.bodyLarge) },
                        onClick = {
                            unidad = option
                            unidadExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = costoUnidad,
            onValueChange = { costoUnidad = it },
            label = { Text("Costo por unidad", color = textColor) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = {
                    if (nombre.isNotBlank() && unidad.isNotBlank() && costoUnidad.toDoubleOrNull() != null) {
                        val nuevo = Ingrediente(
                            nombre = nombre,
                            unidad = unidad,
                            costoUnidad = costoUnidad.toDouble()
                        )
                        viewModel.agregarIngrediente(nuevo)
                        Toast.makeText(context, "Ingrediente agregado", Toast.LENGTH_SHORT).show()
                        nombre = ""
                        unidad = ""
                        costoUnidad = ""
                    } else {
                        Toast.makeText(context, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = gold),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Guardar Ingrediente", color = textColor, style = MaterialTheme.typography.bodyLarge)
            }

            AsistenteButtonFloating(
                currentTabIndex = 0,
                onMicClick = {
                    if (ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.RECORD_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        Toast.makeText(context, "🎤 Escuchando...", Toast.LENGTH_SHORT).show()
                        navController.navigate("asistenteVirtual?activarEscuchaInicial=true")
                    } else {
                        Toast.makeText(context, "Permiso de grabación no concedido", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        HorizontalDivider(color = textColor)

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "Ingredientes Registrados",
            style = MaterialTheme.typography.titleMedium,
            color = textColor
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = textoBusqueda,
            onValueChange = { viewModel.actualizarBusqueda(it) },
            label = { Text("Buscar por nombre", color = textColor) },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Buscar", tint = textColor)
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn {
            items(ingredientesFiltrados) { ingrediente ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = cardColor),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Nombre: ${ingrediente.nombre}", color = textColor)
                            Text("Unidad: ${ingrediente.unidad}", color = textColor)
                            Text("Costo: ${ingrediente.costoUnidad}", color = textColor)
                        }
                        Row {
                            IconButton(onClick = {
                                ingredienteAEditar = ingrediente
                                mostrarDialogoEdicion = true
                            }) {
                                Icon(Icons.Default.Edit, contentDescription = "Editar", tint = gold)
                            }
                            IconButton(onClick = {
                                ingredienteAEliminar = ingrediente
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.Red)
                            }
                        }
                    }
                }
            }
        }
    }

    if (ingredienteAEliminar != null) {
        AlertDialog(
            onDismissRequest = { ingredienteAEliminar = null },
            title = { Text("Eliminar Ingrediente", style = MaterialTheme.typography.titleLarge, color = textColor) },
            text = { Text("¿Estás seguro de que quieres eliminar este ingrediente?", color = textColor, style = MaterialTheme.typography.bodyLarge) },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        viewModel.eliminarIngrediente(ingredienteAEliminar!!.nombre)
                        Toast.makeText(context, "Ingrediente eliminado", Toast.LENGTH_SHORT).show()
                        ingredienteAEliminar = null
                    }
                }) {
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

    if (mostrarDialogoEdicion && ingredienteAEditar != null) {
        var nuevoNombre by remember { mutableStateOf(ingredienteAEditar!!.nombre) }
        var nuevaUnidad by remember { mutableStateOf(ingredienteAEditar!!.unidad) }
        var nuevoCosto by remember { mutableStateOf(ingredienteAEditar!!.costoUnidad.toString()) }
        var editUnidadExpanded by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { mostrarDialogoEdicion = false },
            title = { Text("Editar Ingrediente", style = MaterialTheme.typography.titleLarge, color = textColor) },
            text = {
                Column {
                    OutlinedTextField(
                        value = nuevoNombre,
                        onValueChange = { nuevoNombre = it },
                        label = { Text("Nombre", color = textColor) },
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ExposedDropdownMenuBox(
                        expanded = editUnidadExpanded,
                        onExpandedChange = { editUnidadExpanded = !editUnidadExpanded }
                    ) {
                        OutlinedTextField(
                            value = nuevaUnidad,
                            onValueChange = {},
                            label = { Text("Unidad", color = textColor) },
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = editUnidadExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = editUnidadExpanded,
                            onDismissRequest = { editUnidadExpanded = false }
                        ) {
                            unidades.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option, color = textColor, style = MaterialTheme.typography.bodyLarge) },
                                    onClick = {
                                        nuevaUnidad = option
                                        editUnidadExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = nuevoCosto,
                        onValueChange = { nuevoCosto = it },
                        label = { Text("Costo por unidad", color = textColor) },
                        shape = RoundedCornerShape(12.dp)
                    )
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
                    Toast.makeText(context, "Ingrediente editado", Toast.LENGTH_SHORT).show()
                    mostrarDialogoEdicion = false
                }) {
                    Text("Guardar", color = gold, style = MaterialTheme.typography.bodyLarge)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoEdicion = false }) {
                    Text("Cancelar", color = gold, style = MaterialTheme.typography.bodyLarge)
                }
            },
            containerColor = cardColor
        )
    }
}