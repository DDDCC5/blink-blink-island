# Storybook integration verification - 2026-11-27

Current native build: 1.0.7 / code8. See reports/native/RELEASE_1_0_7.md for Chinese release details and evidence paths.

## Assets

Seven Tripo tasks succeeded, total 430 credits; no paid retries or additional generation for this animation revision. Retired split butterfly parts remain historical only.

Active butterfly: 8,509 triangles, 9,391 vertices, one material and embedded texture. Source head relief was repaired locally; outer-wing geometry and UVs preserved. See anatomical-rebuild/head-repair-verification.json.

NaturalWingbeat has seven bones, four quaternion tracks, 145 keys per track and a 0.72-second loop. Weights, joint indices, monotonic times, normalized quaternions and matching loop endpoints were checked. See anatomical-rebuild/rig-verification.json. This is a visual animation, not a measured species simulation. The generated blue underside remains artistic.

Exports were saved and reloaded in the gallery. Both GLBs are self-contained. Native scene budget: island 36,908 instanced triangles plus two butterflies = 53,926. See exports/manifest.json.

## Native evidence

Debug/Release builds and 127 JVM tests pass; style and architecture checks pass without warnings. Swan reports version1.0.7/code8, both skeleton controllers valid=true duration=0.72, and continuous companion playback paused=false. Initial level placement locks once.

The SDK6.1.9 renderer now advances the wing animation continuously, synchronized to the scored motion clock. Pause/tracking-loss/absent-target paths hold animation. Scored target paths are unchanged. Application recenter button/countdown/reset were removed; system long-press recenter is used. Training preparation/retry countdowns remain.

Previous isolated SQLite instrumentation: three tests passed. This does not establish full Spatial compatibility on the 6.2 emulator.

## Pending

Wearer acceptance of new flight naturalness, real eye-tracked pause/resume and system recenter remains pending. 506-node/17-state design JSON validates against the schema, but formal design_fidelity still fails due to missing complete native implementation mapping. Browser preview is not training/permission validation.

Managed Editor packaging failed; direct self-contained GLB loading uses official local SDK6.1 documentation. See reports/native/TRIPO_INTEGRATION_TOOLING.md.

## Reproduce

Run python3 scripts/serve_tripo_review.py, open gallery.html?asset=butterfly-export under review/tripo-storybook-2026-11-27 on localhost:8769, inspect delivered playback/pause. The complete butterfly source option rebuilds the repaired rig from anatomical-rig.js. Run python3 scripts/audit_storybook_exports.py for file auditing. Follow native/README.md to build and exercise real tracking on Swan.

Motion reference: https://pubmed.ncbi.nlm.nih.gov/33468023/ (flexible wings and asymmetric strokes only).
