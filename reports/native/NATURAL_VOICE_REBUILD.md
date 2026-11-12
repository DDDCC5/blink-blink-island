# Natural neural narration rebuild

Generated 2026-11-12 for Blink Blink Island. This deliverable replaces the source narration set with offline neural speech; runtime copying and APK integration are performed by the enclosing app task.

## Voices and delivery

- Chinese: Kokoro v1.1-zh, stock female `zf_001`, synthesis speed 0.92.
- English: Kokoro v1.0, stock American female `af_heart`, synthesis speed 0.90.
- All 15 source lines in both languages preserve the wording in `handoff/content/VOICE_SCRIPT.md`.
- Sentences receive 220 ms of extra breathing space. Each file has 80 ms of leading and 180 ms of trailing padding. Speech RMS targets -21 dBFS with a -3.5 dBFS peak ceiling; no reverb, pitch manipulation, voice cloning, or macOS system TTS.
- Outputs: `review/audio/voice-natural/voice_<language>_<id>.wav`, WAV signed 16-bit PCM, 24,000 Hz, mono.
- Rebuild script: `scripts/generate_natural_voice.py`. Metadata, SHA-256 hashes and source-model hashes: `review/audio/natural-voice-manifest.json`.

## Sources and license

The [official Chinese model card](https://huggingface.co/hexgrad/Kokoro-82M-v1.1-zh) declares Apache-2.0 and describes professionally contributed Chinese training data. `zf_001` is the card's example female Chinese voice. The [official English model card](https://huggingface.co/hexgrad/Kokoro-82M) also declares Apache-2.0; the [official voice table](https://huggingface.co/hexgrad/Kokoro-82M/blob/main/VOICES.md) lists `af_heart` as female with an A overall grade. These facts support selecting stock neural voices; they do not establish the user's subjective preference.

The [official ONNX release](https://github.com/thewh1teagle/kokoro-onnx/releases/tag/model-files-v1.1) supplies the full precision model exports and voice banks. The [Chinese API example](https://github.com/thewh1teagle/kokoro-onnx/blob/main/examples/chinese.py) specifies v1.1 Chinese phonemization. License text is retained in `review/audio/voice-natural/licenses/` (model Apache-2.0, ONNX implementation MIT, installed wheel license).

## Reproduction

This run reused the existing model files and Python environment in `../Cards of Fate/production/phase-2` read-only, without modifying that workspace. No cloud credentials, model downloads, global configuration changes, or device tools were needed.

```sh
'/Users/chanzungsan/Documents/ChatGPT/Cards of Fate/production/phase-2/.venv/bin/python' scripts/generate_natural_voice.py --models '/Users/chanzungsan/Documents/ChatGPT/Cards of Fate/production/phase-2/tts-models'
```

A separate environment can use `kokoro-onnx==0.6.1`, `misaki-fork[zh,en]==0.9.6`, `onnxruntime==1.31.1`, `numpy==2.4.6`, `soundfile`, `espeakng-loader==0.2.4`, and `phonemizer`. Pass a directory containing the official `kokoro-v1.1-zh.onnx`, `kokoro-v1.0.onnx`, `voices-v1.1-zh.bin`, `voices-v1.0.bin`, plus the Chinese `config.json` saved as `config-zh.json`. Model exports include vocabulary metadata; the explicit Chinese config preserves compatibility with the documented example.

## Validation and listening boundary

The generator reloads each WAV and checks sample rate, channel count, finite values and headroom, then records duration, peak, RMS and clipped-sample count. A separate standard-library WAV/hash verification passed for all 30 files against the manifest: PCM16 mono 24 kHz, matching SHA-256 and duration, non-silent output, zero clipped samples. Total audio is 101.463 seconds; observed peaks range from -8.533 to -3.500 dBFS. These checks establish valid, audible, unclipped files; they cannot establish pronunciation, emotional warmth or subjective naturalness.

Listen first to `voice_zh_butterfly.wav` and `voice_en_butterfly.wav`. Then listen to the safety and countdown clips on the headset at the actual mix level. Short stock-model utterances can have prosody limitations, as the official voice documentation notes. In particular, count clips are approximately 1.05–1.15 seconds including padding; app playback scheduling must avoid cutting them off at a strict one-second boundary.
