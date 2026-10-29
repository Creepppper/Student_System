#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
OUT="$ROOT/output"
cd "$ROOT"
python3 qa/build_srs.py
python3 qa/postprocess_docx.py "$OUT/srs-test.docx" "$OUT/srs-final.docx"
soffice -env:UserInstallation=file:///tmp/lo-profile --headless --convert-to pdf --outdir "$OUT" "$OUT/srs-final.docx"
cp "$OUT/srs-final.docx" "$OUT/学生管理系统需求规格说明书-v1.0-draft.docx"
cp "$OUT/srs-final.pdf" "$OUT/学生管理系统需求规格说明书-v1.0-draft.pdf"
