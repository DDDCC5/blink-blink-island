# Blink Blink Island / 眨眨岛 — Codex handoff

## What is ready

- Original editable SVG assets under assets/ (brand, scene, character states, tracking targets). SVGs are 2D design assets; the island concept board shown in Chatbox is visual reference only.
- Playable synthesized WAV reference audio under assets/audio/. See content/audio_manifest.json. No voice recordings are included; use content/VOICE_SCRIPT.md for licensed voice production.
- A bilingual browser interaction prototype under prototype/index.html. Open directly; it demonstrates screens and flow only. The Next button is manually operated; this is explicitly **not** real eye tracking, background notification, or completed training logic.
- Full specifications in DESIGN_SPEC.md and the Chinese design handoff document.
- Run 'node scripts/generate_assets.mjs' from package root to regenerate deterministic assets. No npm dependencies.

## Development flow for Codex

1. Read CODEX_START_HERE.md, DESIGN_SPEC.md, and the PICO CLI installation guide. Inspect existing project and device capabilities; do not assume Swan API details.
2. Confirm actual CLI setup scope/region/project with user only when performing setup, following the CLI installation guide. Build minimal real-device probes for eye tracking/eyelid closure, wear state, background notice, notification action route, PICO user profile, and input modes.
3. Review SVGs, audio and prototype with user. Produce missing production assets, especially 3D meshes, animations and licensed bilingual voice tracks, prior to business-feature development.
4. Build Spatial app using verified SDK APIs, implement local persistence, localization, training state machine and notifications.
5. Run on Swan hardware and record pass/fail/untested evidence. Export build, source and device test report.

No PICO SDK or project setup has been installed by this package. Refer to the PICO CLI installation guide for prerequisites and approvals.
