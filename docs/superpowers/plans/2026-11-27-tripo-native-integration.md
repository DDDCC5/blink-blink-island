# Tripo Storybook Native Integration Implementation Plan

> **For agentic workers:** Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Integrate the accepted storybook panel, island and repaired complete butterfly into the existing native PICO Spatial application.

**Architecture:** Keep the existing Stage, fixed panel raster/outline, ScenePlacementUseCase, ViewModel and repositories. Replace presentation resources and map existing native pages to the accepted design. Use the existing motion clock to control the GLB skeletal animation so pause and tracking-loss behavior stay synchronized.

**Tech Stack:** Kotlin, Spatial SDK6.1.9, SpatialUI, Gradle wrapper, local GLB assets; no toolchain install or SDK upgrade.

- [x] Inspect the dirty1.0.5 baseline and preserve its APK in deliverables/baseline-1.0.5.
- [ ] Inspect installed6.1.9 source/API contracts for animation resources, controller time and cleanup; the configured6.2 knowledge graph is not version-aligned, so do not treat it as6.1.9 authority.
- [ ] Update IslandColors/NatureArt/IslandPages/IslandContent/SessionPanels with the accepted palette, all-three-mode list and artwork. Keep action/state bindings, page return buttons, no scroll and pre-training-only language control.
- [ ] Import the audited GLBs. Set scale from measured bounds, preserve accepted space anchors, add the permanent island butterfly and use the complete butterfly as the pursuit target.
- [ ] Drive the skinned clip with the real target clock; validate no independent animation continues during pause/loss. Release controllers with entity lifetime.
- [ ] Bump this visual revision to1.0.6/code7; compile debug/release and run the existing meaningful domain/storage regression suite.
- [ ] Use an available owned emulator, inspect real UI bounds/logs and scene rendering; do not interrupt other projects' emulator/Editor sessions.
- [ ] Install/launch on the connected Swan within prior authorized development scope, inspect logs and ask only for actual wearer-only acceptance.
- [ ] Deliver versioned APKs, sources, build instructions, design traceability and evidence report. Do not claim full JSON fidelity or real gaze acceptance from static layout checks. Do not push GitHub without a new upload request.

Ruling: Reviewer found the recenter status row would shrink the new three-row homepage below its minimum height. Reuse the existing recenter button label for countdown/status; preserve all panel dimensions and room anchors. No extra content row.

## 1.0.7 wearer feedback revision
Remove the application recenter action/countdown/reset path; retain initial eye-level fixed placement and use system long-press recenter. Replace 30Hz paused skeletal seeking with continuous renderer animation, synchronized to the scored target clock and explicitly paused on loss/pause. Author four asymmetric beats with passive wingtip flex; lower body pitch and bank about body axis. No pursuit path or scoring changes. Build, audit and replace-install on Swan; wearer naturalness acceptance remains required.
