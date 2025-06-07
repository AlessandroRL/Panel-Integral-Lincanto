package com.example.paneldecontrolreposteria.ui.costos

import android.annotation.SuppressLint
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.paneldecontrolreposteria.model.ProductoCosto
import androidx.compose.foundation.layout.*


@SuppressLint("DefaultLocale")
@Composable
fun DialogDetalleProductoCosto(
    producto: ProductoCosto,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Detalles del producto") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text("Nombre: ${producto.nombre}", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                producto.ingredientes.forEach { (_, ingrediente) ->
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        Text("Ingrediente: ${ingrediente.nombre}")
                        Text("Cantidad: ${ingrediente.cantidad} ${ingrediente.unidad}")
                        Text("Costo unitario: $${String.format("%.2f", ingrediente.costoUnidad)}")
                        Text("Costo total: $${String.format("%.2f", ingrediente.costoTotal)}")
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("Costo total estimado: $${String.format("%.2f", producto.costoTotal)}")
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}