package com.example.paneldecontrolreposteria.ui.costos

import android.annotation.SuppressLint
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.paneldecontrolreposteria.model.IngredienteCosto
import com.example.paneldecontrolreposteria.viewmodel.ProductoCostoViewModel
import com.example.paneldecontrolreposteria.ui.components.BusquedaIngredientesConLista

@SuppressLint("StateFlowValueCalledInComposition", "DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgregarIngredienteCostoComposable(
    viewModel: ProductoCostoViewModel,
    onAgregar: (IngredienteCosto) -> Unit,
    onCancelar: () -> Unit
) {
    val isDarkTheme = isSystemInDarkTheme()
    val ingredientesDisponibles by viewModel.ingredientesDisponibles.collectAsState()
    val ingredientesMap = remember(viewModel.ingredientesBase.value) {
        viewModel.ingredientesBase.value.associateBy { it.nombre }
    }

    var nombre by remember { mutableStateOf("") }
    var unidad by remember { mutableStateOf("") }
    var cantidad by remember { mutableStateOf("") }

    val costoUnidad = ingredientesMap[nombre]?.costoUnidad ?: 0.0
    val cantidadDouble = cantidad.toDoubleOrNull()
    val costoTotal = remember(cantidad, costoUnidad) {
        if (cantidadDouble != null) cantidadDouble * costoUnidad else 0.0
    }

    val textColor = if (isDarkTheme) Color.White else Color.DarkGray
    val gold = Color(0xFFC7A449)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            "Agregar nuevo ingrediente",
            style = MaterialTheme.typography.titleMedium,
            color = textColor
        )

        Spacer(modifier = Modifier.height(12.dp))

        BusquedaIngredientesConLista(
            ingredientes = ingredientesDisponibles,
            ingredienteSeleccionado = nombre,
            onSeleccionarIngrediente = { seleccionado ->
                nombre = seleccionado
                unidad = ingredientesMap[seleccionado]?.unidad ?: ""
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = unidad,
            onValueChange = { unidad = it },
            label = { Text("Unidad (gr, ml, unidad)", style = MaterialTheme.typography.bodyLarge) },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = textColor,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedLabelColor = textColor
            ),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = cantidad,
            onValueChange = { cantidad = it },
            label = { Text("Cantidad", style = MaterialTheme.typography.bodyLarge) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = textColor,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedLabelColor = textColor
            ),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Costo estimado: $${String.format("%.2f", costoTotal)}",
            style = MaterialTheme.typography.bodyLarge,
            color = textColor
        )

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(
                onClick = onCancelar,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color.Red
                )
            ) {
                Text("Cancelar", style = MaterialTheme.typography.bodyLarge)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Button(
                onClick = {
                    if (nombre.isNotBlank() && cantidadDouble != null) {
                        val ingrediente = IngredienteCosto(
                            nombre = nombre,
                            unidad = unidad,
                            cantidad = cantidadDouble,
                            costoUnidad = costoUnidad
                        )
                        onAgregar(ingrediente)
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = gold,
                    contentColor = textColor
                )
            ) {
                Text("Agregar", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}