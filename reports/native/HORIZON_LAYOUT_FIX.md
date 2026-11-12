# Horizontal persistent layout - 2026-11-12

User reports left panel and right island not at horizontal eye level on launch. Source root cause: ScenePlacementUseCase captured the complete initial head rotation, including pitch and roll, then rotated both scene offsets and orientations with it. Once locked, that launch tilt remained. Previous diagnostic eye_level=true was an intent label, not a measured validation; it did not prove a horizontal composition.

Fix: validate the initial fresh pose, project its forward vector into the world XZ plane, preserve yaw only, and retain the initial head position/height. The shared layout reference has zero X/Z quaternion components. Panel and island offsets both have Y=0; rotations remain upright. Both persistent transforms still write once. Training center inherits this fixed horizontal reference, while actual head-stability baseline remains separate. Near-vertical initial direction has no reliable horizontal heading, so wait for a usable sample instead of freezing an arbitrary yaw. No SDK/API/dependency changes.

Placement relative to captured eye-height origin: left panel(-0.95,0,-1.2)m at scale1; right scenery visual center(1.55,0,-2.4)m at scale0.45; center training target(0,0,-1.8)m before its existing paths. These are initial fixed world locations, not head-locked followers.

Verification: horizon-layout-red.log reproduces2 failures; horizon-layout-build-tests.log78 tests,0 failures/errors and assembleDebug passed. Tests combine yaw60/pitch25/roll18, verify equal eye-level Y, upright local up, heading preservation, and vertical-gaze deferral. Existing once-locked and central-target tests still pass.

Swan replacement and launch succeeded, PID32156, horizon-layout-install.log and horizon-layout-launch.log. Actual wearer horizontal/visibility acceptance pending; build/math tests do not replace this.


Review follow-up: if tracking becomes valid during training before the scene's first placement tick, the app previously could capture a tilted training reference first. VM now waits for fixed sceneReference before initializing eye training; regression first failed in horizon-first-frame-red.log. horizon-layout-final-build-tests.log:79 tests and final build pass. Final replacement/start evidence: horizon-layout-final-install/launch.log. This closes the first-frame ordering gap without moving persistent objects.

Final process PID1145. Wearer acceptance still pending.
