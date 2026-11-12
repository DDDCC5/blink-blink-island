# Blink Blink Island Native App Implementation Plan

Goal: Continue the supplied design into a native Spatial development build with genuine offline app behavior and explicit capability gates.
Architecture: Kotlin domain state machine and reminder clock; SQLite repository; ViewModel; SpatialUI attached panel and existing GLB scene in a Mixed Stage. Sensor adapter uses only compiled public 6.1.9 APIs. No invented eyelid, user-center or background wear APIs.
Tech stack: Kotlin 2.1.20, Spatial BOM6.1.9, Android SQLite, MediaPlayer/SoundPool, JUnit.
Spec: handoff/DESIGN_SPEC.md and handoff/Blink_Blink_Island_设计与开发交接包.md; current review/index.html.
Authorization: User requested continued development after reviewing the current progress. Continue with the delivered design; no new setup, PATH or machine configuration changes.

Constraints: 3s×10 blink; 60s evaluable pursuit; horizontal/vertical×10, clockwise/counterclockwise×5; reminder60min adjustable30–120, later10min worn-time; no force/accommodation claims; all persisted training outcomes separate; delete local records; no raw gaze persistence. Unknown sensor capabilities remain gated, never silently replaced.
Review focus: invalid/stale sensor input; pause/resume time jumps; skipped actions falsely counted successful; duplicate save after lifecycle changes; deleted data recreated by a live session.

## Tasks
- [x] 1. Generate native/ from official CLI with explicit6.1.9 and distinct dev application identity; preserve probes/ and root integration.
- [x] 2. Add failing domain tests for session progress, pause/stale gaps, capabilities, retry/skip/exit, and reminder wear/unwear/later/day silence; implement domain models and usecases; run tests.
- [x] 3. Implement SQLite records/settings repository and ViewModel; instrumentation tests use a dedicated test database, verify recreation/idempotent saves/deletion.
- [ ] 4. Implement supplied bilingual pages using SpatialUI/PicoTheme, audio settings/ducking/closure cues, local fallback profile, notification routes and capability disclosures. Integrate existing GLBs with explicit source/blocker record. No unsupported platform claims.
- [ ] 5. Build/test, install development APK to authorized Swan, inspect crash logs and capture spatial screenshot. Fix actionable failures. Update phase/evidence reports and delivery archive.

Execution: Inline in this chat as requested. Current checkout is not a git repository; preserve existing workspace rather than creating a worktree. Tests and build logs under reports/native/. Domain tests prove algorithms, not hardware calibration. Native visual and interaction acceptance requires device observation.

Progress: 39 JVM tests and 2 Swan storage tests passed. User confirmed native scene/welcome, readable text, and exit record not counted complete. Task4 remains partial: real closure audio cues, training calibration, PICO profile, wear reminders not integrated. Task5 has successful native build/install/launch but full machine workflow receipts remain blocked; do not fabricate them. GazeGeometry is implemented/tested as the next calibration building block, not runtime-admitted. Detailed evidence and remaining acceptance are in reports/native/VALIDATION_REPORT.md.
