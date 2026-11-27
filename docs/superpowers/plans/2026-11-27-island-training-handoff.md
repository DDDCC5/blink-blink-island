# Island Training Handoff Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Fly the island butterfly or ball into training, hold after automatic completion, return only after explicit finish.

**Architecture:** A pure TargetJourneyUseCase owns entry/hold/return progress and continuous world-space arcs. ViewModel gates observations while entering and retains last visual on summary. IslandScene presents the journey with existing native Entity assets; no new SDK or dependencies.

**Tech Stack:** Kotlin, Spatial SDK6.1.9, Compose SpatialUI, JUnit.

**Spec:** docs/superpowers/specs/2026-11-27-island-training-handoff-design.md (approved by user).

## Global Constraints

Fixed panel/island anchors; real gaze scoring unchanged. Entry/return never score. Pause/retry do not return. Explicit exit persists immediately and idempotently. No added paid generation or toolchain changes. Keep unrelated uncommitted work.

## Review Focus

Stale/missing eye data cannot start scoring; duplicate finish cannot restart return; summary retains target until confirmation; background time cannot jump animations; mode handoff must not create extra records.

### Task 1: Pure journey and regression tests

Files: domain/usecase/TargetJourneyUseCase.kt and domain/TargetJourneyUseCaseTest.kt.

Interfaces: begin(activity), tick(now,running), finish(), reset(), snapshot stage/progress/activity; arc(start,end,progress,ball) returns Point3.

- [x] Write failing tests for entry endpoints/no scoring interval, pause/background gap, hold until finish, repeated finish, zero-length curve.
- [x] Implement entry3000ms/return2000ms; cap valid frame gaps250ms; immutable journey snapshot.
- [x] Run focused JUnit tests.

### Task 2: ViewModel/scene/UI integration

Files: IslandViewModel.kt, IslandUiState.kt, IslandScreen.kt, IslandScene.kt, IslandPages.kt, TrainingPanel.kt.

- [x] Add ViewModel regressions: entry cannot accumulate practice, finish saves before return, automatic summary keeps visual, repeated exit saves once.
- [x] Integrate entry with existing preparation countdown; preserve final visual independently of active session; explicit finish/return selection initiates return.
- [x] Native scene computes island origin using fixed reference and local butterfly anchor; hides companion while it is participating; applies arc/tangent attitude and continuous wing clock. Ball starts at flower area. Keep scoring coordinates separate from presentation.
- [x] Add summary finish action using existing SpatialUI button component; block restart until return ends. Mirror accepted state in design IR.
- [x] Run whole JVM suite, build Debug/Release, style/architecture checks.

### Task 3: Review/device/delivery

- [x] Read-only final review, fix concrete regressions and rerun necessary checks.
- [ ] Swan offline. User requested emulator first: installed/launched on independent emulator-5558; native HOME/models verified. Full motion end-to-end pending ROM input backend / wearer.
- [x] Deliver source/APK/report; record wearer-only acceptance and any tooling gate limitations. No GitHub push requested.

Verification: 137 JVM tests pass; Debug/Release, architecture and design-style pass. Final read-only review found no remaining P1/P2 after HANDOFF edge fixes. Native simulator launch and HOME fit verified; spatial query reports ROM_BACKEND_UNAVAILABLE, pose injection missing. Full journey playback is not claimed.
