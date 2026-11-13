> Superseded: user cancelled movement. Final fixed-panel state and accepted clipping fix: PANEL_VIEWPORT_AND_INPUT_FIX.md.

# Movable panel rebuild — 2026-11-13

## Requested and implemented
- Existing Stage attachment retained; title/brand region is a dedicated pinch3D drag target, with bilingual instruction and SpatialUI native hover. Action buttons remain separate click targets.
- Public6.1.9 `detectSpatialDragGesture` (2D view, target=null), `PhysicalLengthConverter.dpToLength` and `Entity.convertPositionTo` convert XYZ pixel increments to parent-space displacement, including panel yaw and scale. Y screen-down is inverted for world-up. Release/cancel leaves the last position. Navigation/training entry does not set panel placement; explicit Recenter restores it. Position lasts for the active scene, not cross-session anchoring.
- Shared36dp shape retained; public Android ViewOutlineProvider/clipToOutline adds a hardware outline using actual View dimensions. Capped rasterdensity1.8 gives1980x2016 rather than2200x2240 at hostdensity2, with reciprocal Entityscale1.111111 preserving logical content, text size and world dimensions. This is a compatibility mitigation, NOT a documented SDK2048px limit or a verified root-cause claim.
- No change to accepted gaze-only scoring, expanded paths, neural voice, island placement, no-scroll logical bounds or saved records.

## Evidence and limits
107 JVM tests pass, no failures/errors; assembleDebug passes (`movable-panel-build.log`). Includes raster-bounds and physical-size preservation across density1..4. Style0errors0warnings (`movable-panel-style.log`); architecture PASS0warnings (`movable-panel-architecture.log`). Test success does not prove device gesture or final compositor clipping.

Source: ui/island/IslandScreen.kt; components/IslandContent.kt, IslandScene.kt, PanelDragHandle.kt, PanelMotionController.kt, PanelRaster.kt. Matched official6.1 installed sample_interaction.md and public API docs; current MCP graph6.2 used only for discovery, implementation compiled against6.1.9. No private APIs.

New APK built. Replacement install attempt failed with no online device (`movable-panel-install.log`), so existing installed app is unchanged. Waiting for USB connection/authorization; then install/launch, confirm own runtime dimensions and no crash, and ask wearer to verify bottom corners and title pinch XYZ/release/button independence. Earlier wearer FAIL for bottom corners remains unresolved until that observation. Expanded small-ball range/reacquisition already PASS.

## Device installation follow-up
User reconnected and authorized USB. Replacement install succeeded, launchedPID8374. Runtime confirms1980x2016 root, native_outline=true, scene_loaded GLB and layout_locked_once. HOME text_fit/panel_fit checks alltrue; no AndroidRuntime error in this process log. Logs: movable-panel-install.log, movable-panel-launch.log, movable-panel-runtime.log. These confirm startup only; bottom corner appearance and actual pinch drag/release still await wearer.
