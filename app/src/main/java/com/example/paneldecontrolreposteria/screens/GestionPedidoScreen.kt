package com.example.paneldecontrolreposteria.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.paneldecontrolreposteria.model.Pedido
import com.example.paneldecontrolreposteria.viewmodel.PedidoViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionPedidoScreen(viewModel: PedidoViewModel) {
    val pedidos by viewModel.pedidos.collectAsState()
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Gestión de Pedidos") }) }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            items(pedidos) { pedido ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Cliente: ${pedido.cliente}", style = MaterialTheme.typography.titleMedium)
                        Text("Productos: ${pedido.productos.joinToString(", ")}")
                        Text("Estado: ${pedido.estado}")
                        Button(onClick = {
                            scope.launch {
                                viewModel.actualizarEstadoPedido(pedido.id, "Entregado")
                            }
                        }) {
                            Text("Marcar como Entregado")
                        }
                    }
                }
            }
        }
    }
}
