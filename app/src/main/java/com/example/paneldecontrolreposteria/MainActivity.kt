package com.example.paneldecontrolreposteria

import EditarPedidoScreen
import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.paneldecontrolreposteria.screens.AgregarPedidoScreen
import com.example.paneldecontrolreposteria.screens.GestionPedidoScreen
import com.example.paneldecontrolreposteria.viewmodel.PedidoViewModel
import com.example.paneldecontrolreposteria.ui.components.BottomNavigationBar
import com.example.paneldecontrolreposteria.ui.navigation.BottomNavItem

class MainActivity : ComponentActivity() {
    private val pedidoViewModel: PedidoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            Scaffold(
                bottomBar = { BottomNavigationBar(navController) }
            ) { innerPadding ->
                NavHost(
                    navController = navController,
                    startDestination = BottomNavItem.Pedidos.route,
                    modifier = Modifier.padding(innerPadding)
                ) {
                    composable(BottomNavItem.Pedidos.route) {
                        GestionPedidoScreen(navController, viewModel = viewModel())
                    }
                    composable(BottomNavItem.Produccion.route) {
                        Text("Panel de Producción (Ingredientes, Productos, Costos)") // Temporal
                    }
                    composable(BottomNavItem.Asistente.route) {
                        Text("Asistente Virtual con IA") // Temporal
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


