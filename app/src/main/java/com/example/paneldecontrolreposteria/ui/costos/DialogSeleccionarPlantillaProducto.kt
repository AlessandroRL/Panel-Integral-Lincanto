package com.example.paneldecontrolreposteria.ui.costos

import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color

@Composable
fun DialogSeleccionarPlantillaProducto(
    onDismiss: () -> Unit,
    onSeleccionar: (ProductoCosto) -> Unit,
    viewModel: ProductoCostoViewModel
) {
    val isDarkTheme = isSystemInDarkTheme()
    val scope = rememberCoroutineScope()
    val productos by viewModel.productosBase.collectAsState()
    val cargando by viewModel.cargando.collectAsState()

    val backgroundColor = if (isDarkTheme) Color.Black else Color.White
    val textColor = if (isDarkTheme) Color.White else Color.DarkGray
    val gold = Color(0xFFC7A449)

    LaunchedEffect(Unit) {
        viewModel.cargarProductosBase()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        containerColor = MaterialTheme.colorScheme.background,
        title = {
            Text(
                "Seleccionar plantilla de producto",
                style = MaterialTheme.typography.titleLarge,
                color = gold
            )
        },
        text = {
            if (cargando) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = textColor)
                }
            } else if (productos.isEmpty()) {
                Text(
                    "No hay productos disponibles.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp)
                        .verticalScroll(rememberScrollState()),
                ) {
                    productos.forEach { producto ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .clickable {
                                    scope.launch {
                                        val productoCosto = viewModel.crearProductoCostoDesdePlantilla(producto)
                                        onSeleccionar(productoCosto)
                                        onDismiss()
                                    }
                                },
                            tonalElevation = 3.dp,
                            color = backgroundColor
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = producto.nombre,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = textColor
                                )
                                if (producto.ingredientes.isNotEmpty()) {
                                    Text(
                                        text = "${producto.ingredientes.size} ingrediente(s)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = textColor
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.textButtonColors(
                    contentColor = gold
                )
            ) {
                Text("Cancelar", style = MaterialTheme.typography.bodyLarge, color = gold)
            }
        }
    )
}