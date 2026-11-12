"""Generate licensed, offline neural narration from the bilingual source script.

Run with a Python environment containing kokoro-onnx, misaki-fork[zh,en], soundfile,
onnxruntime, espeakng-loader and phonemizer. Pass --models pointing at the
official Kokoro ONNX release files. This script does not use system voices,
credentials, cloned voices, or modify model files. Outputs review assets only.
"""
from __future__ import annotations

import argparse
import hashlib
import json
import os
from pathlib import Path
import re

os.environ.setdefault("OMP_NUM_THREADS", "4")
import espeakng_loader
import numpy as np
import onnxruntime as ort
import soundfile as sf
from kokoro_onnx import Kokoro
from kokoro_onnx.config import EspeakConfig
from misaki import en, espeak, zh
from phonemizer.backend.espeak.wrapper import EspeakWrapper

ROOT = Path(__file__).resolve().parents[1]
SR = 24000
CONFIG = {
    "zh": {"model": "kokoro-v1.1-zh.onnx", "voices": "voices-v1.1-zh.bin", "voice": "zf_001", "speed": 0.92},
    "en": {"model": "kokoro-v1.0.onnx", "voices": "voices-v1.0.bin", "voice": "af_heart", "speed": 0.90},
}


def sha(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def source_rows():
    rows = []
    for line in (ROOT / "handoff/content/VOICE_SCRIPT.md").read_text().splitlines():
        cells = [v.strip() for v in line.strip().strip("|").split("|")]
        if line.startswith("|") and len(cells) == 3 and cells[0] not in ("id", "---"):
            rows.append(cells)
    if len(rows) != 15 or len({r[0] for r in rows}) != 15:
        raise ValueError("Expected 15 unique bilingual source lines")
    return rows


def prepare_audio(samples):
    a = np.asarray(samples, dtype=np.float32).reshape(-1)
    if not np.isfinite(a).all():
        raise ValueError("Nonfinite neural output")
    active = np.flatnonzero(np.abs(a) > 0.0003)
    if not len(active):
        raise ValueError("Silent neural output")
    # Keep breath and consonant margins; remove only excessive model padding.
    a = a[max(0, active[0] - int(SR * .08)):min(len(a), active[-1] + int(SR * .14))]
    fade = min(int(SR * .005), len(a) // 3)
    a[:fade] *= np.linspace(0, 1, fade)
    a[-fade:] *= np.linspace(1, 0, fade)
    return a


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--models", type=Path, required=True)
    args = parser.parse_args()
    output = ROOT / "review/audio/voice-natural"
    output.mkdir(parents=True, exist_ok=True)
    EspeakWrapper.set_library(espeakng_loader.get_library_path())
    os.environ["ESPEAK_DATA_PATH"] = espeakng_loader.get_data_path()
    rows = source_rows()
    assets = []
    sources = {}
    for lang, cfg in CONFIG.items():
        opts = ort.SessionOptions()
        opts.intra_op_num_threads = 4
        opts.inter_op_num_threads = 1
        model_path = args.models / cfg["model"]
        voices_path = args.models / cfg["voices"]
        sources[lang] = {"model": cfg["model"], "modelSha256": sha(model_path), "voices": cfg["voices"], "voicesSha256": sha(voices_path)}
        session = ort.InferenceSession(str(model_path), sess_options=opts, providers=["CPUExecutionProvider"])
        engine = Kokoro.from_session(session, str(voices_path), espeak_config=EspeakConfig(lib_path=espeakng_loader.get_library_path(), data_path=espeakng_loader.get_data_path()), vocab_config=str(args.models / "config-zh.json") if lang == "zh" else None)
        g2p = zh.ZHG2P(version="1.1") if lang == "zh" else en.G2P(trf=False, british=False, fallback=espeak.EspeakFallback(british=False))
        for ident, chinese, english in rows:
            text = chinese if lang == "zh" else english
            # Retain exact spoken wording. Sentence boundaries get breathing space.
            fragments = [s.strip() for s in re.findall(r"[^。.!?！？]+[。.!?！？]?", text) if s.strip()]
            pieces = []
            for fragment in fragments:
                ph, _ = g2p(fragment)
                if not ph or "❓" in ph:
                    raise ValueError(f"Unresolved phonemes: {lang}/{ident}: {ph}")
                a, sr = engine.create(ph, voice=cfg["voice"], speed=cfg["speed"], is_phonemes=True)
                if sr != SR:
                    raise ValueError(f"Unexpected sample rate {sr}")
                if pieces:
                    pieces.append(np.zeros(int(SR * .22), dtype=np.float32))
                pieces.append(prepare_audio(a))
            a = np.concatenate(pieces)
            rms = float(np.sqrt(np.mean(a * a)))
            gain = min(10 ** (-21 / 20) / rms, 10 ** (-3.5 / 20) / float(np.max(np.abs(a))))
            a = np.pad(a * gain, (int(SR * .08), int(SR * .18)))
            target = output / f"voice_{lang}_{ident}.wav"
            sf.write(target, a, SR, subtype="PCM_16")
            check, sr = sf.read(target, dtype="float32")
            peak = float(np.max(np.abs(check)))
            assert check.ndim == 1 and sr == SR and peak < .9 and np.isfinite(check).all()
            assets.append({"id": ident, "language": lang, "text": text, "voice": cfg["voice"], "speed": cfg["speed"], "model": cfg["model"], "path": str(target.relative_to(ROOT)), "sampleRate": sr, "channels": 1, "format": "WAV PCM_16", "durationSeconds": round(len(check) / sr, 4), "peakDbfs": round(20 * np.log10(peak), 3), "rmsDbfs": round(float(20 * np.log10(np.sqrt(np.mean(check * check)))), 3), "clippedSamples": int(np.count_nonzero(np.abs(check) >= .999)), "sha256": sha(target)})
            print(lang, ident, assets[-1]["durationSeconds"], flush=True)
    manifest = {"status": "GENERATED_AND_WAVEFORM_VALIDATED; subjective listening pending", "engine": "Kokoro ONNX 0.6.1, CPU", "license": "Apache-2.0 models; see official model cards", "voiceCloning": False, "script": "handoff/content/VOICE_SCRIPT.md", "scriptSha256": sha(ROOT / "handoff/content/VOICE_SCRIPT.md"), "processing": {"sentenceGapSeconds": .22, "leadingPadSeconds": .08, "trailingPadSeconds": .18, "speed": {k: v["speed"] for k, v in CONFIG.items()}, "targetSpeechRmsDbfs": -21, "peakCeilingDbfs": -3.5, "reverb": False}, "sources": sources, "assets": assets}
    (ROOT / "review/audio/natural-voice-manifest.json").write_text(json.dumps(manifest, ensure_ascii=False, indent=2) + "\n")
    print("Validated", len(assets), "neural narration files")


if __name__ == "__main__":
    main()
