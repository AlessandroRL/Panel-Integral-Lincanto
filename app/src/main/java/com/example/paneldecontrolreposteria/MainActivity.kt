package com.example.paneldecontrolreposteria

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.paneldecontrolreposteria.screens.AgregarPedidoScreen
import com.example.paneldecontrolreposteria.screens.GestionPedidoScreen
import com.example.paneldecontrolreposteria.viewmodel.PedidoViewModel

class MainActivity : ComponentActivity() {
    private val pedidoViewModel: PedidoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppNavigation(pedidoViewModel)
        }
    }
}

@Composable
fun AppNavigation(viewModel: PedidoViewModel) {
    val navController: NavHostController = rememberNavController()

    NavHost(navController = navController, startDestination = "gestionPedidos") {
        composable("gestionPedidos") { GestionPedidoScreen(navController, viewModel) }
        composable("agregarPedido") { AgregarPedidoScreen(viewModel) { navController.popBackStack() } }
    }
}


