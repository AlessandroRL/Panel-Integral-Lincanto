package com.example.paneldecontrolreposteria.ui.productos

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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

    val isDarkTheme = isSystemInDarkTheme()
    val backgroundColor = if (isDarkTheme) Color.Black else Color.White
    val textColor = if (isDarkTheme) Color.White else Color.DarkGray
    val cardColor = if (isDarkTheme) Color.DarkGray else Color.White
    val gold = Color(0xFFC7A449)

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Text("Gestión de Productos", style = MaterialTheme.typography.titleLarge, color = textColor) },
                colors = TopAppBarDefaults.mediumTopAppBarColors(containerColor = backgroundColor, titleContentColor = textColor)
            )
        },
        floatingActionButton = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.End,
                modifier = Modifier.padding(end = 16.dp, bottom = 16.dp)
            ) {
                FloatingActionButton(
                    onClick = { showDialog = true },
                    containerColor = gold
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Agregar Producto", tint = textColor)
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
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            OutlinedTextField(
                value = textoBusqueda,
                onValueChange = { textoBusqueda = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Buscar producto", color = textColor) },
                singleLine = true,
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Buscar", tint = textColor)
                },
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            productosFiltrados.forEach { producto ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = cardColor)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "🍰 ${producto.nombre}",
                            style = MaterialTheme.typography.titleMedium,
                            color = gold
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text("Ingredientes:", style = MaterialTheme.typography.titleMedium, color = textColor)
                        Spacer(modifier = Modifier.height(4.dp))
                        producto.ingredientes.forEachIndexed { index, ingrediente ->
                            val detalle = buildString {
                                append("${ingrediente.nombre}: ${ingrediente.cantidad} ${ingrediente.unidad}")
                                if (!ingrediente.observacion.isNullOrBlank()) {
                                    append(" (${ingrediente.observacion})")
                                }
                            }
                            Text("${index + 1}. $detalle", style = MaterialTheme.typography.bodyMedium, color = textColor)
                        }

                        producto.preparacion?.let {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Preparación:", style = MaterialTheme.typography.titleMedium, color = textColor)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(it, style = MaterialTheme.typography.bodyMedium, color = textColor)
                        }

                        producto.utensilios?.let {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Utensilios:", style = MaterialTheme.typography.titleMedium, color = textColor)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(it.joinToString("\n"), style = MaterialTheme.typography.bodyMedium, color = textColor)
                        }

                        producto.tips?.let {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Tips:", style = MaterialTheme.typography.titleMedium, color = textColor)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(it, style = MaterialTheme.typography.bodyMedium, color = textColor)
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
                                Icon(imageVector = Icons.Default.Edit, contentDescription = "Editar", tint = gold)
                            }
                            IconButton(onClick = {
                                productoAEliminar = producto
                            }) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.Red)
                            }
                        }
                    }
                }
            }
        }
    }

    if (productoAEliminar != null) {
        AlertDialog(
            onDismissRequest = { productoAEliminar = null },
            title = { Text("Eliminar Producto", style = MaterialTheme.typography.titleLarge, color = textColor) },
            text = { Text("¿Estás seguro de que quieres eliminar este producto?", color = textColor, style = MaterialTheme.typography.bodyLarge) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.eliminarProducto(productoAEliminar!!.id)
                    Toast.makeText(context, "Producto eliminado", Toast.LENGTH_SHORT).show()
                    productoAEliminar = null
                }) {
                    Text("Eliminar", color = Color.Red, style = MaterialTheme.typography.bodyLarge)
                }
            },
            dismissButton = {
                TextButton(onClick = { productoAEliminar = null }) {
                    Text("Cancelar", color = gold, style = MaterialTheme.typography.bodyLarge)
                }
            },
            containerColor = cardColor
        )
    }

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
                        Toast.makeText(context, "Error al actualizar el producto", Toast.LENGTH_SHORT).show()
                    }
                    mostrarDialogoEditar = false
                }
            }
        )
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
    val context = LocalContext.current

    val isDarkTheme = isSystemInDarkTheme()
    val textColor = if (isDarkTheme) Color.White else Color.DarkGray
    val cardColor = if (isDarkTheme) Color.DarkGray else Color.White
    val gold = Color(0xFFC7A449)

    if (mostrarEquivalencias) {
        AlertDialog(
            onDismissRequest = { mostrarEquivalencias = false },
            confirmButton = {
                TextButton(onClick = { mostrarEquivalencias = false }) {
                    Text("Cerrar", color = gold, style = MaterialTheme.typography.bodyLarge)
                }
            },
            title = { Text("Equivalencias comunes", color = textColor, style = MaterialTheme.typography.titleLarge) },
            text = {
                Column {
                    Text("🥄 1 cucharada = 15 ml (líquido) / 10-12 gr (sólido)", color = textColor, style = MaterialTheme.typography.bodyLarge)
                    Text("🧂 1 cucharadita = 5 ml (líquido) / 3-5 gr (sólido)", color = textColor, style = MaterialTheme.typography.bodyLarge)
                    Text("🍵 1 taza = 240 ml (líquido) / 120-130 gr (harina/azúcar)", color = textColor, style = MaterialTheme.typography.bodyLarge)
                    Text("🫶 1 pizca ≈ 0.3 gramos (solo sólidos)", color = textColor, style = MaterialTheme.typography.bodyLarge)
                }
            },
            containerColor = cardColor
        )
    }

    if (ingredienteAEliminar != null) {
        AlertDialog(
            onDismissRequest = { ingredienteAEliminar = null },
            title = { Text("Eliminar Ingrediente", color = textColor, style = MaterialTheme.typography.titleLarge) },
            text = { Text("¿Estás seguro de que quieres eliminar este ingrediente?", color = textColor, style = MaterialTheme.typography.bodyLarge) },
            confirmButton = {
                TextButton(
                    onClick = {
                        ingredientes.remove(ingredienteAEliminar)
                        ingredienteAEliminar = null
                        Toast.makeText(context, "Ingrediente eliminado", Toast.LENGTH_SHORT).show()
                    }
                ) {
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
                },
                colors = ButtonDefaults.buttonColors(containerColor = gold),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Agregar", color = textColor, style = MaterialTheme.typography.bodyLarge)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                border = BorderStroke(1.dp, Color.Red),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Cancelar", color = Color.Red, style = MaterialTheme.typography.bodyLarge)
            }
        },
        title = { Text("Nuevo Producto", color = textColor, style = MaterialTheme.typography.titleLarge) },
        text = {
            Box {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it },
                        label = { Text("Nombre", color = textColor) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(Modifier.height(16.dp))

                    Text("Ingredientes del Producto", style = MaterialTheme.typography.titleMedium, color = textColor)

                    Spacer(Modifier.height(8.dp))

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

                        Spacer(Modifier.height(8.dp))

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
                                label = { Text("Unidad", color = textColor) },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedUnidad)
                                },
                                modifier = Modifier.menuAnchor(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = expandedUnidad,
                                onDismissRequest = { expandedUnidad = false }
                            ) {
                                unidadesDisponibles.forEach { unidad ->
                                    DropdownMenuItem(
                                        text = { Text(unidad, color = textColor, style = MaterialTheme.typography.bodyLarge) },
                                        onClick = {
                                            ingredientes[index] = ingrediente.copy(unidad = unidad)
                                            expandedUnidad = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        OutlinedTextField(
                            value = if (ingrediente.cantidad == 0.0) "" else ingrediente.cantidad.toString(),
                            onValueChange = {
                                ingredientes[index] = ingrediente.copy(cantidad = it.toDoubleOrNull() ?: 0.0)
                            },
                            label = { Text("Cantidad", color = textColor) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(Modifier.height(8.dp))

                        OutlinedTextField(
                            value = ingrediente.observacion ?: "",
                            onValueChange = {
                                ingredientes[index] = ingrediente.copy(observacion = it)
                            },
                            label = { Text("Observación (opcional)", color = textColor) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            IconButton(onClick = { ingredienteAEliminar = ingrediente }) {
                                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.Red)
                            }
                        }

                        Spacer(Modifier.height(12.dp))
                    }

                    Button(
                        onClick = {
                            ingredientes.add(IngredienteDetalle(nombre = "", cantidad = 0.0, unidad = ""))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = gold),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Añadir nuevo ingrediente", color = textColor, style = MaterialTheme.typography.bodyLarge)
                    }

                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = preparacion,
                        onValueChange = { preparacion = it },
                        label = { Text("Preparación (opcional)", color = textColor) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = utensiliosTexto,
                        onValueChange = { utensiliosTexto = it },
                        label = { Text("Utensilios (uno por línea, opcional)", color = textColor) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = tips,
                        onValueChange = { tips = it },
                        label = { Text("Tips (opcional)", color = textColor) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(Modifier.height(32.dp))
                }

                FloatingActionButton(
                    onClick = { mostrarEquivalencias = true },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp),
                    containerColor = gold
                ) {
                    Icon(Icons.Default.Info, contentDescription = "Equivalencias", tint = textColor)
                }
            }
        },
        containerColor = cardColor
    )
}