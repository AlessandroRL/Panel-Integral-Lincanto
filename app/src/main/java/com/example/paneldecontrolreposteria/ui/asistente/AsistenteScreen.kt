package com.example.paneldecontrolreposteria.ui.asistente

import android.os.Build
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Mic
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.TextStyle
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AsistenteScreen(
    geminiViewModel: GeminiViewModel,
    controller: AsistenteController,
    speechRecognizerManager: SpeechRecognizerManager,
    activarEscuchaInicial: Boolean = false
) {
    val contexto = LocalContext.current
    var instruccion by remember { mutableStateOf("") }
    val mensajes by geminiViewModel.mensajes.observeAsState(emptyList())
    var isListening by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    var ttsDisponible by remember { mutableStateOf(false) }

    val isDarkTheme = isSystemInDarkTheme()
    val background = if (isDarkTheme) Color.Black else Color.White
    val textColor = if (isDarkTheme) Color.White else Color.DarkGray
    val gold = Color(0xFFC7A449)

    LaunchedEffect(activarEscuchaInicial) {
        if (activarEscuchaInicial) {
            isListening = true
            speechRecognizerManager.startListening()
        }
    }

    DisposableEffect(Unit) {
        val ttsLocal = TextToSpeech(contexto) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts?.setLanguage(Locale("es", "ES"))
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("AsistenteScreen", "Idioma español no soportado para TTS")
                } else {
                    ttsDisponible = true
                }
            } else {
                Log.e("AsistenteScreen", "Error al inicializar TTS")
            }
        }

        tts = ttsLocal

        onDispose {
            ttsLocal.stop()
            ttsLocal.shutdown()
            tts = null
        }
    }

    LaunchedEffect(mensajes.size) {
        mensajes.lastOrNull { it.emisor == GeminiViewModel.Emisor.ASISTENTE }?.let { mensaje ->
            if (geminiViewModel.hablarRespuestas && ttsDisponible) {
                tts?.speak(mensaje.contenido, TextToSpeech.QUEUE_FLUSH, null, "msg_id")
            }
        }
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .background(background)
        .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 88.dp)
        ) {
            Text(
                "Asistente Virtual",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = gold,
                modifier = Modifier
                    .padding(top = 16.dp, start = 16.dp)
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 60.dp)
                    .weight(1f)
                    .padding(8.dp),
                reverseLayout = true
            ) {
                items(mensajes.reversed()) { mensaje ->
                    val alignment = if (mensaje.emisor == GeminiViewModel.Emisor.USUARIO) Alignment.End else Alignment.Start
                    val bgColor = if (mensaje.emisor == GeminiViewModel.Emisor.USUARIO) gold else Color(0xFF444444)
                    val fgColor = if (mensaje.emisor == GeminiViewModel.Emisor.USUARIO) Color.Black else Color.White

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = if (alignment == Alignment.End) Arrangement.End else Arrangement.Start
                    ) {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = bgColor),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            modifier = Modifier.widthIn(max = 280.dp)
                        ) {
                            Text(
                                text = mensaje.contenido,
                                color = fgColor,
                                fontSize = 16.sp,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            }

            if (errorMessage != null) {
                Text(
                    text = "Error: $errorMessage",
                    color = Color.Red,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                )
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .background(background)
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = instruccion,
                    onValueChange = { instruccion = it },
                    label = { Text("Escribe tu instrucción", color = textColor) },
                    modifier = Modifier.weight(1f),
                    textStyle = TextStyle(color = textColor),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = gold,
                        unfocusedBorderColor = gold,
                        cursorColor = gold
                    ),
                    singleLine = true,
                    trailingIcon = {
                        IconButton(
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
                            enabled = instruccion.isNotBlank()
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Enviar",
                                tint = if (instruccion.isNotBlank()) gold else Color.Gray
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        isListening = true
                        errorMessage = null
                        speechRecognizerManager.startListening()
                    },
                    modifier = Modifier
                        .size(54.dp)
                        .background(
                            if (isListening) Color.Green else gold,
                            shape = CircleShape
                        )
                ) {
                    Icon(Icons.Default.Mic, contentDescription = "Hablar", tint = Color.Black)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Switch(
                    checked = geminiViewModel.hablarRespuestas,
                    onCheckedChange = { geminiViewModel.hablarRespuestas = it },
                    modifier = Modifier.scale(0.8f),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = gold,
                        checkedTrackColor = gold.copy(alpha = 0.5f)
                    )
                )
                Text(
                    "Leer respuestas en voz alta",
                    modifier = Modifier.padding(start = 8.dp),
                    color = textColor
                )
            }
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

        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {}
            override fun onDone(utteranceId: String?) {}
            override fun onError(utteranceId: String?) {
                Log.e("AsistenteTTS", "Error al hablar")
            }
        })

        onDispose {
            speechRecognizerManager.destroy()
            tts?.stop()
            tts?.shutdown()
        }
    }
}