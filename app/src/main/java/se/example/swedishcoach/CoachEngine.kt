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

    private val guard = setOf(
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
        onStatus("Preparing English → Swedish vocabulary model…")
        runCatching { Tasks.await(enToSv.downloadModelIfNeeded()) }
        onStatus(grammar.description)
    }

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
            if (lower.length < 3 || lower in guard || !seen.add(lower)) continue

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

    suspend fun correctSentence(
        original: String,
        suggestions: List<WordSuggestion>,
    ): Correction = withContext(Dispatchers.IO) {
        var seeded = original
        for (suggestion in suggestions) {
            seeded = Regex(
                "(?i)(?<![A-Za-zÅÄÖåäö])${Regex.escape(suggestion.original)}(?![A-Za-zÅÄÖåäö])"
            ).replace(seeded, suggestion.swedish)
        }

        val result = grammar.correct(seeded)
        val flags = buildList {
            if (result.secondPass) add("2-pass")
            if (result.modelEditUsed) add("model+validator") else add("strict validator")
        }

        Correction(
            naturalSwedish = result.text,
            engine = "Swedish CPT 135M • ${"%.1f".format(result.tokensPerSecond)} tok/s • ${flags.joinToString("+")}"
        )
    }

    override fun close() {
        grammar.close()
        enToSv.close()
        languageId.close()
    }
}
