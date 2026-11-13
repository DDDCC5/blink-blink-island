# Eye-following policy and panel redesign - 2026-11-13

## Requested behavior
User explicitly requires removing head/gyro constraints from butterfly and eye movement training, while reconstructing the left panel. This requirement overrides prior design head-stability limits and old head-assisted rejection tests; no unrelated layout/scene or eyelid capability change.

## Implemented
- GazeGeometry.evaluateEye checks only fresh/finite normalized eye pose, distance and angular target hit. Runtime no longer requires live HMD or timestamp pairing. Removed relative-eye-motion participation requirement. Legacy paired evaluate remains for initial scene placement diagnostics only.
- Calibration hold, Runtime path motion/freeze and Session scoring no longer read headStable. Head movement does not fail, freeze time or reset rounds; naturally turning to follow a target is allowed. Eye tracking loss/invalid samples still pause scoring, and missing the target still cannot complete a round. Existing60-second valid pursuit, ordered path/coverage scoring and3-second manual preparation preserved.
- New branded panel hierarchy: original SVG-derived mode illustrations, mode titles/subtitles/durations, quiet utilities, one primary session action, phase/large progress display and summary statistics. No head-correction prompts. Language remains WELCOME/HOME only; all pages remain non-scrolling with paginated history.
- Identified an actual attachment constraint: after redesign, content area was only987px high and four home buttons had empty bounds. SDK6.1 local attachment documentation/sample explicitly supports attachmentPanelComponent(size=panelSize(...)); applied1100dp x1120dp matching root. New device trace shows1787px home content area and all rendered home actions inside it. This fixes the real clipping instead of assuming the static preview proved fit.
-30 V2 female clips integrated; new content/VOICE_SCRIPT_V2.md removes head-stability instructions and technical pause language. Original handoff preserved. Model/voice choices unchanged from accepted voice. See NATURAL_VOICE_REBUILD_V2.md.

## Verification
- Domain red run:85 tests with6 expected assertion failures before implementation, then additional eye-only regressions. Final gaze-only-ui-final-build-tests.log:91 tests,0 failures/errors and assembleDebug pass. Cases include calibration/pursuit/full30 eye rounds without HMD, head-assisted following success, invalid/stale eye rejection, tracking loss and preparation.
- gaze-only-ui-style.log PASS0 errors/warnings; gaze-only-ui-architecture.log PASS. All30 APK WAV hashes match V2 manifest, new vector icons packaged.
- Read-only review found no remaining training head gate or control-access regression; corrected resume guidance to say target rather than center because resume preserves an off-center target.
- Connected new Swan PB3110MGL8190063B; delivery install and launch succeeded, finalPID29334, gaze-only-ui-delivery-install/launch.log. Prior fit evidence PID27211 in gaze-only-ui-final-runtime.log; all home component bounds passed after explicit size. Headset visual and complete natural-head-motion training acceptance pending.
- Knowledge query returned6.2 paths; implementation API evidence was checked in matching installed6.1 documents/sample and compiled against exact BOM6.1.9. Did not upgrade SDK or change host knowledge configuration.

## Locations
- Native UI: ui/island/components/IslandContent.kt, IslandPages.kt, TrainingPanel.kt, IslandActionButton.kt and ui/island/IslandScreen.kt.
- Domain: GazeGeometry.kt, GazeRuntime.kt, CalibrationUseCase.kt and SessionUseCase.kt.
- Review: review/panel-redesign-2026-11-13.html and .png (layout preview only, not actual Spatial screenshot).
- New script: content/VOICE_SCRIPT_V2.md, runtime voice manifest/audio.

Source/APK handoff refreshed locally. This turn does not push another public GitHub snapshot; existing GitHub release is the earlier backup.
