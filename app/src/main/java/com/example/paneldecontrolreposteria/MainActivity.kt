package com.example.paneldecontrolreposteria

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.paneldecontrolreposteria.model.Pedido
import com.example.paneldecontrolreposteria.viewmodel.PedidoRepository
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PedidoScreen()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PedidoScreen() {
    val pedidos = remember { mutableStateListOf<Pedido>() }
    val scope = rememberCoroutineScope()
    val pedidoRepository = remember { PedidoRepository() }

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val pedidosObtenidos = pedidoRepository.obtenerPedidos()
                pedidos.clear()
                pedidos.addAll(pedidosObtenidos)
            } catch (e: Exception) {
                println("Error al obtener pedidos: ${e.message}")
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Gestión de Pedidos") })
        }
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
                                try {
                                    pedidoRepository.actualizarEstadoPedido(pedido.id, "Entregado")
                                    pedido.estado = "Entregado"
                                } catch (e: Exception) {
                                    println("Error al actualizar estado: ${e.message}")
                                }
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

