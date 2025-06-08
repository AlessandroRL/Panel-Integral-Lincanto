package com.example.paneldecontrolreposteria.ui.productos

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.paneldecontrolreposteria.model.IngredienteDetalle
import com.example.paneldecontrolreposteria.model.Producto
import com.example.paneldecontrolreposteria.ui.asistente.AsistenteButtonFloating
import com.example.paneldecontrolreposteria.ui.components.BusquedaIngredientesConLista
import com.example.paneldecontrolreposteria.viewmodel.IngredienteViewModel
import com.example.paneldecontrolreposteria.viewmodel.ProductoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun GestionarProductos(
    viewModel: ProductoViewModel,
    navController: NavController,
    context: Context
) {
    var showDialog by remember { mutableStateOf(false) }
    var textoBusqueda by remember { mutableStateOf("") }
    var mostrarDialogoEditar by remember { mutableStateOf(false) }
    var productoParaEditar by remember { mutableStateOf<Producto?>(null) }
    var productoAEliminar by remember { mutableStateOf<Producto?>(null) }

    val listaProductos by viewModel.productos.collectAsState()
    val productosFiltrados = listaProductos
        .filter { it.nombre.contains(textoBusqueda, ignoreCase = true) }
        .sortedBy { it.nombre.lowercase() }

    val scrollState = rememberLazyListState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Gestión de Productos") },
                navigationIcon = {},
                actions = {}
            )
        },
        floatingActionButton = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.End,
                modifier = Modifier.padding(end = 16.dp, bottom = 16.dp)
            ) {
                FloatingActionButton(onClick = { showDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Agregar Producto")
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
        }
    ) { paddingValues ->
        LazyColumn(
            state = scrollState,
            contentPadding = PaddingValues(
                top = paddingValues.calculateTopPadding() + 8.dp,
                bottom = paddingValues.calculateBottomPadding() + 16.dp,
                start = 16.dp,
                end = 16.dp
            ),
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                OutlinedTextField(
                    value = textoBusqueda,
                    onValueChange = { textoBusqueda = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    label = { Text("Buscar producto") },
                    singleLine = true,
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Buscar")
                    }
                )
            }

            items(productosFiltrados) { producto ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "🍰 ${producto.nombre}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text("Ingredientes:", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.primary)
                        producto.ingredientes.forEachIndexed { index, ingrediente ->
                            val detalle = buildString {
                                append("${ingrediente.nombre}: ${ingrediente.cantidad} ${ingrediente.unidad}")
                                if (!ingrediente.observacion.isNullOrBlank()) {
                                    append(" (${ingrediente.observacion})")
                                }
                            }
                            Text("${index + 1}. $detalle", style = MaterialTheme.typography.bodyMedium)
                        }

                        producto.preparacion?.let {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Preparación:", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(it, style = MaterialTheme.typography.bodyMedium)
                        }

                        producto.utensilios?.let {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Utensilios:", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(it.joinToString("\n"), style = MaterialTheme.typography.bodyMedium)
                        }

                        producto.tips?.let {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Tips:", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.primary)
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
                                productoAEliminar = producto
                            }) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Eliminar")
                            }
                        }
                    }
                }
            }
        }

        if (productoAEliminar != null) {
            AlertDialog(
                onDismissRequest = { productoAEliminar = null },
                title = { Text("Eliminar Producto") },
                text = { Text("¿Estás seguro de que quieres eliminar este producto?") },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.eliminarProducto(productoAEliminar!!.id)
                        Toast.makeText(context, "Producto eliminado", Toast.LENGTH_SHORT).show()
                        productoAEliminar = null
                    }) {
                        Text("Eliminar", color = Color.Red)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { productoAEliminar = null }) {
                        Text("Cancelar")
                    }
                }
            )
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
                Toast.makeText(context, "Producto agregado", Toast.LENGTH_SHORT).show()
                showDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("MutableCollectionMutableState")
