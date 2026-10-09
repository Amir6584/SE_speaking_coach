package se.example.swedishcoach

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer

class MixedSpeechRecognizer(context: Context, private val callbacks: Callbacks) {
    interface Callbacks {
        fun onReady() {}
        fun onSpeechStarted() {}
        fun onSpeechEnded() {}
        fun onPartial(text: String) {}
        fun onFinal(text: String) {}
        fun onLanguage(tag: String, info: String) {}
        fun onStatus(text: String) {}
        fun onError(text: String, recoverable: Boolean) {}
    }

    private val appContext = context.applicationContext
    private val handler = Handler(Looper.getMainLooper())
    private var recognizer: SpeechRecognizer? = null
    private var running = false
    private var restartPosted = false

    fun start() {
        if (running) return
        if (!SpeechRecognizer.isRecognitionAvailable(appContext)) {
            callbacks.onError("No Android speech recognition service is available.", false)
            return
        }
        running = true
        if (recognizer == null) {
            recognizer = SpeechRecognizer.createSpeechRecognizer(appContext).also { it.setRecognitionListener(listener) }
        }
        startNow()
    }

    fun stop() {
        running = false
        restartPosted = false
        handler.removeCallbacksAndMessages(null)
        runCatching { recognizer?.cancel() }
        callbacks.onStatus("Stopped")
    }

    fun destroy() {
        running = false
        handler.removeCallbacksAndMessages(null)
        runCatching { recognizer?.destroy() }
        recognizer = null
    }

    private fun intent() = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "sv-SE")
        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
        putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, false)
        if (Build.VERSION.SDK_INT >= 33) {
            putExtra(RecognizerIntent.EXTRA_ENABLE_FORMATTING, RecognizerIntent.FORMATTING_OPTIMIZE_LATENCY)
            putExtra(RecognizerIntent.EXTRA_HIDE_PARTIAL_TRAILING_PUNCTUATION, true)
        }
        if (Build.VERSION.SDK_INT >= 34) {
            val languages = arrayListOf("sv-SE", "en-US")
            putExtra(RecognizerIntent.EXTRA_ENABLE_LANGUAGE_DETECTION, true)
            putStringArrayListExtra(RecognizerIntent.EXTRA_LANGUAGE_DETECTION_ALLOWED_LANGUAGES, languages)
            putExtra(RecognizerIntent.EXTRA_ENABLE_LANGUAGE_SWITCH, RecognizerIntent.LANGUAGE_SWITCH_QUICK_RESPONSE)
            putStringArrayListExtra(RecognizerIntent.EXTRA_LANGUAGE_SWITCH_ALLOWED_LANGUAGES, languages)
        }
        if (Build.VERSION.SDK_INT >= 35) putExtra(RecognizerIntent.EXTRA_LANGUAGE_SWITCH_MAX_SWITCHES, 8)
    }

    private fun startNow() {
        if (!running) return
        restartPosted = false
        callbacks.onStatus(if (Build.VERSION.SDK_INT >= 34) "Listening — Swedish + English auto-switch" else "Listening — Swedish primary")
        runCatching { recognizer?.startListening(intent()) }.onFailure {
            running = false
            callbacks.onError("Could not start recognition: ${it.message}", false)
        }
    }

    private fun restart(delay: Long = 220L) {
        if (!running || restartPosted) return
        restartPosted = true
        handler.postDelayed({ if (running) startNow() }, delay)
    }

    private fun best(bundle: Bundle?): String = bundle?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()?.trim().orEmpty()

    private val listener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) = callbacks.onReady()
        override fun onBeginningOfSpeech() = callbacks.onSpeechStarted()
        override fun onEndOfSpeech() = callbacks.onSpeechEnded()
        override fun onRmsChanged(rmsdB: Float) = Unit
        override fun onBufferReceived(buffer: ByteArray?) = Unit
        override fun onEvent(eventType: Int, params: Bundle?) = Unit
        override fun onPartialResults(partialResults: Bundle?) { best(partialResults).takeIf { it.isNotBlank() }?.let(callbacks::onPartial) }
        override fun onResults(results: Bundle?) { best(results).takeIf { it.isNotBlank() }?.let(callbacks::onFinal); restart(180L) }
        override fun onError(error: Int) {
            if (!running) return
            val recoverable = error in setOf(SpeechRecognizer.ERROR_NO_MATCH, SpeechRecognizer.ERROR_SPEECH_TIMEOUT, SpeechRecognizer.ERROR_RECOGNIZER_BUSY, SpeechRecognizer.ERROR_NETWORK_TIMEOUT, SpeechRecognizer.ERROR_SERVER_DISCONNECTED)
            val message = when (error) {
                SpeechRecognizer.ERROR_NO_MATCH -> "Listening — no clear match"
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Listening — no speech yet"
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognizer busy — retrying"
                SpeechRecognizer.ERROR_NETWORK -> "Speech recognition network error"
                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Speech recognition network timeout"
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission is missing"
                SpeechRecognizer.ERROR_LANGUAGE_NOT_SUPPORTED -> "Swedish/English recognition not supported"
                else -> "Speech recognition error $error"
            }
            callbacks.onError(message, recoverable)
            if (recoverable) restart(if (error == SpeechRecognizer.ERROR_RECOGNIZER_BUSY) 650L else 300L) else running = false
        }
        override fun onLanguageDetection(results: Bundle) {
            if (Build.VERSION.SDK_INT < 34) return
            val tag = results.getString(SpeechRecognizer.DETECTED_LANGUAGE).orEmpty()
            if (tag.isNotBlank()) callbacks.onLanguage(tag, "detected")
        }
    }
}
