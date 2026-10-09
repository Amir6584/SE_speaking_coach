package se.example.swedishcoach

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var status: TextView
    private lateinit var recognizerInfo: TextView
    private lateinit var grammarInfo: TextView
    private lateinit var live: TextView
    private lateinit var original: TextView
    private lateinit var words: TextView
    private lateinit var corrected: TextView
    private lateinit var start: Button
    private lateinit var progress: ProgressBar

    private val coach by lazy { CoachEngine(this) }
    private lateinit var speech: MixedSpeechRecognizer
    private var listening = false
    private var afterPermission = false
    private var partialJob: Job? = null
    private val finalChannel = Channel<String>(Channel.UNLIMITED)
    private val shown = linkedSetOf<String>()

    private val micPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted && afterPermission) {
            afterPermission = false
            begin()
        } else if (!granted) {
            setStatus("Microphone permission is required.")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildUi()

        speech = MixedSpeechRecognizer(this, object : MixedSpeechRecognizer.Callbacks {
            override fun onStatus(text: String) {
                setStatus(text)
            }

            override fun onReady() {
                recognizerInfo.text = recognizerDescription()
            }

            override fun onSpeechStarted() {
                setStatus("Listening — Swedish + English")
            }

            override fun onSpeechEnded() {
                setStatus("Finishing sentence…")
            }

            override fun onPartial(text: String) {
                live.text = text
                scheduleWordHelp(text)
            }

            override fun onFinal(text: String) {
                live.text = ""
                text.trim().takeIf { it.isNotBlank() }?.let { finalChannel.trySend(it) }
            }

            override fun onLanguage(tag: String, info: String) {
                recognizerInfo.text = "Recognizer: $tag • $info"
            }

            override fun onError(text: String, recoverable: Boolean) {
                setStatus(text)
                if (!recoverable) {
                    listening = false
                    start.text = "Start speaking"
                }
            }
        })

        start.setOnClickListener {
            if (listening) stop() else requestStart()
        }

        findViewById<Button>(CLEAR_BUTTON_ID).setOnClickListener {
            live.text = ""
            original.text = ""
            words.text = ""
            corrected.text = ""
            shown.clear()
        }

        lifecycleScope.launch {
            progress.visibility = View.VISIBLE
            start.isEnabled = false
            try {
                coach.prepare { message ->
                    runOnUiThread {
                        grammarInfo.text = message
                        setStatus(message)
                    }
                }
                grammarInfo.text = coach.grammarEngineDescription
                setStatus("Ready")
            } catch (t: Throwable) {
                setStatus("Model setup error: ${t.message}")
            } finally {
                progress.visibility = View.GONE
                start.isEnabled = true
            }
        }

        lifecycleScope.launch {
            for (sentence in finalChannel) {
                processSentence(sentence)
            }
        }
    }

    private fun requestStart() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            begin()
        } else {
            afterPermission = true
            micPermission.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    private fun begin() {
        if (listening) return
        listening = true
        start.text = "Stop"
        speech.start()
    }

    private fun stop() {
        listening = false
        partialJob?.cancel()
        speech.stop()
        live.text = ""
        start.text = "Start speaking"
    }

    private fun scheduleWordHelp(text: String) {
        partialJob?.cancel()
        partialJob = lifecycleScope.launch {
            delay(120)
            coach.findEnglishSuggestions(text).forEach(::showSuggestion)
        }
    }

    private suspend fun processSentence(sentence: String) {
        append(original, sentence)
        setStatus("Checking English words…")
        coach.findEnglishSuggestions(sentence) { showSuggestion(it) }
        setStatus("Correcting with Qwen3 0.6B…")
        val result = coach.correctSentence(sentence)
        grammarInfo.text = "Grammar: ${result.engine}"
        append(corrected, result.naturalSwedish)
        if (listening) setStatus("Listening — ready for next sentence")
    }

    private fun showSuggestion(suggestion: CoachEngine.WordSuggestion) {
        val key = "${suggestion.original.lowercase()}→${suggestion.swedish.lowercase()}"
        if (shown.add(key)) {
            append(words, "${suggestion.original} → ${suggestion.swedish}")
        }
    }

    private fun recognizerDescription(): String =
        if (Build.VERSION.SDK_INT >= 34) {
            "Recognizer: Swedish ↔ English auto-switch + partial results"
        } else {
            "Recognizer: Swedish primary + partial results"
        }

    private fun setStatus(message: String) {
        status.text = message
    }

    private fun append(view: TextView, text: String) {
        if (text.isBlank()) return
        if (view.text.isEmpty()) {
            view.text = text
        } else {
            view.append("\n$text")
        }
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    private fun label(text: String): TextView = TextView(this).apply {
        this.text = text
        textSize = 13f
        setPadding(0, dp(16), 0, dp(4))
    }

    private fun box(size: Float, heightDp: Int): TextView = TextView(this).apply {
        text = ""
        textSize = size
        setPadding(dp(12), dp(12), dp(12), dp(12))
        minHeight = dp(heightDp)
        setBackgroundResource(android.R.drawable.edit_text)
        movementMethod = ScrollingMovementMethod()
    }

    private fun buildUi() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(20), dp(20), dp(20))
        }

        root.addView(TextView(this).apply {
            text = "Swedish Speak Coach"
            textSize = 28f
        })
        root.addView(TextView(this).apply {
            text = "Speak Swedish; use an English word when you do not know the Swedish one."
            textSize = 14f
        })

        status = TextView(this).apply {
            text = "Preparing…"
            textSize = 16f
            setPadding(0, dp(10), 0, dp(8))
        }
        root.addView(status)

        recognizerInfo = TextView(this).apply {
            text = "Recognizer: checking…"
            textSize = 12f
        }
        root.addView(recognizerInfo)

        grammarInfo = TextView(this).apply {
            text = "Grammar: Qwen3 0.6B local corrector"
            textSize = 12f
        }
        root.addView(grammarInfo)

        progress = ProgressBar(this).apply {
            isIndeterminate = true
            visibility = View.GONE
        }
        root.addView(
            progress,
            LinearLayout.LayoutParams(dp(36), dp(36)).apply {
                gravity = Gravity.CENTER_HORIZONTAL
            }
        )

        root.addView(label("Live — what I hear now"))
        live = box(19f, 65)
        root.addView(live)

        root.addView(label("What you said"))
        original = box(18f, 90)
        root.addView(original)

        root.addView(label("English word help"))
        words = box(18f, 80)
        root.addView(words)

        root.addView(label("Corrected Swedish"))
        corrected = box(21f, 110)
        root.addView(corrected)

        start = Button(this).apply {
            text = "Start speaking"
            isEnabled = false
        }
        root.addView(
            start,
            LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                topMargin = dp(18)
            }
        )

        root.addView(Button(this).apply {
            id = CLEAR_BUTTON_ID
            text = "Clear"
        })

        root.addView(TextView(this).apply {
            text = "Grammar is corrected by a local Qwen3 0.6B model. English words stay in the original sentence so the model can translate them using context. Only output-format checks are applied; there is no rule-based grammar patcher."
            textSize = 12f
            setPadding(0, dp(12), 0, dp(8))
        })

        setContentView(ScrollView(this).apply { addView(root) })
    }

    override fun onDestroy() {
        partialJob?.cancel()
        finalChannel.close()
        if (::speech.isInitialized) speech.destroy()
        coach.close()
        super.onDestroy()
    }

    private companion object {
        const val CLEAR_BUTTON_ID = 1002
    }
}
