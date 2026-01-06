#!/data/data/com.termux/files/usr/bin/bash
set -euo pipefail

echo "== [1/4] Write missing models (Kind/RectN/MediaInfo etc) =="

mkdir -p app/src/main/java/com/pasiflonet/mobile/util

cat > app/src/main/java/com/pasiflonet/mobile/util/EditModels.kt <<'KOT'
package com.pasiflonet.mobile.util

import android.net.Uri

/** Normalized rect 0..1 (relative to video width/height). */
data class RectN(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
) {
    fun clamp(): RectN = RectN(
        left.coerceIn(0f, 1f),
        top.coerceIn(0f, 1f),
        right.coerceIn(0f, 1f),
        bottom.coerceIn(0f, 1f),
    )
}

/** Back-compat name used around the project. */
typealias BlurRectN = RectN

enum class Kind { VIDEO, PHOTO, AUDIO, DOCUMENT, UNKNOWN }

data class WatermarkConfig(
    val assetPath: String = "watermark.png", // assets/
    val alpha: Float = 0.85f,
    val scale: Float = 0.22f, // relative to min(width,height)
    val marginN: Float = 0.03f,
    val position: Position = Position.BOTTOM_RIGHT
) {
    enum class Position { TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT }
}

data class MediaInfo(
    val kind: Kind,
    val srcUri: Uri? = null,
    val mime: String? = null,
    val thumbB64: String? = null
)
KOT

echo "== [2/4] Patch SendWorker.kt (imports + TAG + remove leftover session/ok + applicationContext scope) =="

python - <<'PY'
from pathlib import Path
import re

p = Path("app/src/main/java/com/pasiflonet/mobile/worker/SendWorker.kt")
s = p.read_text(encoding="utf-8")

# Ensure needed imports exist
need_imports = [
    "import android.util.Log",
    "import com.pasiflonet.mobile.util.Kind",
    "import com.pasiflonet.mobile.util.MediaInfo",
    "import com.pasiflonet.mobile.util.RectN",
    "import com.pasiflonet.mobile.util.BlurRectN",
    "import com.pasiflonet.mobile.util.WatermarkConfig",
    "import com.pasiflonet.mobile.util.VideoEditPipeline",
]

# insert imports after package line + existing imports
lines = s.splitlines(True)
out=[]
inserted=False
for i,ln in enumerate(lines):
    out.append(ln)
    if not inserted and ln.startswith("package "):
        # after package line, keep going; we'll inject after the first blank line or first import block start
        pass

# If file has import section, inject after last import; else after package
text = "".join(out)
if "import " in text:
    parts = text.splitlines(True)
    out=[]
    last_import_idx=-1
    for i,ln in enumerate(parts):
        if ln.lstrip().startswith("import "):
            last_import_idx=i
        out.append(ln)
    if last_import_idx >= 0:
        # check existing imports
        existing = set([ln.strip() for ln in out if ln.strip().startswith("import ")])
        inject = [imp+"\n" for imp in need_imports if imp not in existing]
        out = out[:last_import_idx+1] + inject + out[last_import_idx+1:]
        text = "".join(out)
else:
    # no imports -> add block after package
    text = re.sub(r'^(package [^\n]+\n)',
                  r'\1\n' + "\n".join(need_imports) + "\n\n",
                  text, count=1, flags=re.M)

# Ensure TAG exists (top-level const)
if re.search(r'private const val TAG\s*=', text) is None:
    # put after imports block
    m = re.search(r'(\n)(?=class\s+SendWorker\b|@)', text)
    if m:
        text = text[:m.start()] + '\nprivate const val TAG = "SendWorker"\n' + text[m.start():]
    else:
        text = 'private const val TAG = "SendWorker"\n' + text

# Fix applicationContext references to be valid inside nested lambdas
# (only if file has class SendWorker)
if "class SendWorker" in text:
    text = re.sub(r'\bapplicationContext\b', 'this@SendWorker.applicationContext', text)

# Remove leftover FFmpegKit-era vars that now cause "session/ok" unresolved
# Remove lines that reference "session" or "ok" in the FFmpeg sense
text = re.sub(r'^\s*val\s+session\b.*\n', '', text, flags=re.M)
text = re.sub(r'^\s*val\s+ok\b.*\n', '', text, flags=re.M)
text = re.sub(r'^.*\bsession\b.*\n', lambda m: '' if ('td' not in m.group(0).lower()) else m.group(0), text, flags=re.M)
text = re.sub(r'^.*\bok\b.*\n', '', text, flags=re.M)

# If RectN/Kind/MediaInfo were referenced via wrong package, imports handle it.

p.write_text(text, encoding="utf-8")
print("OK: SendWorker patched.")
PY

echo "== [3/4] Quick verify the symbols that failed =="
rg -n "Unresolved reference: (Kind|RectN|MediaInfo|TAG|applicationContext|session|ok)" -S app/src/main/java/com/pasiflonet/mobile/worker/SendWorker.kt || true
echo "-- Check that SendWorker now references util models:"
rg -n "Kind|RectN|MediaInfo|TAG" -S app/src/main/java/com/pasiflonet/mobile/worker/SendWorker.kt | head || true

echo "== [4/4] Commit + Push =="
git add -A
git commit -m "Fix: SendWorker missing models + TAG + clean leftover FFmpeg vars" || true
git push
echo "DONE ✅"
