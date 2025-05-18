package com.example.paneldecontrolreposteria

import EditarPedidoScreen
import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.compose.runtime.mutableStateOf
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.paneldecontrolreposteria.screens.AgregarPedidoScreen
import com.example.paneldecontrolreposteria.screens.GestionIngredientesScreen
import com.example.paneldecontrolreposteria.screens.GestionPedidoScreen
import com.example.paneldecontrolreposteria.ui.asistente.AsistenteScreen
import com.example.paneldecontrolreposteria.ui.asistente.voice.SpeechRecognizerManager
import com.example.paneldecontrolreposteria.viewmodel.IngredienteViewModel
import com.example.paneldecontrolreposteria.viewmodel.PedidoViewModel
import kotlin.io.encoding.ExperimentalEncodingApi

class MainActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.O)
    @OptIn(ExperimentalEncodingApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)
        splashScreen.setKeepOnScreenCondition {
            false
        }

        setContent {
            val pedidoViewModel: PedidoViewModel = viewModel()
            val ingredienteViewModel = remember { IngredienteViewModel() }
            val recognizedText = remember { mutableStateOf("") }
            val context = this
            val navController = rememberNavController()
            val speechRecognizerManager = remember {
                SpeechRecognizerManager(
                    context = context,
                    onResult = { result ->
                        recognizedText.value = result
                        navController.navigate("asistenteVirtual")
                    },
                    onError = { error ->
                        Log.e("SpeechRecognizer", "Error: $error")
                    }
                )
            }
            MainApp(pedidoViewModel, ingredienteViewModel, recognizedText.value, navController)
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

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainApp(
    pedidoViewModel: PedidoViewModel,
    ingredienteViewModel: IngredienteViewModel,
    recognizedText: String,
    navController: NavHostController
)
 {
    val items = listOf("Pedidos", "Gestion", "Asistente")
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
                                    "Gestion" -> Icons.Default.ShoppingCart
                                    else -> Icons.Filled.Mic
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
                GestionIngredientesScreen()
            }
            composable("asistenteVirtual") {
                AsistenteScreen(textoInicial = recognizedText)
            }
        }
    }
}