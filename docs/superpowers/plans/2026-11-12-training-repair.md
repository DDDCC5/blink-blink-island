# Three-mode repair

User scope: repair the original three training activities; existing browser Next is not judgment. Keep package, localized name, branded launcher and persisted records.

Root causes: unconditional false capabilities; discarded SDK poses; no moving training target; no calibration; static generic instructions; UI-only observer interval; session outcome could remain incomplete after successful retry. No public eyelid field has been evidenced in Spatial6.1.9.

Implementation:
1. Reproduce domain failures and add tests for targets, calibration, real geometry-to-session flow, tracking/phase resets, and retry outcome.
2. Add runtime alignment: five gaze targets relative to initial head pose, freshness/support checks, monotonic clock, same-space converted Eye/HMD poses. Calibration failures cannot grant admission. Numeric tolerances are development parameters needing hardware validation, not hardware specification.
3. Add actual butterfly pursuit path, left-right-left/up-down-up targets, closed clockwise/counterclockwise paths with continuity. Freeze on pause/loss/failure. Use existing GLB butterfly and an unlit marker; preserve island art. Main-thread ECS transform updates; no scene ownership rewrite.
4. Connect per-mode instructions, counts, permission/calibration recovery, audio and session state. Closed-eye remains explicitly unavailable unless a public eyelid source is verified; no loss-of-eye-data inference, time-only success or invented API.
5. Build/test, install on authorized Swan, observe path and gaze hit/miss/pause/head movement, update honest report and archives. Ask a specific product decision for any unavoidable downgrade only after API investigation.

Acceptance: real calibrated gaze qualifies pursuit for60 valid seconds; gaze motions require ordered geometric targets and full circles, no static-target repeat success; uncalibrated/invalid data never passes; pause freezes target and timer; closed eye never claims measured closure without source. Use synthetic tests for algorithms and wearer observations for hardware, separately.
