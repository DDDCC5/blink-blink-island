# Training startup, visible actions and pursuit speed - 2026-11-13

## Evidence and user clarification
User reports content still missing and apparent head-motion interruption. PID29334 post-redesign-feedback-runtime.log actually shows CAPABILITY_BLOCKED at old orange alignment point3 (index2), GAZE_OFF_TARGET with head_gate=disabled; no ACTIVE or INCOMPLETE occurred before Skip to summary. Therefore this observation did not establish a remaining runtime head-motion gate. The old app-owned five-point admission still blocked entry and has now been removed.

User clarified the missing item was Start full session, requested Finish/Back side-by-side, a visible return in History, and a modest butterfly speed increase.

## Implemented
- Production GazeRuntime uses gazeReady, not a calibrated claim: fixed scene reference plus fresh valid eye data admits training. Session then runs3s preparation before ACTIVE. No five-point target holding. Missing/stale/invalid eye cannot admit or advance the countdown and still pauses running training. Gaze target following is still required for real completion;3degree hit threshold and valid-time/path checks were not relaxed.
- Start full session moved above the three mode choices. Actual HOME action bounds moved from y1368..1528px to y532..692px; native component fit logs confirm it inside current content.
- Finish and Back to activities share the training action row. History has Back beside its top heading; removed duplicate lower Home action on History.
- Mode buttons use adaptive minimum height rather than fixed clipping height. Added public SpatialUI Text.onTextLayout checks for internal glyph overflow (text_fit), alongside geometry probes. Initial HOME measurements show no false text/component fit flags, but full-page multilingual/wearer viewing still needs acceptance.
- Butterfly period16s to14s, approximately14.3percent faster. Target position and body tangent/bank use one shared period; wing rhythm remains unchanged.
- Pre-training Recenter action waits3s for wearer to look forward, then resets placement from a fresh HMD position and yaw. Initial locked height was approximately0.83m; this does not prove the headset was on a desk, but the old path could not refresh after later wearing. Normal head movement/training never repositions scenery. Scene-applied-version acknowledgement keeps UI locked until the new reference is installed, avoiding a stale-reference race. Home Start then creates a new Runtime.

## Verification
- Red tests include actual VM startup readiness and speed mismatch; final startup-layout-followup-build-tests.log98 tests,0 failures/errors and assembleDebug pass. Includes eye-only readiness negative cases,3s startup/quality loss, complete pursuit and30 eye rounds, explicit placement reset, and14s periodic flight.
- startup-layout-followup-style.log PASS0 errors/warnings; architecture PASS. Public Text callback verified in installed6.1.9 source after version-mismatched graph lookup; no SDK/host update.
- Installed/launched final PID30352 on Swan PB3110MGL8190063B; startup-layout-followup-delivery-install/launch.log. Functional/layout wearer confirmation pending. Earlier fit observation PID29976 is preserved in startup-layout-followup-runtime.log.

Original handoff retained. Local source/APK package refreshed; this is not a new GitHub release.
