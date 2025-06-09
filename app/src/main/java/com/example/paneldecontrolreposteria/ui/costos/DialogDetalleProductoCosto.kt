package com.example.paneldecontrolreposteria.ui.costos

import android.annotation.SuppressLint
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.paneldecontrolreposteria.model.ProductoCosto
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color

@SuppressLint("DefaultLocale")
@Composable
fun DialogDetalleProductoCosto(
    producto: ProductoCosto,
    onDismiss: () -> Unit
) {
    val isDarkTheme = isSystemInDarkTheme()
    val textColor = if (isDarkTheme) Color.White else Color.DarkGray
    val cardColor = if (isDarkTheme) Color.DarkGray else Color.White
    val gold = Color(0xFFC7A449)

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        containerColor = cardColor,
        title = {
            Text(
                "Detalles del producto",
                style = MaterialTheme.typography.titleLarge,
                    color = textColor
                )
        },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(end = 4.dp)
            ) {
                Text(
                    "Nombre: ${producto.nombre}",
                    style = MaterialTheme.typography.titleMedium,
                    color = gold
                )
                Spacer(modifier = Modifier.height(12.dp))

                producto.ingredientes.forEach { (_, ingrediente) ->
                    Column(
                        modifier = Modifier
                            .padding(vertical = 6.dp)
                    ) {
                        Text(
                            "Ingrediente: ${ingrediente.nombre}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = textColor
                        )
                        Text(
                            "Cantidad: ${ingrediente.cantidad} ${ingrediente.unidad}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = textColor
                        )
                        Text(
                            "Costo unitario: $${String.format("%.2f", ingrediente.costoUnidad)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = textColor
                        )
                        Text(
                            "Costo total: $${String.format("%.2f", ingrediente.costoTotal)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = textColor
                        )
                    }
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 6.dp),
                        color = textColor,
                        thickness = 1.dp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Costo total estimado: $${String.format("%.2f", producto.costoTotal)}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = textColor
                )
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
                Text("Cerrar", style = MaterialTheme.typography.bodyLarge, color = gold)
            }
        }
    )
}