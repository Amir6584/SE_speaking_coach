package se.example.swedishcoach

/**
 * Output-format guard only. This class deliberately contains NO Swedish grammar
 * rules. Grammar, word choice, and contextual English -> Swedish translation are
 * the language model's job.
 */
object ModelOutputGuard {
    private val thinkBlock = Regex("(?is)<think>.*?</think>")
    private val knownLabels = Regex(
        "(?i)^(?:corrected(?: swedish)?(?: sentence)?|korrigerad(?: svensk)? mening|rätt|svar)\\s*:\\s*"
    )
    private val metaStarts = listOf(
        "here is", "the corrected", "corrected sentence", "explanation",
        "förklaring", "jag har korrigerat", "den korrekta meningen", "rättelse"
    )

    fun extract(raw: String, original: String): String? {
        var text = raw
            .replace(thinkBlock, "")
            .replace("<|assistant|>", "")
            .replace("<|endoftext|>", "")
            .replace("<|im_end|>", "")
            .replace("<|end|>", "")
            .trim()

        // Qwen may occasionally wrap the one-line answer in a known label.
        text = text.replace(knownLabels, "").trim()
        text = text.trim('"', '\'', '`', ' ')

        val lines = text.lineSequence().map { it.trim() }.filter { it.isNotEmpty() }.toList()
        if (lines.size != 1) return null

        val candidate = lines.single()
        val lower = candidate.lowercase()
        if (metaStarts.any { lower.startsWith(it) }) return null
        if (candidate.startsWith("-") || candidate.startsWith("*") || candidate.startsWith("#")) return null
        if (candidate.length > original.length * 3 + 80) return null

        // Grammar correction must not silently alter numeric facts.
        val inputNumbers = Regex("\\d+(?:[.,]\\d+)?").findAll(original).map { it.value }.toList()
        val outputNumbers = Regex("\\d+(?:[.,]\\d+)?").findAll(candidate).map { it.value }.toList()
        if (inputNumbers != outputNumbers) return null

        return tidy(candidate)
    }

    private fun tidy(text: String): String {
        val clean = text.replace(Regex("\\s+"), " ").trim()
        if (clean.isBlank()) return clean
        val capitalized = clean.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase() else it.toString()
        }
        return if (capitalized.last() in listOf('.', '!', '?')) capitalized else "$capitalized."
    }
}
