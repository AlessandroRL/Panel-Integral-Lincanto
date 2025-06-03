package com.example.paneldecontrolreposteria.ui.asistente

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.paneldecontrolreposteria.ui.asistente.voice.SpeechRecognizerManager
import com.example.paneldecontrolreposteria.viewmodel.GeminiViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AsistenteScreen(
    geminiViewModel: GeminiViewModel,
    controller: AsistenteController,
    speechRecognizerManager: SpeechRecognizerManager
) {
    val contexto = LocalContext.current
    var instruccion by remember { mutableStateOf("") }
    val respuesta by geminiViewModel.respuesta.observeAsState("")
    var isListening by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Asistente Virtual", fontSize = 22.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = instruccion,
            onValueChange = { instruccion = it },
            label = { Text("Escribe tu instrucción") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = {
                    if (instruccion.isNotBlank()) {
                        geminiViewModel.procesarYEjecutar(
                            texto = instruccion,
                            asistenteController = controller
                        ) { resultadoFinal ->
                            Toast.makeText(contexto, resultadoFinal, Toast.LENGTH_LONG).show()
                        }
                        instruccion = ""
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = instruccion.isNotBlank()
            ) {
                Icon(Icons.Default.Send, contentDescription = "Enviar")
                Spacer(Modifier.width(4.dp))
                Text("Enviar")
            }

            Spacer(modifier = Modifier.width(12.dp))

            IconButton(
                onClick = {
                    isListening = true
                    errorMessage = null
                    speechRecognizerManager.startListening()
                },
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        if (isListening) Color.Green else MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    )
            ) {
                Icon(Icons.Default.Mic, contentDescription = "Hablar", tint = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("Respuesta del Asistente:", fontWeight = FontWeight.Medium)
        Text(
            text = if (respuesta.isNotBlank()) respuesta else "Aquí aparecerá la respuesta del asistente...",
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .background(Color(0xFFF0F0F0), shape = RoundedCornerShape(8.dp))
                .padding(12.dp),
            fontSize = 16.sp
        )

        errorMessage?.let {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Error: $it",
                color = Color.Red,
                fontWeight = FontWeight.Bold
            )
        }
    }

    DisposableEffect(Unit) {
        speechRecognizerManager.apply {
            onResult = { result ->
                isListening = false
                instruccion = result
                if (instruccion.isNotBlank()) {
                    geminiViewModel.procesarYEjecutar(
                        texto = instruccion,
                        asistenteController = controller
                    ) { resultadoFinal ->
                        Toast.makeText(contexto, resultadoFinal, Toast.LENGTH_LONG).show()
                    }
                    instruccion = ""
                }
            }
            onError = { error ->
                isListening = false
                errorMessage = error
            }
        }
        onDispose {
            speechRecognizerManager.destroy()
        }
    }
}