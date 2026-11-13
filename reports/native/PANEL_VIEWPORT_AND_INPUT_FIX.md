# Panel viewport and input follow-up — 2026-11-13

## Confirmed cause and accepted fixes
Previous application View measured1980×2016, but `dumpsys window windows` shows its attachment request1980×2016 and actual frame1980×1440. Software render/layout checks missed this platform window truncation. This directly explains bottom square edges, disappearing lower controls, and footer Back being inaccessible. No documented2048px cap is claimed. Evidence is own-application-only window metadata in attachment-window-crop-before.txt.

PanelRaster now derives output density from host configuration viewport2560×1440, leaving32px margin and preserving logical1100×1120 and physical size through reciprocal Entity scale. Current raster1383×1408, scale1.590909. After window metadata shows request1383×1408 == frame1383×1408 (attachment-window-frame-after.txt); runtime PID15134 confirms root matches. View outline36dp and Compose same shape remain.

Every non-home/welcome page now has header Back; delete returns to History, others to activities via existing save-aware navigation. History duplicate header return removed, training Pause/Skip and Finish/Back split into separate rows. No scroll, no training-score changes.

Wearer PASS: **四角和内容完整，返回正常**. User exercised Settings and Help with successful Back events; settings volume buttons also have native press/release receipts. New raster viewport tests,108 total JVM tests pass; build/style/architecture pass. Transient fit-probe stale-area during recenter resolves true in the next layout; not treated as persistent clipping.

## Drag still under verification
Wearer reported drag failure for both specialized SpatialDragGesture and generic SpatialPointerEvent header. Logs show no header callbacks while ordinary native Buttons get Press/Release. Hypothesis is missing runtime input targeting for the plain title region; no claim that the public APIs are unsupported.

Changed only the handle to SpatialUI native Button with interactionSource, readable Mint/Forest, native hover and empty click action. Public pointerInput observes Initial pass before native click consumption, `PointerInputChange.position3d()` supplies XYZ; root Entity conversion remains. Down does not move; move consumes; release leaves placement. Lifecycle cancellation drops pointer state. No raw input/eye/head traces persisted. Logs record only event receipt, first-move presence and boolean depth change. Installed/startedPID16777 after108tests/build, style0errors/warnings and architecturePASS. Logs panel-native-handle-*.log. Actual movement/release still pending wearer; not complete.

Source: PanelRaster.kt, IslandScreen.kt, IslandContent.kt, IslandPages.kt, TrainingPanel.kt and PanelDragHandle.kt. Preserve accepted corner/layout behavior while iterating input only.

## Final user decision and delivered state
User cancelled movable-panel work: “算了，取消可移动，恢复原来的位置吧”. Removed PanelDragHandle/PanelMotionController and all drag bindings. Restored original static brand/subtitle and original fixed left panel placement (local−.95,0,−1.2m with38.4degree yaw), with existing pre-training Recenter. Accepted viewport/corners/content and top Back remain. Built108tests, style/architecture PASS; replacement install/launchPID17492; runtime root1383x1408 == system frame1383x1408, scene loaded and layout locked; no process AndroidRuntime errors. Final evidence fixed-panel-final-*.log and fixed-panel-final-window.txt. Drag is cancelled by user, not claimed as working or pending acceptance.
