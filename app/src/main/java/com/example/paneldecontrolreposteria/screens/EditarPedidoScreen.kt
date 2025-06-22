package com.example.paneldecontrolreposteria.screens

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.DatePickerDialog
import android.content.Context
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.*
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.paneldecontrolreposteria.model.Pedido
import com.example.paneldecontrolreposteria.model.ProductoPedido
import com.example.paneldecontrolreposteria.viewmodel.PedidoViewModel
import kotlinx.coroutines.launch
import java.util.Calendar
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Add
import androidx.compose.ui.Alignment
import com.example.paneldecontrolreposteria.ui.notificaciones.AgregarNotificacionUI
import com.example.paneldecontrolreposteria.ui.notificaciones.PermissionManager
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@RequiresApi(value = 36)
@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarPedidoScreen(
    viewModel: PedidoViewModel,
    pedido: Pedido,
    onPedidoEditado: () -> Unit
) {
    val isDarkTheme = isSystemInDarkTheme()
    val backgroundColor = if (isDarkTheme) Color.Black else Color.White
    val textColor = if (isDarkTheme) Color.White else Color.DarkGray
    val cardColor = if (isDarkTheme) Color.DarkGray else Color.White
    val gold = Color(0xFFC7A449)

    var cliente by remember { mutableStateOf(pedido.cliente) }
    var fechaLimite by remember { mutableStateOf(pedido.fechaLimite) }
    var errorMensaje by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    val productos = remember { mutableStateListOf<ProductoPedido>().apply { addAll(pedido.productos) } }
    var productosDisponibles by remember { mutableStateOf<List<String>>(emptyList()) }
    var productoAEliminar by remember { mutableStateOf<ProductoPedido?>(null) }

    val fechasNotificacion = remember {
        mutableStateListOf<LocalDateTime>().apply {
            addAll(pedido.notificaciones.mapNotNull {
                try {
                    LocalDateTime.parse(it)
                } catch (_: Exception) {
                    null
                }
            })
        }
    }
    var mostrarDialogoNotificacion by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.obtenerNombresProductos { productos ->
            productosDisponibles = productos
        }
    }

    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _, selectedYear, selectedMonth, selectedDay ->
                fechaLimite = String.format(
                    "%04d-%02d-%02d",
                    selectedYear,
                    selectedMonth + 1,
                    selectedDay
                )
            },
            year,
            month,
            day
        )
    }

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                title = { Text("Editar Pedido", style = MaterialTheme.typography.titleLarge, color = textColor) },
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

                    productos.forEachIndexed { index, producto ->
                        var expanded by remember { mutableStateOf(false) }

                        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                            OutlinedTextField(
                                readOnly = true,
                                value = producto.nombre,
                                onValueChange = {},
                                label = { Text("Producto ${index + 1}", color = textColor) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                productosDisponibles.forEach { nombre ->
                                    DropdownMenuItem(
                                        text = { Text(nombre, color = textColor, style = MaterialTheme.typography.bodyLarge) },
                                        onClick = {
                                            productos[index] = producto.copy(nombre = nombre)
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = producto.cantidad.toString(),
                            onValueChange = {
                                productos[index] = producto.copy(cantidad = it.toIntOrNull() ?: producto.cantidad)
                            },
                            label = { Text("Cantidad", color = textColor) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = producto.tamano.toString(),
                            onValueChange = {
                                productos[index] = producto.copy(tamano = it.toIntOrNull() ?: producto.tamano)
                            },
                            label = { Text("Tamaño (personas)", color = textColor) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        IconButton(onClick = { productoAEliminar = producto }, modifier = Modifier.padding(top = 8.dp)) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar Producto", tint = Color.Red)
                        }
                    }

                    Button(
                        onClick = {
                            productos.add(ProductoPedido(nombre = "", cantidad = 1, tamano = 1))
                        },
                        modifier = Modifier.padding(vertical = 12.dp).fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = gold)
                    ) {
                        Text("Añadir Producto", color = textColor, style = MaterialTheme.typography.bodyLarge)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = fechaLimite,
                        onValueChange = {},
                        label = { Text("Fecha Límite", color = textColor) },
                        readOnly = true,
                        enabled = false,
                        modifier = Modifier.fillMaxWidth().clickable { datePickerDialog.show() },
                        shape = RoundedCornerShape(12.dp),
                        isError = errorMensaje != null && fechaLimite.isBlank()
                    )

                    Spacer(modifier = Modifier.height(24.dp))

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
                            modifier = Modifier.fillMaxWidth().padding(vertical = 0.dp)
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
                            onClick = { onPedidoEditado() },
                            border = BorderStroke(1.dp, Color.Red),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Cancelar", color = Color.Red, style = MaterialTheme.typography.bodyLarge)
                        }

                        Button(
                            onClick = {
                                if (cliente.isBlank() || productos.any { it.nombre.isBlank() || it.cantidad <= 0 || it.tamano <= 0 }) {
                                    errorMensaje = "Por favor, complete todos los campos correctamente."
                                    return@Button
                                }

                                val pedidoActualizado = pedido.copy(
                                    cliente = cliente,
                                    productos = productos,
                                    fechaLimite = fechaLimite,
                                    notificaciones = fechasNotificacion.map { it.toString() }
                                )

                                scope.launch {
                                    viewModel.editarPedido(pedidoActualizado) { success ->
                                        if (success) {
                                            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                                            if (!alarmManager.canScheduleExactAlarms()) {
                                                PermissionManager.solicitarPermisoAlarmasExactas(context)
                                                return@editarPedido
                                            }

                                            viewModel.programarNotificacion(context, pedidoActualizado)
                                            Toast.makeText(context, "Pedido editado", Toast.LENGTH_SHORT).show()
                                            onPedidoEditado()
                                        } else {
                                            errorMensaje = "Error al editar el pedido."
                                        }
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = gold),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Guardar Cambios", color = textColor, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
        }
    }

    if (productoAEliminar != null) {
        AlertDialog(
            onDismissRequest = { productoAEliminar = null },
            title = { Text("Eliminar Producto", style = MaterialTheme.typography.titleLarge) },
            text = { Text("¿Estás seguro de que quieres eliminar este producto?", color = textColor, style = MaterialTheme.typography.bodyLarge) },
            confirmButton = {
                TextButton(
                    onClick = {
                        productos.remove(productoAEliminar)
                        productoAEliminar = null
                        Toast.makeText(context, "Producto eliminado", Toast.LENGTH_SHORT).show()
                    }
                ) {
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

    if (mostrarDialogoNotificacion) {
        AgregarNotificacionUI(
            onGuardar = {
                fechasNotificacion.add(it)
                mostrarDialogoNotificacion = false
            },
            onCancelar = {
                mostrarDialogoNotificacion = false
            }
        )
    }
}