@Composable
fun DialogoAgregarProducto(
    onDismiss: () -> Unit,
    onAgregar: (Producto) -> Unit
) {
    val ingredienteViewModel: IngredienteViewModel = viewModel()
    val ingredientesDisponibles = ingredienteViewModel.ingredientes.collectAsState().value
    val unidadesDisponibles = listOf("gr", "ml", "unidad")

    var nombre by remember { mutableStateOf("") }
    val ingredientes = remember { mutableStateListOf<IngredienteDetalle>() }
    var ingredienteAEliminar by remember { mutableStateOf<IngredienteDetalle?>(null) }
    var preparacion by remember { mutableStateOf("") }
    var utensiliosTexto by remember { mutableStateOf("") }
    var tips by remember { mutableStateOf("") }
    var mostrarEquivalencias by remember { mutableStateOf(false) }

    if (mostrarEquivalencias) {
        AlertDialog(
            onDismissRequest = { mostrarEquivalencias = false },
            confirmButton = {
                TextButton(onClick = { mostrarEquivalencias = false }) {
                    Text("Cerrar")
                }
            },
            title = { Text("Equivalencias comunes") },
            text = {
                Column {
                    Text("🥄 1 cucharada = 15 ml (líquido) / 10-12 gr (sólido)")
                    Text("🧂 1 cucharadita = 5 ml (líquido) / 3-5 gr (sólido)")
                    Text("🍵 1 taza = 240 ml (líquido) / 120-130 gr (harina/azúcar)")
                    Text("🫶 1 pizca ≈ 0.3 gramos (solo sólidos)")
                }
            }
        )
    }

    if (ingredienteAEliminar != null) {
        AlertDialog(
            onDismissRequest = { ingredienteAEliminar = null },
            title = { Text("Eliminar Ingrediente") },
            text = { Text("¿Estás seguro de que quieres eliminar este ingrediente?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        ingredientes.remove(ingredienteAEliminar)
                        ingredienteAEliminar = null
                    }
                ) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { ingredienteAEliminar = null }) {
                    Text("Cancelar")
                }
            }
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

                    val producto = Producto(
                        nombre = nombre.trim(),
                        ingredientes = ingredientes.toList(),
                        preparacion = preparacion.trim().ifBlank { null },
                        utensilios = if (utensilios.isNotEmpty()) utensilios else null,
                        tips = tips.trim().ifBlank { null }
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
            Box {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it },
                        label = { Text("Nombre (usa guión si es subproducto)") },
                        singleLine = true
                    )
                    Spacer(Modifier.height(8.dp))

                    Text("Ingredientes del Producto", style = MaterialTheme.typography.titleMedium)

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

                        var expandedUnidad by remember { mutableStateOf(false) }

                        ExposedDropdownMenuBox(
                            expanded = expandedUnidad,
                            onExpandedChange = { expandedUnidad = !expandedUnidad },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        ) {
                            OutlinedTextField(
                                value = ingrediente.unidad,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Unidad") },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedUnidad)
                                },
                                modifier = Modifier.menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = expandedUnidad,
                                onDismissRequest = { expandedUnidad = false }
                            ) {
                                unidadesDisponibles.forEach { unidad ->
                                    DropdownMenuItem(
                                        text = { Text(unidad) },
                                        onClick = {
                                            ingredientes[index] = ingrediente.copy(unidad = unidad)
                                            expandedUnidad = false
                                        }
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = if (ingrediente.cantidad == 0.0) "" else ingrediente.cantidad.toString(),
                            onValueChange = {
                                val nuevaCantidad = it.toDoubleOrNull() ?: 0.0
                                ingredientes[index] = ingrediente.copy(cantidad = nuevaCantidad)
                            },
                            label = { Text("Cantidad") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        )

                        OutlinedTextField(
                            value = ingrediente.observacion ?: "",
                            onValueChange = {
                                ingredientes[index] = ingrediente.copy(observacion = it.ifBlank { null })
                            },
                            label = { Text("Observación (opcional)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            IconButton(onClick = { ingredienteAEliminar = ingrediente }) {
                                Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    Button(
                        onClick = {
                            ingredientes.add(IngredienteDetalle())
                        },
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        Text("Añadir nuevo ingrediente")
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
                    Spacer(Modifier.height(32.dp))
                }

                FloatingActionButton(
                    onClick = { mostrarEquivalencias = true },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp),
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Icon(Icons.Default.Info, contentDescription = "Equivalencias")
                }
            }
        }
    )
}