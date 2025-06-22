package com.example.paneldecontrolreposteria.screens

import android.annotation.SuppressLint
import android.app.AlarmManager
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.paneldecontrolreposteria.model.Pedido
import com.example.paneldecontrolreposteria.viewmodel.PedidoViewModel
import kotlinx.coroutines.launch
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.DropdownMenuItem
import android.app.DatePickerDialog
import android.content.Context
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.paneldecontrolreposteria.model.ProductoPedido
import com.example.paneldecontrolreposteria.ui.notificaciones.AgregarNotificacionUI
import com.example.paneldecontrolreposteria.ui.notificaciones.PermissionManager
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@RequiresApi(value = 36)
@SuppressLint("MutableCollectionMutableState", "DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgregarPedidoScreen(viewModel: PedidoViewModel, onPedidoAgregado: () -> Unit) {
    val isDarkTheme = isSystemInDarkTheme()
    var cliente by remember { mutableStateOf("") }
    var fechaLimite by remember { mutableStateOf("") }
    var errorMensaje by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    data class ProductoSeleccionado(val nombre: String, var cantidad: String, var tamano: String)
    var productosSeleccionados by remember { mutableStateOf(mutableListOf<ProductoSeleccionado>()) }

    var productosDisponibles by remember { mutableStateOf<List<String>>(emptyList()) }
    var productoActual by remember { mutableStateOf("") }
    var cantidadActual by remember { mutableStateOf("") }
    var tamanoActual by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var productoAEliminar by remember { mutableStateOf<ProductoSeleccionado?>(null) }
    val context = LocalContext.current

    val fechasNotificacion = remember { mutableStateListOf<LocalDateTime>() }
    var mostrarDialogoNotificacion by remember { mutableStateOf(false) }

    val backgroundColor = if (isDarkTheme) Color.Black else Color.White
    val textColor = if (isDarkTheme) Color.White else Color.DarkGray
    val cardColor = if (isDarkTheme) Color.DarkGray else Color.White
    val gold = Color(0xFFC7A449)

    LaunchedEffect(Unit) {
        viewModel.obtenerNombresProductos { productos -> productosDisponibles = productos }
    }

    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)
    val datePickerDialog = remember {
        DatePickerDialog(context, { _, y, m, d ->
            fechaLimite = String.format("%04d-%02d-%02d", y, m + 1, d)
        }, year, month, day)
    }

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                title = { Text("Agregar Pedido", style = MaterialTheme.typography.titleLarge, color = textColor) },
                colors = TopAppBarDefaults.mediumTopAppBarColors(containerColor = backgroundColor, titleContentColor = textColor)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(8.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = cardColor)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = cliente,
                        onValueChange = { cliente = it },
                        label = { Text("Cliente", color = textColor) },
                        modifier = Modifier.fillMaxWidth(),
                        isError = errorMensaje != null && cliente.isBlank(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                        OutlinedTextField(
                            readOnly = true,
                            value = productoActual,
                            onValueChange = {},
                            label = { Text("Producto", color = textColor) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            isError = errorMensaje != null && productoActual.isBlank(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            productosDisponibles.forEach { nombre ->
                                DropdownMenuItem(
                                    text = { Text(nombre,color = textColor, style = MaterialTheme.typography.bodyLarge) },
                                    onClick = {
                                        productoActual = nombre
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = cantidadActual,
                        onValueChange = { cantidadActual = it },
                        label = { Text("Cantidad") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = cantidadActual.toIntOrNull() == null,
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = tamanoActual,
                        onValueChange = { tamanoActual = it },
                        label = { Text("Tamaño (personas)") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = tamanoActual.isBlank(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Button(
                        onClick = {
                            if (productoActual.isNotBlank() && cantidadActual.isNotBlank() && tamanoActual.isNotBlank()) {
                                productosSeleccionados.add(ProductoSeleccionado(productoActual, cantidadActual, tamanoActual))
                                productoActual = ""
                                cantidadActual = ""
                                tamanoActual = ""
                            }
                        },
                        modifier = Modifier
                            .padding(vertical = 12.dp)
                            .fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = gold)
                    ) {
                        Text("Añadir Producto", color = textColor, style = MaterialTheme.typography.bodyLarge)
                    }

                    if (productosSeleccionados.isNotEmpty()) {
                        Text("Productos Agregados:", style = MaterialTheme.typography.titleMedium)
                        productosSeleccionados.forEach { producto ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = gold)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        "- ${producto.nombre} (${producto.cantidad} u, ${producto.tamano} personas)",
                                        modifier = Modifier.weight(1f),
                                        color = textColor
                                    )
                                    IconButton(onClick = { productoAEliminar = producto }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.Red)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = fechaLimite,
                        onValueChange = {},
                        label = { Text("Fecha Límite") },
                        readOnly = true,
                        enabled = false,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { datePickerDialog.show() },
                        shape = RoundedCornerShape(12.dp),
                        isError = errorMensaje != null && fechaLimite.isBlank()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Fechas de notificación", style = MaterialTheme.typography.titleSmall)

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(onClick = { mostrarDialogoNotificacion = true }, colors = ButtonDefaults.buttonColors(containerColor = gold)) {
                        Icon(Icons.Default.Add, contentDescription = "Agregar", tint = textColor)
                        Spacer(Modifier.width(4.dp))
                        Text("Agregar notificación", color = textColor)
                    }

                    fechasNotificacion.forEach { fecha ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Text("📅 ${fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy h:mm a"))}")
                            IconButton(onClick = { fechasNotificacion.remove(fecha) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.Red)
                            }
                        }
                    }

                    if (errorMensaje != null) {
                        Text(errorMensaje!!, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        OutlinedButton(
                            onClick = { onPedidoAgregado() },
                            border = BorderStroke(1.dp, Color.Red),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Cancelar", color = Color.Red, style = MaterialTheme.typography.bodyLarge)
                        }

                        Button(
                            onClick = {
                                if (cliente.isBlank() || productosSeleccionados.isEmpty() || fechaLimite.isBlank()) {
                                    errorMensaje = "Por favor, complete todos los campos obligatorios."
                                    return@Button
                                }
                                scope.launch {
                                    try {
                                        val nuevoPedido = Pedido(
                                            cliente = cliente,
                                            productos = productosSeleccionados.map {
                                                ProductoPedido(it.nombre, it.cantidad.toInt(), it.tamano.toInt())
                                            },
                                            fechaLimite = fechaLimite,
                                            notificaciones = fechasNotificacion.map { it.toString() }
                                        )
                                        viewModel.agregarPedido(nuevoPedido)

                                        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                                        if (!alarmManager.canScheduleExactAlarms()) {
                                            PermissionManager.solicitarPermisoAlarmasExactas(context)
                                            return@launch
                                        }

                                        viewModel.programarNotificacion(context, nuevoPedido)
                                        Toast.makeText(context, "Pedido agregado", Toast.LENGTH_SHORT).show()
                                        onPedidoAgregado()
                                    } catch (e: Exception) {
                                        errorMensaje = "Error al agregar pedido: ${e.message}"
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = gold),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Agregar Pedido", color = textColor, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
        }
    }

    if (mostrarDialogoNotificacion) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoNotificacion = false },
            confirmButton = {},
            title = null,
            text = {
                AgregarNotificacionUI(
                    onGuardar = {
                        fechasNotificacion.add(it)
                        mostrarDialogoNotificacion = false
                    },
                    onCancelar = {
                        mostrarDialogoNotificacion = false
                    }
                )
            },
            containerColor = cardColor
        )
    }

    if (productoAEliminar != null) {
        AlertDialog(
            onDismissRequest = { productoAEliminar = null },
            title = { Text("Eliminar Producto", style = MaterialTheme.typography.titleLarge) },
            text = { Text("¿Estás seguro de que quieres eliminar este producto?",
                color = textColor,
                style = MaterialTheme.typography.bodyLarge
            ) },
            confirmButton = {
                TextButton(onClick = {
                    productosSeleccionados.remove(productoAEliminar)
                    productoAEliminar = null
                    Toast.makeText(context, "Producto eliminado", Toast.LENGTH_SHORT).show()
                }) {
                    Text("Eliminar", color = Color.Red, style = MaterialTheme.typography.bodyLarge)
                }
            },
            dismissButton = {
                TextButton(onClick = { productoAEliminar = null }) {
                    Text("Cancelar", color = gold, style = MaterialTheme.typography.bodyLarge)
                }
            }
        )
    }
}