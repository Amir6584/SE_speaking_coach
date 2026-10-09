package se.example.swedishcoach

import android.content.Context
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.nl.languageid.LanguageIdentificationOptions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CoachEngine(context: Context) : AutoCloseable {
    data class WordSuggestion(
        val original: String,
        val swedish: String,
    )

    data class Correction(
        val naturalSwedish: String,
        val engine: String,
    )

    private val enToSv = Translation.getClient(
        TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.ENGLISH)
            .setTargetLanguage(TranslateLanguage.SWEDISH)
            .build()
    )

    private val languageId = LanguageIdentification.getClient(
        LanguageIdentificationOptions.Builder()
            .setConfidenceThreshold(0.15f)
            .build()
    )

    private val grammar = SwedishGrammarLlm(context)

    val grammarEngineDescription: String
        get() = grammar.description

    private val commonSwedishWords = setOf(
        "jag", "du", "han", "hon", "vi", "ni", "de", "den", "det",
        "är", "var", "har", "hade", "vill", "kan", "ska", "skulle",
        "måste", "och", "eller", "men", "att", "som", "om", "för",
        "med", "till", "från", "på", "i", "av", "en", "ett", "inte",
        "också", "bara", "mycket", "bra", "vad", "vem", "varför", "hur",
        "när", "idag", "imorgon", "igår", "sedan", "hem", "hemma",
        "jobbar", "arbete", "mat", "kaffe"
    )

    suspend fun prepare(onStatus: (String) -> Unit = {}) = withContext(Dispatchers.IO) {
        grammar.prepare(onStatus)
        onStatus("Preparing English → Swedish word-help model…")
        runCatching { Tasks.await(enToSv.downloadModelIfNeeded()) }
        onStatus(grammar.description)
    }

    /**
     * Fast word-level hints remain available while speaking. They are display-only.
     * The grammar LLM always receives the original mixed-language sentence so it
     * can translate English words contextually rather than inheriting a literal
     * word-for-word replacement.
     */
    suspend fun findEnglishSuggestions(
        text: String,
        onSuggestion: suspend (WordSuggestion) -> Unit = {},
    ): List<WordSuggestion> = withContext(Dispatchers.IO) {
        val out = mutableListOf<WordSuggestion>()
        val seen = mutableSetOf<String>()
        val words = Regex("[A-Za-zÅÄÖåäö]+(?:['’-][A-Za-zÅÄÖåäö]+)?")
            .findAll(text)
            .map { it.value }

        for (word in words) {
            val lower = word.lowercase()
            if (lower.length < 3 || lower in commonSwedishWords || !seen.add(lower)) continue

            val languages = runCatching {
                Tasks.await(languageId.identifyPossibleLanguages(word))
            }.getOrDefault(emptyList())

            val enConfidence = languages
                .firstOrNull { it.languageTag.startsWith("en") }
                ?.confidence ?: 0f
            val svConfidence = languages
                .firstOrNull { it.languageTag.startsWith("sv") }
                ?.confidence ?: 0f

            if (enConfidence < 0.35f || enConfidence <= svConfidence + 0.05f) continue

            val translation = runCatching {
                Tasks.await(enToSv.translate(word))
            }.getOrNull()?.trim().orEmpty()

            if (translation.isBlank() || translation.equals(word, ignoreCase = true)) continue

            val suggestion = WordSuggestion(word, translation)
            out += suggestion
            onSuggestion(suggestion)
        }

        out
    }

    suspend fun correctSentence(original: String): Correction = withContext(Dispatchers.IO) {
        val result = grammar.correct(original)
        val retry = if (result.retriedForFormat) " • format retry" else ""

        Correction(
            naturalSwedish = result.text,
            engine = "Qwen3 0.6B Q4_0 • ${"%.1f".format(result.tokensPerSecond)} tok/s$retry"
        )
    }

    override fun close() {
        grammar.close()
        enToSv.close()
        languageId.close()
    }
}
