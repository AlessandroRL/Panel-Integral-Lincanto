package com.example.paneldecontrolreposteria.ui.costos

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.paneldecontrolreposteria.model.ProductoCosto
import com.example.paneldecontrolreposteria.viewmodel.ProductoCostoViewModel
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment

@Composable
fun DialogSeleccionarPlantillaProducto(
    onDismiss: () -> Unit,
    onSeleccionar: (ProductoCosto) -> Unit,
    viewModel: ProductoCostoViewModel
) {
    val scope = rememberCoroutineScope()
    val productos by viewModel.productosBase.collectAsState()
    val cargando by viewModel.cargando.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.cargarProductosBase()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Seleccionar plantilla de producto") },
        text = {
            if (cargando) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (productos.isEmpty()) {
                Text("No hay productos disponibles.")
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    productos.forEach { producto ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    scope.launch {
                                        val productoCosto = viewModel.crearProductoCostoDesdePlantilla(producto)
                                        onSeleccionar(productoCosto)
                                        onDismiss()
                                    }
                                },
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            tonalElevation = 1.dp
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = producto.nombre,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (producto.ingredientes.isNotEmpty()) {
                                    Text(
                                        text = "${producto.ingredientes.size} ingrediente(s)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}