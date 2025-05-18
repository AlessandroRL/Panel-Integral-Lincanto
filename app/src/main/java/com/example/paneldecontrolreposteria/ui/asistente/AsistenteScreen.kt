package com.example.paneldecontrolreposteria.ui.asistente

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.paneldecontrolreposteria.ui.asistente.core.AsistenteController
import com.example.paneldecontrolreposteria.viewmodel.AsistenteViewModel

@Composable
fun AsistenteScreen(
    textoInicial: String = "",
    viewModel: AsistenteViewModel = viewModel(),
    asistenteController: AsistenteController
) {
    val context = LocalContext.current

    val textoReconocido by viewModel.textoReconocido
    val estaEscuchando by viewModel.estaEscuchando
    val errorReconocimiento by viewModel.errorReconocimiento
    var respuesta = asistenteController.procesarTexto(textoInicial)

    LaunchedEffect(textoInicial) {
        if (textoInicial.isNotBlank()) {
            viewModel.establecerTextoReconocido(textoInicial)
            viewModel.establecerRespuesta(respuesta)
        }
    }

    LaunchedEffect(textoReconocido) {
        if (textoReconocido.isNotBlank()) {
            respuesta = asistenteController.procesarTexto(textoReconocido)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            Text(
                text = "Asistente Inteligente",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (estaEscuchando) {
                Text(
                    text = "🎙️ Escuchando...",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            if (textoReconocido.isNotBlank()) {
                Text(
                    text = "📝 Comando recibido:\n\n\"$textoReconocido\"",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
                Divider()
                Text(
                    text = "🧠 Respuesta simulada:",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(top = 12.dp)
                )
                Text(
                    text = procesarSimulacion(textoReconocido),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.DarkGray,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            if (errorReconocimiento.isNotBlank()) {
                Text(
                    text = "❌ Error: $errorReconocimiento",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = { viewModel.iniciarEscucha(context) },
                enabled = !estaEscuchando
            ) {
                Text("🎤 Escuchar")
            }

            OutlinedButton(onClick = { viewModel.resetear() }) {
                Text("🔄 Limpiar")
            }
        }
    }
}

private fun procesarSimulacion(texto: String): String {
    return when {
        texto.contains("agrega", ignoreCase = true) && texto.contains("pedido") ->
            "Entendido. Puedes usar el botón de agregar en la sección de Pedidos."
        texto.contains("elimina", ignoreCase = true) && texto.contains("ingrediente") ->
            "Recuerda que puedes eliminar un ingrediente desde la lista de ingredientes."
        texto.contains("cuánto cuesta", ignoreCase = true) ->
            "Puedes ver el costo estimado en la sección de Costos."
        else -> "Estoy procesando tu orden. Próximamente podré ejecutar acciones automáticas."
    }
}