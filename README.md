# Swedish Speak Coach v31 — Qwen3 model-only grammar correction

v31 replaces the accumulated Swedish grammar-rule validator with a stronger local language model.

## Architecture

Speech recognition remains unchanged: Android SpeechRecognizer uses Swedish as the primary language and requests Swedish/English language switching where Android supports it.

The correction path is now:

1. Preserve the original mixed Swedish/English transcript.
2. Send it directly to **Qwen3 0.6B Q4_0** on-device.
3. Ask the model to silently inspect grammar and context and output only one corrected Swedish sentence.
4. Apply a tiny output-format guard only. It does **not** contain Swedish grammar rules.
5. If the model violates the one-sentence output contract, retry once with a stricter format prompt.

ML Kit English -> Swedish translation remains only for fast word hints shown while speaking. Its literal word translations are no longer substituted into the sentence before grammar correction.

## Model

- Repository: `ggml-org/Qwen3-0.6B-GGUF`
- File: `Qwen3-0.6B-Q4_0.gguf`
- Quantization: Q4_0
- Approximate model size: 429 MB
- License: Apache-2.0
- SHA-256 checked by the build script: `da2572f16c06133561ce56accaa822216f2391ef4d37fba427801cd6736417d4`

The existing `dev.ffmpegkit-maintained:llama-android:0.1.1` wrapper is retained. Its documented llama.cpp build is new enough for Qwen3, so v31 avoids an unnecessary JNI/runtime migration.

## Qwen3 thinking mode

The prompt ends with `/no_think`. The model output guard also strips any `<think>...</think>` block if the runtime still emits an empty or unexpected thinking wrapper.

## Packaging

GitHub Actions downloads the official GGUF and splits it into exactly 16 `res/raw` chunks. `BundledModelParts.kt` contains direct compile-time `R.raw` references to every chunk, so a build cannot succeed if a part is missing.

The final APK is large because the ~429 MB model is bundled. On first launch, the chunks are reconstructed into app-private storage for llama.cpp.

## Build artifact

`SwedishSpeakCoach-v31-debug-apk`

## Manual acceptance tests after install

These are behavior tests for the model prompt, not hard-coded rules in the app:

- `jag har en barnen` should become `Jag har ett barn.`
- `jag kommer from japanska` should become `Jag kommer från Japan.`
- `jag kan pratar svenska` should become `Jag kan prata svenska.`
- `idag jag jobbar hemma` should become `Idag jobbar jag hemma.`
- already-correct Swedish should remain semantically unchanged.

If Qwen3 returns commentary instead of one sentence, the app rejects that output and retries once. It does not apply a hand-written Swedish grammar repair afterward.
