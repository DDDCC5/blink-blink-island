# Natural narration V2

Date: 2026-11-13. The previously accepted neural female voices are retained. This delivery regenerates all 30 app narration WAVs using the revised app copy in `content/VOICE_SCRIPT_V2.md`.

## Copy changes

Only two bilingual entries differ from the preserved `handoff/content/VOICE_SCRIPT.md`:

| ID | 中文 | English |
| --- | --- | --- |
| butterfly | 用目光慢慢跟随蝴蝶。放松身体，可以自然眨眼。 | Follow the butterfly gently with your eyes. Relax, and blink naturally. |
| tracking_paused | 先休息一下，调整好佩戴后，我们再继续。 | Take a moment. Adjust your headset, and we will continue. |

The butterfly narration has no instruction to keep the head stable. Pause guidance uses plain language. The other 13 bilingual entries remain unchanged, including the existing blink instruction; this narration change adds no force measurement or strength requirement. Head-pose logic and runtime asset integration belong to the enclosing app task.

## Voice and reproducibility

- Chinese: full-precision Kokoro v1.1-zh, `zf_001`, speed 0.92.
- English: full-precision Kokoro v1.0, `af_heart`, speed 0.90.
- The engine and processing match V1: Kokoro ONNX 0.6.1 on CPU, dry speech, 220 ms sentence gaps, 80 ms leading and 180 ms trailing padding, target speech RMS -21 dBFS and peak ceiling -3.5 dBFS.
- Output folder: `review/audio/voice-natural/`; manifest: `review/audio/natural-voice-manifest.json`.
- Generator accepts optional `--script` and defaults to `content/VOICE_SCRIPT_V2.md`. The manifest records the selected script and its actual SHA-256.

```sh
'/Users/chanzungsan/Documents/ChatGPT/Cards of Fate/production/phase-2/.venv/bin/python' scripts/generate_natural_voice.py --script content/VOICE_SCRIPT_V2.md --models '/Users/chanzungsan/Documents/ChatGPT/Cards of Fate/production/phase-2/tts-models'
```

The existing environment/model files are used read-only. All writes stay in this workspace. No native assets, UI files, original handoff files or external repositories were changed by this subtask.

## License and listening

Model licensing is unchanged: [Chinese Kokoro](https://huggingface.co/hexgrad/Kokoro-82M-v1.1-zh) and [English Kokoro](https://huggingface.co/hexgrad/Kokoro-82M) are Apache-2.0; the ONNX implementation is MIT. License copies remain in `review/audio/voice-natural/licenses/`. Stock voices are used, without cloning.

The voice identity was accepted in the preceding review. New butterfly and pause phrasing should still be heard in context; waveform checks cannot establish pronunciation or subjective delivery. Suggested files: `voice_zh_butterfly.wav`, `voice_en_butterfly.wav`, `voice_zh_tracking_paused.wav`, and `voice_en_tracking_paused.wav`. Countdown clips are slightly longer than one second including padding; playback scheduling should allow them to finish.

## Verification

PASS 30/30 files: exact V2 text/IDs, retained voice settings, PCM16 mono 24 kHz, SHA256 and durations verified, audible signal, zero clipping. Total 99.002s; peaks -8.547 to -3.500 dBFS. Original handoff hash unchanged.
