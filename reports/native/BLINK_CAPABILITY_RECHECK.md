# Close-and-rest entry restoration and API recheck - 2026-11-12

User explicitly requested restoration and asked whether eye tracking can detect closed eyes. Home now restores BLINK as the first independent action, followed by BUTTERFLY and GAZE. Existing bilingual availability explanation and blocked-mode view remain. This restores navigation, not fabricated closure sensing.

Current app BOM:6.1.9, native/gradle/libs.versions.toml. Official version-matched public EyeTrackingData lists eyePose and timestamp only; EyePose lists position and rotation. No eye openness, per-eye blink or eyelid state is exposed in these types.

Official exact-version reference:
https://developer-cn.picoxr.com/spatial-api/6.1.9/trackingpack/tracking/com.pico.spatial.tracking.eye/-eye-tracking-data/index.html
https://developer-cn.picoxr.com/spatial-api/6.1.9/trackingpack/tracking/com.pico.spatial.tracking.eye/-eye-pose/index.html

Local matching references: /Users/chanzungsan/Library/PICO/sdk/6.1/agent-vault/spatial/api-reference/com.pico.spatial.tracking.eye.md and documentation/spatial-sdk_tracking_eye-tracking.md. Tracking loss, stale data and permission/state transitions are not classified as closed eyes. Observation.eyesClosed stays null; existing missingEyelidBlocksBlinkWithoutCountingSuccess test protects this. User previously rejected timer-only/guided fallback, so no replacement is enabled.

Other SDKs: public PXR_EyeTracking openness methods are Unity APIs with explicitly limited device support; they do not establish Swan Kotlin availability. Do not copy API names into this app or change SDK/toolchain automatically. Current evidence is absence in the inspected public Spatial interface, not proof that Swan hardware cannot sense eyelids. A supported Swan-compatible public API/extension and true-device open/closed/tracking-loss verification are still required.

Verification: restore-blink-entry-build-tests.log (76 tests, assembleDebug successful), restore-blink-entry-style.log PASS0 errors/warnings; restore-blink-entry-install.log Success; restore-blink-entry-launch.log entry started, PID30761. Wearer home-entry observation pending. No change to permissions or local records.
