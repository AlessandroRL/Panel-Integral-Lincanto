package com.example.paneldecontrolreposteria

import EditarPedidoScreen
import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.paneldecontrolreposteria.screens.AgregarPedidoScreen
import com.example.paneldecontrolreposteria.screens.GestionIngredientesScreen
import com.example.paneldecontrolreposteria.screens.GestionPedidoScreen
import com.example.paneldecontrolreposteria.ui.asistente.AsistenteScreen
import com.example.paneldecontrolreposteria.ui.asistente.core.AsistenteController
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

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                1
            )
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
                        if (result.isNotBlank()) {
                            recognizedText.value = result
                        } else {
                            recognizedText.value = "No se reconoció ningún comando."
                        }
                        navController.navigate("asistenteVirtual")
                    }
                    ,
                    onError = { error ->
                        Log.e("SpeechRecognizer", "Error: $error")
                    }
                )
            }
            MainApp(pedidoViewModel, ingredienteViewModel, recognizedText.value, navController, speechRecognizerManager)
        }
    }

    @Deprecated("This method has been deprecated in favor of using the Activity Result API\n      which brings increased type safety via an {@link ActivityResultContract} and the prebuilt\n      contracts for common intents available in\n      {@link androidx.activity.result.contract.ActivityResultContracts}, provides hooks for\n      testing, and allow receiving results in separate, testable classes independent from your\n      activity. Use\n      {@link #registerForActivityResult(ActivityResultContract, ActivityResultCallback)} passing\n      in a {@link RequestMultiplePermissions} object for the {@link ActivityResultContract} and\n      handling the result in the {@link ActivityResultCallback#onActivityResult(Object) callback}.")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d("Permissions", "Permiso de grabación concedido")
        } else {
            Log.e("Permissions", "Permiso de grabación denegado")
        }
    }
}

@SuppressLint("StateFlowValueCalledInComposition")
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainApp(
    pedidoViewModel: PedidoViewModel,
    ingredienteViewModel: IngredienteViewModel,
    recognizedText: String,
    navController: NavHostController,
    speechRecognizerManager: SpeechRecognizerManager
)
 {
    val items = listOf("Pedidos", "Gestion", "Asistente")
    val routes = listOf("gestionPedidos", "gestionIngredientes", "asistenteVirtual")
     val asistenteController = remember { AsistenteController(navController) }


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
                    viewModel = pedidoViewModel,
                    speechRecognizerManager = speechRecognizerManager
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
                GestionIngredientesScreen()
            }
            composable("asistenteVirtual") {
                AsistenteScreen(
                    textoInicial = recognizedText,
                    asistenteController = asistenteController
                )
            }
        }
    }
}