package se.example.swedishcoach

import android.content.Context
import dev.ffmpegkit.llama.Llama
import dev.ffmpegkit.llama.LlamaConfig
import dev.ffmpegkit.llama.LlamaModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Pure model-based Swedish correction.
 *
 * There is intentionally no grammar-rule validator here. Qwen3 receives the
 * original mixed Swedish/English sentence and returns one corrected Swedish
 * sentence. ModelOutputGuard only enforces output format and factual numbers.
 */
class SwedishGrammarLlm(context: Context) : AutoCloseable {
    private val modelManager = BundledModelManager(context)
    private var model: LlamaModel? = null

    val description: String
        get() = "Grammar: ${BundledModelManager.MODEL_LABEL} • on-device"

    suspend fun prepare(onStatus: (String) -> Unit = {}) = withContext(Dispatchers.IO) {
        if (model != null) return@withContext

        val file = modelManager.ensureModel(onStatus)
        onStatus("Loading ${BundledModelManager.MODEL_LABEL}…")

        model = try {
            Llama.loadModel(
                modelPath = file.absolutePath,
                config = LlamaConfig(
                    contextSize = 2048,
                    threads = 4,
                    // Qwen3 recommends sampling rather than greedy decoding.
                    temperature = 0.7f,
                    topP = 0.8f,
                    topK = 20,
                    seed = 42,
                ),
            )
        } catch (t: Throwable) {
            error("Qwen3 grammar model could not be loaded: ${t.message ?: t.javaClass.simpleName}")
        }

        onStatus(description)
    }

    suspend fun correct(sentence: String): Result = withContext(Dispatchers.IO) {
        val handle = model ?: error("Grammar model is not loaded")
        val input = sentence.replace(Regex("\\s+"), " ").trim()
        if (input.isBlank()) return@withContext Result("", 0f, false)

        val first = Llama.complete(
            handle,
            prompt = userPrompt(input, retry = false),
            systemPrompt = SYSTEM_PROMPT,
            maxTokens = 72,
        )
        val firstSentence = ModelOutputGuard.extract(first.text, input)
        if (firstSentence != null) {
            return@withContext Result(firstSentence, first.tokensPerSecond, false)
        }

        // Retry only for output-format failure. This is not a grammar-rule pass.
        val second = Llama.complete(
            handle,
            prompt = userPrompt(input, retry = true),
            systemPrompt = STRICT_SYSTEM_PROMPT,
            maxTokens = 72,
        )
        val secondSentence = ModelOutputGuard.extract(second.text, input)

        Result(
            text = secondSentence ?: tidy(input),
            tokensPerSecond = second.tokensPerSecond,
            retriedForFormat = true,
        )
    }

    data class Result(
        val text: String,
        val tokensPerSecond: Float,
        val retriedForFormat: Boolean,
    )

    private fun userPrompt(sentence: String, retry: Boolean): String {
        val instruction = if (retry) {
            "Return exactly one corrected Swedish sentence and nothing else."
        } else {
            "Correct this sentence into natural Swedish:"
        }
        return "$instruction\n$sentence\n/no_think"
    }

    private fun tidy(text: String): String {
        val clean = text.replace(Regex("\\s+"), " ").trim()
        if (clean.isBlank()) return clean
        val capitalized = clean.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase() else it.toString()
        }
        return if (capitalized.last() in listOf('.', '!', '?')) capitalized else "$capitalized."
    }

    override fun close() {
        model?.let { runCatching { Llama.releaseModel(it) } }
        model = null
    }

    private companion object {
        val SYSTEM_PROMPT = """
You correct short spoken learner sentences into natural, grammatically correct Swedish.
The input can mix Swedish and English. Translate English words or phrases into the contextually correct Swedish expression.
Silently check grammar, word choice, noun form and definiteness, adjective agreement, verb form and tense, word order, pronouns, prepositions, and meaning.
Preserve the speaker's intended meaning and factual information. Correct semantic category mistakes when context makes the intent clear, for example a language name used where a country is intended.
If the sentence is already natural Swedish, return it unchanged except for capitalization or punctuation.
Output ONLY the final corrected Swedish sentence. Do not explain, label, quote, list alternatives, or discuss the correction.
""".trimIndent()

        val STRICT_SYSTEM_PROMPT = """
You are a Swedish sentence correction engine.
Return exactly ONE natural Swedish sentence.
Translate any English words according to context and fix all Swedish grammar and word-choice errors while preserving intended meaning.
No explanation. No label. No quotation marks. No alternatives. No analysis. No markdown.
""".trimIndent()
    }
}
