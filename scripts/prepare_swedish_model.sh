#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"
RAW_DIR="$ROOT/app/src/main/res/raw"
mkdir -p "$RAW_DIR"
READY=1
for i in 000 001 002 003 004 005 006 007; do [ -s "$RAW_DIR/swedish_model_part_${i}.bin" ] || READY=0; done
if [ "$READY" -eq 1 ]; then TOTAL=$(find "$RAW_DIR" -maxdepth 1 -type f -name 'swedish_model_part_*.bin' -printf '%s\n' | awk '{s+=$1} END {print s+0}'); if [ "$TOTAL" -gt 120000000 ]; then echo "Swedish model chunks already prepared: $TOTAL bytes"; exit 0; fi; fi
rm -f "$RAW_DIR"/swedish_model_part_*.bin
python -m pip install --disable-pip-version-check -q huggingface_hub
python - <<'PY'
from huggingface_hub import snapshot_download
snapshot_download(repo_id="jekunz/smollm-135m-cpt-fineweb-swedish", local_dir="grammar-hf")
PY
if [ ! -d llama.cpp/.git ]; then git clone -q https://github.com/ggml-org/llama.cpp.git llama.cpp; fi
cd llama.cpp; git fetch -q --all; git checkout -q 2da6686; cd "$ROOT"
python -m pip install --disable-pip-version-check -q -r llama.cpp/requirements.txt
python llama.cpp/convert_hf_to_gguf.py grammar-hf --outfile grammar-f16.gguf --outtype f16
cmake -S llama.cpp -B llama.cpp/build -DLLAMA_CURL=OFF -DGGML_NATIVE=OFF >/dev/null
cmake --build llama.cpp/build --config Release -j 2 --target llama-quantize >/dev/null
llama.cpp/build/bin/llama-quantize grammar-f16.gguf swedish-smollm2-135m-cpt-q8_0.gguf Q8_0
MODEL=swedish-smollm2-135m-cpt-q8_0.gguf
SIZE=$(stat -c%s "$MODEL"); test "$SIZE" -gt 120000000
python - <<'PY'
from pathlib import Path
model=Path('swedish-smollm2-135m-cpt-q8_0.gguf'); raw=Path('app/src/main/res/raw'); size=model.stat().st_size; parts=8; chunk=(size+parts-1)//parts
with model.open('rb') as src:
    for i in range(parts):
        out=raw/f'swedish_model_part_{i:03d}.bin'; remaining=max(0,min(chunk,size-i*chunk))
        with out.open('wb') as dst:
            while remaining:
                b=src.read(min(1024*1024,remaining));
                if not b: raise RuntimeError('unexpected EOF')
                dst.write(b); remaining-=len(b)
PY
COUNT=$(find "$RAW_DIR" -maxdepth 1 -type f -name 'swedish_model_part_*.bin' | wc -l); TOTAL=$(find "$RAW_DIR" -maxdepth 1 -type f -name 'swedish_model_part_*.bin' -printf '%s\n' | awk '{s+=$1} END {print s+0}'); test "$COUNT" -eq 8; test "$TOTAL" -eq "$SIZE"
