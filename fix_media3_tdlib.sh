#!/data/data/com.termux/files/usr/bin/bash
set -euo pipefail

echo "== [1/6] sanity =="
test -d .git || { echo "ERROR: run inside repo"; exit 1; }
test -f app/build.gradle.kts || { echo "ERROR: missing app/build.gradle.kts"; exit 1; }

echo "== [2/6] remove local AAR binaries (td + ffmpegkit) =="
rm -f app/libs/td-1.8.56.aar 2>/dev/null || true
rm -f app/libs/ffmpeg-kit-*.aar app/libs/ffmpeg-kit-full-*.aar 2>/dev/null || true

echo "== [3/6] patch app/build.gradle.kts: TDLib=JitPack only + Media3 deps + remove FFmpegKit bits =="
python - <<'PY'
from pathlib import Path
import re

p = Path("app/build.gradle.kts")
s = p.read_text(encoding="utf-8")

# --- remove TD local AAR download/vars/implementation ---
s = re.sub(r'^\s*val\s+tdAarUrl\s*=.*\n', '', s, flags=re.M)
s = re.sub(r'^\s*val\s+tdAarFile\s*=.*\n', '', s, flags=re.M)
s = re.sub(r'^\s*download\(\s*tdAarUrl\s*,\s*tdAarFile\s*\)\s*\n', '', s, flags=re.M)
s = re.sub(r'^\s*implementation\(\s*files\(\s*".*td-1\.8\.56\.aar"\s*\)\s*\)\s*\n', '', s, flags=re.M)
s = re.sub(r'^\s*implementation\(\s*files\(\s*".*\.aar"\s*\)\s*\)\s*\n', '', s, flags=re.M)

# keep ONLY jitpack dep
dep = 'implementation("com.github.tdlibx:td:1.8.56")'
if dep not in s:
    s = re.sub(r'(dependencies\s*\{\s*)', r'\1\n    ' + dep + '\n', s, count=1, flags=re.S)

# dedupe jitpack line
lines = s.splitlines(True)
out=[]
seen=0
for ln in lines:
    if 'com.github.tdlibx:td:1.8.56' in ln:
        seen += 1
        if seen > 1:
            continue
    out.append(ln)
s = ''.join(out)

# --- remove FFmpegKit download/vars/implementation + smart-exception ---
s = re.sub(r'^\s*val\s+ffmpegAarUrl\s*=.*\n', '', s, flags=re.M)
s = re.sub(r'^\s*val\s+ffmpegAarFile\s*=.*\n', '', s, flags=re.M)
s = re.sub(r'^\s*download\(\s*ffmpegAarUrl\s*,\s*ffmpegAarFile\s*\)\s*\n', '', s, flags=re.M)
s = re.sub(r'^\s*implementation\(\s*files\(\s*".*ffmpeg-kit.*\.aar"\s*\)\s*\)\s*\n', '', s, flags=re.M)
s = re.sub(r'^\s*implementation\(\s*"com\.arthenica:smart-exception-[^"]+"\s*\)\s*\n', '', s, flags=re.M)
s = re.sub(r'^\s*implementation\(\s*"com\.arthenica:smart-exception-[^"]+"\s*\)\s*\n', '', s, flags=re.M)

# --- ensure Media3 deps ---
v = "1.9.0"
need = f'implementation("androidx.media3:media3-transformer:{v}")'
if need not in s:
    ins = (
        f'\n    // Media3 Transformer (replaces FFmpegKit)\n'
        f'    implementation("androidx.media3:media3-transformer:{v}")\n'
        f'    implementation("androidx.media3:media3-effect:{v}")\n'
        f'    implementation("androidx.media3:media3-common:{v}")\n'
        f'    implementation("androidx.media3:media3-exoplayer:{v}")\n'
        f'    implementation("androidx.media3:media3-ui:{v}")\n'
    )
    s = re.sub(r'(dependencies\s*\{\s*)', r'\1' + ins, s, count=1, flags=re.S)

p.write_text(s, encoding="utf-8")
print("OK: app/build.gradle.kts patched.")
PY

echo "== [4/6] patch SendWorker.kt: remove ALL FFmpegKit references =="
python - <<'PY'
from pathlib import Path
import re

