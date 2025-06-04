package com.example.paneldecontrolreposteria.ui.asistente

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.paneldecontrolreposteria.ui.asistente.voice.SpeechRecognizerManager
import com.example.paneldecontrolreposteria.viewmodel.GeminiViewModel
import androidx.compose.foundation.lazy.items

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AsistenteScreen(
    geminiViewModel: GeminiViewModel,
    controller: AsistenteController,
    speechRecognizerManager: SpeechRecognizerManager
) {
    val contexto = LocalContext.current
    var instruccion by remember { mutableStateOf("") }
    val mensajes by geminiViewModel.mensajes.observeAsState(emptyList())
    var isListening by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Asistente Virtual", fontSize = 22.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(8.dp),
            reverseLayout = true
        ) {
            items(mensajes.reversed()) { mensaje ->
                val alignment = if (mensaje.emisor == GeminiViewModel.Emisor.USUARIO) Alignment.CenterEnd else Alignment.CenterStart
                val backgroundColor = if (mensaje.emisor == GeminiViewModel.Emisor.USUARIO) Color(0xFFD1E8FF) else Color(0xFFE1FFC7)

                Box(
                    contentAlignment = alignment,
                    modifier = Modifier.fillMaxWidth().padding(4.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = backgroundColor),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.widthIn(max = 300.dp)
                    ) {
                        Text(
                            text = mensaje.contenido,
                            modifier = Modifier.padding(12.dp),
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }

        Column {
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
                                if (resultadoFinal.length < 60) {
                                    Toast.makeText(contexto, resultadoFinal, Toast.LENGTH_LONG).show()
                                }
                            }
                            instruccion = ""
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = instruccion.isNotBlank()
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Enviar")
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
        }

        errorMessage?.let {
            Spacer(modifier = Modifier.height(8.dp))
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
                        if (resultadoFinal.length < 60) {
                            Toast.makeText(contexto, resultadoFinal, Toast.LENGTH_LONG).show()
                        }
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