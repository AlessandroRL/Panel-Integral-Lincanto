package com.example.paneldecontrolreposteria

import EditarPedidoScreen
import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.SmartToy
import com.example.paneldecontrolreposteria.screens.AgregarPedidoScreen
import com.example.paneldecontrolreposteria.screens.GestionIngredientesScreen
import com.example.paneldecontrolreposteria.screens.GestionPedidoScreen
import com.example.paneldecontrolreposteria.viewmodel.IngredienteViewModel
import com.example.paneldecontrolreposteria.viewmodel.PedidoViewModel
import kotlin.io.encoding.ExperimentalEncodingApi

class MainActivity : ComponentActivity() {
    private val pedidoViewModel: PedidoViewModel by viewModels()

    @OptIn(ExperimentalEncodingApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val pedidoViewModel: PedidoViewModel = viewModel()
            val selectedIndex = remember { mutableIntStateOf(0) }
            val ingredienteViewModel = remember { IngredienteViewModel() }
            MainApp(pedidoViewModel, ingredienteViewModel)

            Scaffold(
                bottomBar = {
                    NavigationBar {
                        NavigationBarItem(
                            selected = selectedIndex.intValue == 0,
                            onClick = { selectedIndex.intValue = 0 },
                            icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Pedidos") },
                            label = { Text("Pedidos") }
                        )
                        NavigationBarItem(
                            selected = selectedIndex.intValue == 1,
                            onClick = { selectedIndex.intValue = 1 },
                            icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Productos") },
                            label = { Text("Productos") }
                        )
                        NavigationBarItem(
                            selected = selectedIndex.intValue == 2,
                            onClick = { selectedIndex.intValue = 2 },
                            icon = { Icon(Icons.Filled.Mic, contentDescription = "Asistente") },
                            label = { Text("Asistente") }
                        )
                    }
                }
            ) { padding ->
                Box(modifier = Modifier.padding(padding)) {
                    when (selectedIndex.intValue) {
                        0 -> AppNavigation(viewModel = pedidoViewModel) // Panel de gestión de pedidos
                        1 -> GestionIngredientesScreen(ingredienteViewModel)  // Gestión de ingredientes, productos y costos
                        2 -> Text("Asistente Virtual (en desarrollo)")
                    }
                }
            }
        }
    }
}

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun AppNavigation(viewModel: PedidoViewModel) {
    val navController: NavHostController = rememberNavController()

    NavHost(navController = navController, startDestination = "gestionPedidos") {
        composable("gestionPedidos") { GestionPedidoScreen(navController, viewModel) }
        composable("agregarPedido") { AgregarPedidoScreen(viewModel) { navController.popBackStack() } }
        composable("editarPedido/{pedidoId}") { backStackEntry ->
            val pedidoId = backStackEntry.arguments?.getString("pedidoId")
            val pedido = viewModel.pedidos.value.find { it.id == pedidoId }

            if (pedido != null) {
                EditarPedidoScreen(viewModel, pedido) {
                    navController.popBackStack()
                }
            } else {
                Text("Pedido no encontrado.")
            }
        }
    }
}

@Composable
fun MainApp(
    pedidoViewModel: PedidoViewModel,
    ingredienteViewModel: IngredienteViewModel
) {
    val navController = rememberNavController()
    val items = listOf("Pedidos", "Ingredientes", "Asistente")
    val routes = listOf("gestionPedidos", "gestionIngredientes", "asistenteVirtual")

    Scaffold(
        bottomBar = {
            NavigationBar {
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = navController.currentDestination?.route == routes[index],
                        onClick = { navController.navigate(routes[index]) },
                        icon = {
                            Icon(
                                imageVector = when (item) {
                                    "Pedidos" -> Icons.AutoMirrored.Filled.List
                                    "Ingredientes" -> Icons.Default.Kitchen
                                    else -> Icons.Default.SmartToy
                                },
                                contentDescription = item
                            )
                        },
                        label = { Text(item) }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "gestionPedidos",
            modifier = Modifier.padding(padding)
        ) {
            composable("gestionPedidos") {
                AppNavigation(pedidoViewModel)
            }
            composable("gestionIngredientes") {
                GestionIngredientesScreen(viewModel = ingredienteViewModel)
            }
            composable("asistenteVirtual") {
                Text("Aquí irá el asistente virtual con IA") // Temporal
            }
        }
    }
}