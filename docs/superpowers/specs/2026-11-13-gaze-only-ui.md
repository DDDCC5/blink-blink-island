# Gaze-only training and panel reconstruction

Explicit user change supersedes the original head-stability requirement in handoff/DESIGN_SPEC.md for BUTTERFLY and GAZE. Only world-space gaze following is judged. Calibration hold, path timing and scoring must not depend on head angle/translation/paired HMD freshness or eye motion relative to head. Initial HMD pose is still used once for the accepted fixed horizontal room layout. Eye freshness/finite validity, target following, valid-time scoring and tracking pauses remain.

UI: preserve horizontal scene positions and no-scroll interaction. Rebuild visual hierarchy with branded header, original SVG-derived mode illustrations, three mode choices, distinct primary/secondary/quiet native buttons, larger progress and clear round state, and four summary metrics. Language stays pre-training only. No developer error/SDK/gyro text in the training UI. Use matched6.1 public panelSize to give the attachment1100x1120dp, avoiding actual default-height clipping discovered in device logs.

Keep the accepted female voice and butterfly flight. New narration V2 removes head-stability instructions and developer terms; original handoff is preserved. UI preview is a design artifact, not a device screenshot or app replacement.

Validation: new policy negative-first then green tests, full91 JVM cases, build/style/architecture, packaged30 V2 WAV hashes, actual home component fit and launch/crash observations, wearer visual and natural-head-motion acceptance pending.
