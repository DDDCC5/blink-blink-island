> Historical proposal; user cancelled movement on2026-11-13. Final app uses original fixed placement and viewport-adapted raster.

# Movable rounded control panel

User requests four rounded corners and pinch placement anywhere. Existing Stage, fixed island, center training, all pages, no-scroll content and logical1100x1120 layout remain. Header brand/subtitle becomes the dedicated3D drag target with bilingual instruction and SpatialUI hover. Buttons keep click behavior. Public6.1.9 detectSpatialDragGesture supplies XYZ pixel increments; public physical converter and Entity.convertPositionTo map them into parent coordinates. Release/cancel retains last placement; navigation/training never reapplies placement. Explicit Recenter restores default.

Use one panel-owned surface with symmetric36dp radius. Render the attachment at density capped to1.8 so both raster dimensions remain below2048px on currentdensity2hardware; reciprocal entity scale preserves physical dimensions and text size. This is a conservative raster compatibility mitigation, not a claim that the SDK documents a2048px limit. Public Android View outline clipping at the actual measured size provides a hardware clipping boundary in addition to Compose shape. No hidden APIs, window-container migration, or changes to scoring.

Alternatives: a system WindowContainer would supply system movement but changes the Stage/container model and scene relationship; whole-panel drag risks conflicting with training actions. Dedicated header with existing attachment is the bounded option chosen by implementer under user's refactor authorization. Within-session placement persists; restart starts at the default, cross-session anchors not requested.

Validate: density/scale and drag-vector domain checks,105 existing tests, build/style/architecture, install without data deletion, wearer checks all4corners, XYZ pinch movement, release persistence and button input. Own software screenshot alone is insufficient for corner acceptance. Current cause of final-display bottom clipping remains unconfirmed; SurfaceFlinger metadata denied to shell.