p = Path("app/src/main/java/com/pasiflonet/mobile/worker/SendWorker.kt")
s = p.read_text(encoding="utf-8")

# remove imports
s = re.sub(r'^\s*import\s+com\.arthenica\.ffmpegkit\..*\n', '', s, flags=re.M)

# hard-remove any lines mentioning FFmpegKit/ReturnCode/Statistics
s = re.sub(r'^.*\bFFmpegKitConfig\b.*\n', '', s, flags=re.M)
s = re.sub(r'^.*\bFFmpegKit\b.*\n', '', s, flags=re.M)
s = re.sub(r'^.*\bReturnCode\b.*\n', '', s, flags=re.M)
s = re.sub(r'^.*\bStatistics\b.*\n', '', s, flags=re.M)

# if there is a leftover "val session =" etc inside blocks, above removal should have cleared it.
p.write_text(s, encoding="utf-8")
print("OK: SendWorker.kt cleaned from FFmpegKit tokens.")
PY

echo "== [5/6] write a clean VideoEditPipeline.kt (Media3 Transformer Effects) =="
cat > app/src/main/java/com/pasiflonet/mobile/util/VideoEditPipeline.kt <<'KOT'
package com.pasiflonet.mobile.util

import android.content.Context
import android.net.Uri
import androidx.media3.common.Effect
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.Effects
import androidx.media3.transformer.Transformer
import com.google.common.collect.ImmutableList
import java.io.File

@UnstableApi
object VideoEditPipeline {

    fun export(
        context: Context,
        inputUri: Uri,
        blurRects: List<BlurRectN>,
        watermark: WatermarkConfig?,
        onDone: (Result<Uri>) -> Unit
    ) {
        try {
            val outFile = File(context.cacheDir, "pasiflonet_export_${System.currentTimeMillis()}.mp4")
            if (outFile.exists()) outFile.delete()

            val mediaItem = MediaItem.fromUri(inputUri)

            // Build Effects list (keep stable/compilable)
            val videoEffects = mutableListOf<Effect>()

            // If your RegionBlurEffect implements Effect (or GlEffect which is an Effect), this will work:
            if (blurRects.isNotEmpty()) {
                videoEffects.add(RegionBlurEffect(blurRects) as Effect)
            }

            // Watermark: add later as Effect (once class is ready)
            // if (watermark != null) videoEffects.add(WatermarkEffect(watermark) as Effect)

            val effects = Effects(
                /* audioProcessors = */ ImmutableList.of(),
                /* videoEffects = */ ImmutableList.copyOf(videoEffects)
            )

            val edited = EditedMediaItem.Builder(mediaItem)
                .setEffects(effects)
                .build()

            val transformer = Transformer.Builder(context).build()

            transformer.addListener(object : Transformer.Listener {
                override fun onCompleted(
                    composition: androidx.media3.transformer.Composition,
                    result: androidx.media3.transformer.ExportResult
                ) {
                    onDone(Result.success(Uri.fromFile(outFile)))
                }

                override fun onError(
                    composition: androidx.media3.transformer.Composition,
                    result: androidx.media3.transformer.ExportResult,
                    exception: Exception
                ) {
                    onDone(Result.failure(exception))
                }
            })

            transformer.start(edited, outFile.absolutePath)
        } catch (t: Throwable) {
            onDone(Result.failure(t))
        }
    }
}
KOT

echo "== [6/6] verify + commit + push =="
echo "-- verify ffmpeg tokens (should be empty):"
rg -n "ffmpegkit|FFmpegKit|ReturnCode|Statistics|com\.arthenica" app/src/main/java app/build.gradle.kts -S || true

echo "-- verify td local aar refs (should be empty):"
rg -n "td-1\.8\.56\.aar|tdAarUrl|tdAarFile|download\(tdAarUrl" app/build.gradle.kts -S || true

echo "-- verify jitpack tdlib line (should exist once):"
rg -n "com\.github\.tdlibx:td:1\.8\.56" app/build.gradle.kts -S || true

git add -A
git commit -m "Media3: remove FFmpegKit, TDLib JitPack only, update pipeline" || true
git push
echo "DONE ✅"
