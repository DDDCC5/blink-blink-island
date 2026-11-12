# Language, narration and butterfly attitude polish - 2026-11-12

Wearer confirmed panel/island placement now correct. Preserve ScenePlacementUseCase, all accepted offsets, target path, gaze thresholds and retry logic.

Implemented:
- IslandContent hides the language switch on Page.TRAINING, including calibration/preparation/pause/retry. Pre-training pages retain language selection.
- Replaced all30 bilingual narration assets with licensed local neural female voices: Chinese zf_001 (Kokoro v1.1-zh) and English af_heart (Kokoro v1.0). Original15 bilingual script lines retained. PCM16/24kHz/mono, sentence gaps, normalized volume and0 clipped samples. APK verification checks all30 hashes and ZIP stored mode required by asset openFd. Runtime voice_manifest.json and model/tool licenses included. Full production/validation report: NATURAL_VOICE_REBUILD.md. No runtime network/model service or global setup. Naturalness still requires listening.
- ButterflyFlightUseCase uses actual pursuit-path tangent to orient local -Z head, gentle66-74degree model pitch, smooth bank bounded18degrees, and bilateral opposite-wing4.2Hz flapping. Original fixed Rx90 body caused the upright sliding appearance. Local GLB axes are grounded in scripts/generate_review_3d.py. Same motionMs drives body/wing pose so tracking pause/preparation/stop freeze animation. Existing path/target center/scoring and persistent scenery preserved.

Verification:
- natural-flight-red.log records missing behavior before implementation. voice-flight-final-build-tests.log:81 JVM tests and assembleDebug passed,0 failures/errors. Geometry test compares projected head with actual path tangent across a full cycle, quaternion norm, pitch/bank, wing range/opposition and deterministic pause clock.
- voice-flight-style.log PASS0 errors/warnings. Review inline JavaScript syntax checked after switching audio selector to new files.
- Read-only review found no model-axis/pause/input regression.4.2Hz comfort and naturalness require wearer acceptance.
- voice-flight-install.log Success; voice-flight-launch.log started PID4236. Voice/audio and animation visual acceptance pending.
- Managed scene workflow natural-flight-scene-*.json submitted source and built but final delivery validator blocked, as before; not a clean managed acceptance receipt. Independent Gradle and device evidence above do not overwrite that result.

Source: domain/usecase/ButterflyFlightUseCase.kt, ui/island/components/IslandScene.kt and IslandContent.kt, scripts/generate_natural_voice.py. Review samples: review/audio/voice-natural/voice_zh_butterfly.wav and voice_en_butterfly.wav. Source/archive updated.
