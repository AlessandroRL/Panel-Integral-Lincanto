package com.example.paneldecontrolreposteria.ui.asistente.core

import android.util.Log
import androidx.navigation.NavController
import com.example.paneldecontrolreposteria.ui.asistente.voice.TextInputInterpreter
import com.example.paneldecontrolreposteria.ui.asistente.voice.Comando

class AsistenteController(
    private val navController: NavController
) {
    private val interprete = TextInputInterpreter()

    /**
     * Analiza el texto recibido, interpreta el comando y ejecuta la acción correspondiente.
     */
    fun procesarTexto(texto: String): String {
        val comando = interprete.interpretar(texto)
        Log.d("AsistenteController", "Comando interpretado: $comando")

        return when (comando) {
            is Comando.AbrirAgregarPedido -> {
                navController.navigate("agregarPedido")
                "Abriendo pantalla para agregar un pedido."
            }

            is Comando.AbrirSeccionPedidos -> {
                navController.navigate("gestionPedidos")
                "Abriendo la sección de pedidos."
            }

            is Comando.AbrirSeccionIngredientes -> {
                navController.navigate("gestionIngredientes")
                "Abriendo la sección de ingredientes."
            }

            is Comando.AbrirSeccionCostos -> {
                navController.navigate("gestionCostos")
                "Abriendo la sección de costos."
            }

            is Comando.EliminarIngrediente -> {
                "Eliminar ingrediente: ${comando.nombre} (acción simulada)"
            }

            is Comando.EditarProducto -> {
                "Editar producto: ${comando.nombre} (acción simulada)"
            }

            is Comando.NoReconocido -> {
                "No entendí tu comando. Intenta algo como: 'Agrega un pedido' o 'Elimina el ingrediente azúcar'."
            }
        }
    }
}
