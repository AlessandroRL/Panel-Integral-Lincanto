package com.example.paneldecontrolreposteria

import com.example.paneldecontrolreposteria.screens.EditarPedidoScreen
import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.paneldecontrolreposteria.screens.AgregarPedidoScreen
import com.example.paneldecontrolreposteria.screens.GestionIngredientesScreen
import com.example.paneldecontrolreposteria.screens.GestionPedidoScreen
import com.example.paneldecontrolreposteria.ui.asistente.AsistenteScreen
import com.example.paneldecontrolreposteria.ui.asistente.voice.SpeechRecognizerManager
import com.example.paneldecontrolreposteria.viewmodel.IngredienteViewModel
import com.example.paneldecontrolreposteria.viewmodel.PedidoViewModel
import kotlin.io.encoding.ExperimentalEncodingApi
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.paneldecontrolreposteria.ui.asistente.AsistenteController
import com.example.paneldecontrolreposteria.ui.ai.GeminiCommandInterpreter
import com.example.paneldecontrolreposteria.viewmodel.GeminiViewModel
import com.example.paneldecontrolreposteria.viewmodel.ProductoViewModel

class MainActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.O)
    @OptIn(ExperimentalEncodingApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)
        splashScreen.setKeepOnScreenCondition {
            false
        }

        val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                Log.d("Permissions", "Permiso de grabación concedido")
            } else {
                Log.e("Permissions", "Permiso de grabación denegado")
            }
        }

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }

        setContent {
            val navController = rememberNavController()
            MainApp(navController)
        }
    }
}

@SuppressLint("StateFlowValueCalledInComposition")
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainApp(navController: NavHostController) {
    val items = listOf("Pedidos", "Gestion", "Asistente")
    val routes = listOf("gestionPedidos", "gestionIngredientes", "asistenteVirtual")
    val geminiViewModel: GeminiViewModel = viewModel()
    val pedidoViewModel: PedidoViewModel = viewModel()
    val ingredienteViewModel: IngredienteViewModel = viewModel()
    val productoViewModel: ProductoViewModel = viewModel()

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
                GestionPedidoScreen(
                    navController = navController,
                    viewModel = pedidoViewModel
                )
            }
            composable("agregarPedido") {
                AgregarPedidoScreen(viewModel = pedidoViewModel) {
                    navController.popBackStack()
                }
            }
            composable("editarPedido/{pedidoId}") { backStackEntry ->
                val pedidoId = backStackEntry.arguments?.getString("pedidoId")
                val pedido = pedidoViewModel.pedidos.value.find { it.id == pedidoId }

                if (pedido != null) {
                    EditarPedidoScreen(viewModel = pedidoViewModel, pedido = pedido) {
                        navController.popBackStack()
                    }
                } else {
                    Text("Pedido no encontrado.")
                }
            }
            composable("gestionIngredientes") {
                GestionIngredientesScreen(navController = navController)
            }
            composable("asistenteVirtual?activarEscuchaInicial={activarEscuchaInicial}",
                arguments = listOf(navArgument("activarEscuchaInicial") {
                    type = NavType.BoolType
                    defaultValue = false
                })
            ) { backStackEntry ->
                val activarEscuchaInicial = backStackEntry.arguments?.getBoolean("activarEscuchaInicial") == true
                val context = LocalContext.current

                val speechRecognizerManager = remember {
                    SpeechRecognizerManager(
                        context = context,
                        onResult = {},
                        onError = { error ->
                            Log.e("SpeechRecognizer", "Error: $error")
                        }
                    )
                }

                AsistenteScreen(
                    geminiViewModel = geminiViewModel,
                    controller = AsistenteController(
                        interpreter = GeminiCommandInterpreter(
                            pedidoViewModel = pedidoViewModel,
                            ingredienteViewModel = ingredienteViewModel,
                            productoViewModel = productoViewModel,
                        ),
                    ),
                    speechRecognizerManager = speechRecognizerManager,
                    activarEscuchaInicial = activarEscuchaInicial
                )
            }
        }
    }
}