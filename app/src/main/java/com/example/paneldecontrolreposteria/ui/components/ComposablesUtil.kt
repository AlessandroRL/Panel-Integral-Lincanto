package com.example.paneldecontrolreposteria.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.items

@Composable
fun BusquedaIngredientesConLista(
    ingredientes: List<String>,
    ingredienteSeleccionado: String,
    onSeleccionarIngrediente: (String) -> Unit
) {
    var texto by remember { mutableStateOf(ingredienteSeleccionado) }
    var mostrarLista by remember { mutableStateOf(false) }
    var hizoFocus by remember { mutableStateOf(false) }

    val ingredientesFiltrados = remember(texto, mostrarLista, hizoFocus) {
        if (texto.isBlank() && mostrarLista && hizoFocus) {
            ingredientes
        } else {
            ingredientes.filter { it.contains(texto, ignoreCase = true) }
        }
    }

    Column {
        OutlinedTextField(
            value = texto,
            onValueChange = {
                texto = it
                mostrarLista = true
            },
            label = { Text("Buscar ingrediente") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    if (focusState.isFocused) {
                        mostrarLista = true
                        hizoFocus = true
                    } else {
                        mostrarLista = false
                    }
                }
        )

        if (mostrarLista && ingredientesFiltrados.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .heightIn(max = 200.dp)
            ) {
                items(ingredientesFiltrados) { ingrediente ->
                    Text(
                        text = ingrediente,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                texto = ingrediente
                                onSeleccionarIngrediente(ingrediente)
                                mostrarLista = false
                            }
                            .padding(12.dp)
                    )
                }
            }
        }
    }
}