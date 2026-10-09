# Important: remove old Android workflows

The failure that checked `swedish_model_part_000` through `007` came from an old
pre-Qwen workflow. v31 uses 16 resources named `qwen_grammar_part_000` through
`qwen_grammar_part_015`.

In GitHub, the repository should contain this canonical workflow:

`.github/workflows/android.yml`

Delete older workflow files under `.github/workflows/` if they contain any of:

- `swedish_model_part_`
- `COUNT ... -eq 8`
- `120000000`

Do not use GitHub's "Re-run jobs" button on an old v29/v30 run; that reruns the
workflow from the old commit. Push a new commit containing v31 and open the run
named `Build Android APK v31 - Qwen3`.

The correct artifact is `SwedishSpeakCoach-v31-debug-apk`.
