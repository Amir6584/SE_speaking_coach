package se.example.swedishcoach

/**
 * Fixed compile-time references to all bundled Qwen3 GGUF chunks.
 * If any chunk is missing, the Android build fails instead of shipping a broken APK.
 */
object BundledModelParts {
    val ids: IntArray = intArrayOf(
        R.raw.qwen_grammar_part_000,
        R.raw.qwen_grammar_part_001,
        R.raw.qwen_grammar_part_002,
        R.raw.qwen_grammar_part_003,
        R.raw.qwen_grammar_part_004,
        R.raw.qwen_grammar_part_005,
        R.raw.qwen_grammar_part_006,
        R.raw.qwen_grammar_part_007,
        R.raw.qwen_grammar_part_008,
        R.raw.qwen_grammar_part_009,
        R.raw.qwen_grammar_part_010,
        R.raw.qwen_grammar_part_011,
        R.raw.qwen_grammar_part_012,
        R.raw.qwen_grammar_part_013,
        R.raw.qwen_grammar_part_014,
        R.raw.qwen_grammar_part_015,
    )
}
