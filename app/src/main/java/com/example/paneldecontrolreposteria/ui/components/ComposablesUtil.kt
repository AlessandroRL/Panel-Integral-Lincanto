package com.example.paneldecontrolreposteria.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun DropdownBusquedaIngredientes(
    ingredientes: List<String>,
    ingredienteSeleccionado: String,
    onSeleccionarIngrediente: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var texto by remember { mutableStateOf(ingredienteSeleccionado) }

    val ingredientesFiltrados = ingredientes.filter {
        it.contains(texto, ignoreCase = true)
    }

    Column {
        OutlinedTextField(
            value = texto,
            onValueChange = {
                texto = it
                expanded = true
            },
            label = { Text("Buscar ingrediente") },
            modifier = Modifier
                .fillMaxWidth(),
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Mostrar opciones",
                    modifier = Modifier.clickable { expanded = true }
                )
            },
            singleLine = true
        )

        DropdownMenu(
            expanded = expanded && ingredientesFiltrados.isNotEmpty(),
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
        ) {
            ingredientesFiltrados.forEach { ingrediente ->
                DropdownMenuItem(
                    text = { Text(ingrediente) },
                    onClick = {
                        texto = ingrediente
                        onSeleccionarIngrediente(ingrediente)
                        expanded = false
                    }
                )
            }
        }
    }
}