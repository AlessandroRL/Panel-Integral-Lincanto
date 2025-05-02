package com.example.paneldecontrolreposteria.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import com.example.paneldecontrolreposteria.model.Ingrediente
import com.example.paneldecontrolreposteria.viewmodel.IngredienteViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionIngredientesScreen(viewModel: IngredienteViewModel) {
    val ingredientes by viewModel.ingredientes.collectAsState()
    val scope = rememberCoroutineScope()

    var nombre by remember { mutableStateOf("") }
    var unidad by remember { mutableStateOf("") }
    var costoUnidad by remember { mutableStateOf("") }
    var errorMensaje by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Gestión de Ingredientes") }) }
    ) { padding ->
        Column(modifier = Modifier
            .padding(padding)
            .padding(16.dp)) {

            // Formulario para agregar ingrediente
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre del Ingrediente") },
                isError = errorMensaje != null && nombre.isBlank(),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = unidad,
                onValueChange = { unidad = it },
                label = { Text("Unidad (gr, ml, unidad, etc.)") },
                isError = errorMensaje != null && unidad.isBlank(),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = costoUnidad,
                onValueChange = { costoUnidad = it },
                label = { Text("Costo por Unidad") },
                isError = errorMensaje != null && costoUnidad.toDoubleOrNull() == null,
                modifier = Modifier.fillMaxWidth()
            )

            if (errorMensaje != null) {
                Text(errorMensaje!!, color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(onClick = {
                if (nombre.isBlank() || unidad.isBlank() || costoUnidad.toDoubleOrNull() == null) {
                    errorMensaje = "Complete todos los campos correctamente."
                    return@Button
                }

                val nuevoIngrediente = Ingrediente(
                    nombre = nombre.trim(),
                    unidad = unidad.trim(),
                    costoUnidad = costoUnidad.toDouble()
                )

                scope.launch {
                    viewModel.agregarIngrediente(nuevoIngrediente)
                    nombre = ""
                    unidad = ""
                    costoUnidad = ""
                    errorMensaje = null
                }
            }) {
                Text("Agregar Ingrediente")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Lista de ingredientes
            LazyColumn {
                items(ingredientes) { ingrediente ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Nombre: ${ingrediente.nombre}")
                                Text("Unidad: ${ingrediente.unidad}")
                                Text("Costo/U: ${ingrediente.costoUnidad}")
                            }
                            IconButton(onClick = {
                                scope.launch {
                                    viewModel.eliminarIngrediente(ingrediente.id)
                                }
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.Red)
                            }
                        }
                    }
                }
            }
        }
    }
}