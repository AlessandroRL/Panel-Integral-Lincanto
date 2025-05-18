package com.example.paneldecontrolreposteria.ui.asistente.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log

class SpeechRecognizerManager(
    private val context: Context,
    private val onResult: (String) -> Unit,
    private val onError: (String) -> Unit
) {
    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening = false

    private val intent: Intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-ES")
        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
    }

    private val recognitionListener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            Log.d("SpeechRecognizer", "✅ Listo para escuchar...")
        }

        override fun onBeginningOfSpeech() {
            Log.d("SpeechRecognizer", "🎙️ Comenzó la entrada de voz")
        }

        override fun onRmsChanged(rmsdB: Float) {}

        override fun onBufferReceived(buffer: ByteArray?) {}

        override fun onEndOfSpeech() {
            Log.d("SpeechRecognizer", "⏹️ Fin de la voz")
        }

        override fun onError(error: Int) {
            isListening = false
            val message = when (error) {
                SpeechRecognizer.ERROR_AUDIO -> "Error de audio"
                SpeechRecognizer.ERROR_CLIENT -> "Error del cliente"
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Permiso insuficiente"
                SpeechRecognizer.ERROR_NETWORK -> "Error de red"
                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Tiempo de red agotado"
                SpeechRecognizer.ERROR_NO_MATCH -> "No se entendió lo dicho"
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Reconocedor ocupado"
                SpeechRecognizer.ERROR_SERVER -> "Error del servidor"
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Tiempo de espera agotado"
                else -> "Error desconocido: $error"
            }
            Log.e("SpeechRecognizer", "❌ Error: $message")
            onError(message)
            destroy()
        }

        override fun onResults(results: Bundle?) {
            isListening = false
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val recognizedText = matches?.firstOrNull().orEmpty()
            Log.d("SpeechRecognizer", "✅ Resultado final: $recognizedText")
            onResult(recognizedText)
            destroy()
        }

        override fun onPartialResults(partialResults: Bundle?) {
            val partial = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val preview = partial?.firstOrNull().orEmpty()
            Log.d("SpeechRecognizer", "📝 Resultado parcial: $preview")
        }

        override fun onEvent(eventType: Int, params: Bundle?) {}
    }

    fun startListening() {
        if (isListening) {
            Log.d("SpeechRecognizer", "⚠️ Ya se está escuchando, cancelando anterior...")
            stopListening()
        }

        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(recognitionListener)
                startListening(intent)
            }
            isListening = true
            Log.d("SpeechRecognizer", "🔊 Iniciando escucha...")
        } else {
            Log.e("SpeechRecognizer", "❌ Reconocimiento de voz no disponible")
            onError("Reconocimiento de voz no disponible en este dispositivo")
        }
    }

    fun stopListening() {
        Log.d("SpeechRecognizer", "🛑 stopListening() llamado")
        speechRecognizer?.stopListening()
        destroy()
    }

    fun destroy() {
        Log.d("SpeechRecognizer", "🧹 Liberando recursos del SpeechRecognizer")
        speechRecognizer?.destroy()
        speechRecognizer = null
        isListening = false
    }
}