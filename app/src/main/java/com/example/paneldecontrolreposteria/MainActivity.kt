package com.example.paneldecontrolreposteria

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.paneldecontrolreposteria.ui.theme.PanelDeControlReposteriaTheme
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val db = FirebaseFirestore.getInstance()

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
    val pedidoRepository = PedidoRepository()

    LaunchedEffect(Unit) {
        scope.launch {
            pedidos.addAll(pedidoRepository.obtenerPedidos())
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
                        Text("Producto: ${pedido.producto}")
                        Text("Estado: ${pedido.estado}")
                        Button(onClick = {
                            scope.launch {
                                pedidoRepository.actualizarEstadoPedido(pedido.id, "Entregado")
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