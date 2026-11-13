# Rounded panel and expanded waiting gaze paths - 2026-11-13

## Training changes and wearer result
User requested larger small-target horizontal/vertical and circular paths and no gyro-based difficulty. Training already had no head gate; prior failures were gaze timeout/coverage branches, not head pose. GAZE now freezes the visible target and effective scoring while gaze is off target, resumes from that point when reacquired, and does not fail/reset the round. The first reacquisition frame adds no unobserved time. Head movement remains allowed; tracking loss still pauses. Butterfly behavior is unchanged.

GAZE horizontal endpoints +/-15degrees, vertical +/-10degrees, circle endpoints at radius1.8*tan12degrees (about0.383m) at the same virtual1.8m depth. Horizontal/vertical transitions3s, circle2s. Circle is still12 smooth chord segments through equal-radius endpoints. No real accommodation/physical focusing claim.

App HMD provider is now stopped during training after initial scene placement; Eye remains started. Own eye/head conversions are isolated. System head tracking needed for XR rendering is not disabled. Actual runtime PID31527 recorded Eye SUPPORTED/STARTED and HMD SUPPORTED/STOPPED during training.

105 tests/build pass, rounded-gaze-wait-final-build-tests.log. Styles0 errors/warnings, architecture pass. New tests cover range/period, waiting5s without failure, reacquisition no catch-up, preserved target and eye-only full30 rounds. Wearer explicitly confirms expanded range is suitable and reacquiring gaze continues without reset. Earlier full-session/Back/History actions were also confirmed normal.

## Corner investigation remains open
First attempt: one shared36dp RoundedCornerShape for clip/background,8dp transparent outer inset and32dp inner padding (total content offset unchanged40dp). Wearer reports bottom still square, so do not claim corner issue fixed.

A broad FOV capture did not contain the complete panel and was removed; not retained as visual proof. Temporary debug helper used only public AttachmentPanelComponent.content View.draw, never a parent/window/passthrough. Own panel software PNG panel-own-view-diagnostic.png is2200x2240, all public parent views also2200x2240, matching desired size. Pixel alpha at all4raster/card corners and9dp offset=0, at18dp offset=255; center column opaque16..2223 exactly matches8dp inset atdensity2. Thus own software raster is symmetric and not clipped. This does not verify hardware layers or final XR compositor.

Logs: panel-render-diagnostic-runtime.log. Helper source archived locally as reports/native/PanelRenderEvidence.kt and removed from production app wiring after evidence collection. No automatic own-view capture retained in delivery source. Current diagnostic install PID32513; final display investigation requires wearer to look at complete panel for a fresh final-view observation. Async request pending.

Next evidence needed: whether full panel including bottom corners can be viewed, then final-view/layer crop investigation. Do not keep changing radius based only on software pixels or claim the user's observation is disproved.
