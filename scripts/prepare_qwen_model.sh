#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

RAW_DIR="$ROOT/app/src/main/res/raw"
MODEL_DIR="$ROOT/qwen-model"
MODEL="$MODEL_DIR/Qwen3-0.6B-Q4_0.gguf"
EXPECTED_SHA256="da2572f16c06133561ce56accaa822216f2391ef4d37fba427801cd6736417d4"
PARTS=16
MIN_BYTES=400000000

mkdir -p "$RAW_DIR" "$MODEL_DIR"

ready=1
for i in $(seq -w 0 15); do
  [ -s "$RAW_DIR/qwen_grammar_part_0${i}.bin" ] || ready=0
done
if [ "$ready" -eq 1 ]; then
  count=$(find "$RAW_DIR" -maxdepth 1 -type f -name 'qwen_grammar_part_*.bin' | wc -l)
  total=$(find "$RAW_DIR" -maxdepth 1 -type f -name 'qwen_grammar_part_*.bin' -printf '%s\n' | awk '{s+=$1} END {print s+0}')
  if [ "$count" -eq "$PARTS" ] && [ "$total" -gt "$MIN_BYTES" ]; then
    combined_sha=$(cat "$RAW_DIR"/qwen_grammar_part_*.bin | sha256sum | awk '{print $1}')
    if [ "$combined_sha" = "$EXPECTED_SHA256" ]; then
      echo "Qwen3 model chunks already prepared and SHA-256 verified: $count parts / $total bytes"
      exit 0
    fi
    echo "Existing Qwen3 chunks failed SHA-256; rebuilding them"
  fi
fi

rm -f "$RAW_DIR"/qwen_grammar_part_*.bin
# Remove obsolete v29 generated model resources if present in a reused checkout.
rm -f "$RAW_DIR"/swedish_model_part_*.bin

python -m pip install --disable-pip-version-check -q huggingface_hub
python - <<'PY'
from huggingface_hub import hf_hub_download
path = hf_hub_download(
    repo_id="ggml-org/Qwen3-0.6B-GGUF",
    filename="Qwen3-0.6B-Q4_0.gguf",
    local_dir="qwen-model",
)
print(f"Downloaded Qwen3 GGUF: {path}")
PY

test -s "$MODEL"
size=$(stat -c%s "$MODEL")
echo "Qwen3 GGUF bytes: $size"
test "$size" -gt "$MIN_BYTES"
echo "$EXPECTED_SHA256  $MODEL" | sha256sum -c -

python - <<'PY'
from pathlib import Path

model = Path("qwen-model/Qwen3-0.6B-Q4_0.gguf")
raw = Path("app/src/main/res/raw")
parts = 16
size = model.stat().st_size
chunk = (size + parts - 1) // parts

with model.open("rb") as src:
    for i in range(parts):
        out = raw / f"qwen_grammar_part_{i:03d}.bin"
        remaining = max(0, min(chunk, size - i * chunk))
        if remaining <= 0:
            raise RuntimeError(f"Model unexpectedly produced an empty chunk at index {i}")
        with out.open("wb") as dst:
            while remaining:
                data = src.read(min(1024 * 1024, remaining))
                if not data:
                    raise RuntimeError("Unexpected EOF while splitting Qwen3 GGUF")
                dst.write(data)
                remaining -= len(data)

print(f"Created exactly {parts} Qwen3 model chunks")
PY

count=$(find "$RAW_DIR" -maxdepth 1 -type f -name 'qwen_grammar_part_*.bin' | wc -l)
total=$(find "$RAW_DIR" -maxdepth 1 -type f -name 'qwen_grammar_part_*.bin' -printf '%s\n' | awk '{s+=$1} END {print s+0}')

echo "Qwen3 chunk count: $count"
echo "Qwen3 chunk bytes: $total"
test "$count" -eq "$PARTS"
test "$total" -eq "$size"
