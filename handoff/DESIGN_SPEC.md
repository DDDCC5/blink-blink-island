# Design and interaction specification

## Visual design

Natural floating island, mint #D7ECE1, water #83D0D2, forest #398E86, cream #F6F5E9, peach #F6C9A6. Spatial UI panels: soft translucent pale cards, large targets, calm transitions. Avoid sharp flicker, rapid tracking objects and high-contrast medical imagery. Use programmatic bilingual text; generated image art contains no labels. SVGs are production-ready flat assets, not ready-made 3D environments.

## Pages

Onboarding (language, permission explanation, comfort advice); Home (complete session and each activity); Training selection; Blink; Butterfly; Gaze movement; Failure actions; Tracking interrupted; Done; Local statistics; Settings. prototype/index.html shows the main screens, but does not implement SDK interactions or persistence.

## User flow

First run → language and permission explanation → device capability check → home. A session may start as full set (blink → butterfly → gaze) or individual. Every action: tracking quality check → observe → pass or record incomplete → retry/skip. Tracking failure pauses timers and offers device checks. Session result persists locally. A wear-time reminder can route to start; later: +10 minutes while worn, today: mute until device-local next day. If an actual notification API cannot provide action buttons, surface the verified platform behavior and obtain product decision.

## Training

Blink: close fully and gently squeeze eyelids for 3 s, then open; ten rounds. Judge closure/open timing only, not squeeze force. If too short, incomplete; retry or skip. Butterfly: 60 s smooth visual tracking, normal blinks allowed, head comfortably steady. Evaluate gaze trajectory with quality threshold from hardware. Gaze movement: left→right→left ×10; up→down→up ×10; clockwise full circle ×5; counterclockwise full circle ×5. Eye and head tracking both needed; no time-only completion. Never imply virtual near/far images cause true accommodation.

## Reminder and storage

Count only time actively worn, accumulate across removals. 60-minute default, adjustable 30–120 minutes. Cross-app notifications and background wear-time accounting are mandatory capability validation gates; prototype alone proves neither. Local records distinguish completed/incomplete/skipped/exited, with training type, timestamp, attempted count and tracking interruption; do not store raw gaze traces by default. Read PICO nickname/avatar when permission and API permit; otherwise display local default.

## Audio behavior

SFX cues: count, closed, holding, open, success, incomplete, tracking pause, reminder and completion. Holding loop only while sustained closure, fade out at open/interruption. Keep BGM low; duck for voice/cues. Three separate controls: BGM, SFX and voice. All sounds are reference mixes; verify comfortable loudness and repetition on actual headset. Licensed bilingual voice narration is still to be produced from VOICE_SCRIPT.md.

## Accessibility and comfort

Gesture, controller and dwell-gaze selection; show dwell progress and avoid accidental activation. Eyes-closed steps rely on sound instead of visual feedback. Discomfort stops session. Never assert medical treatment or vision improvement. Allow opt-out and deletion of locally saved records.

## Acceptance evidence

For each device capability report documentation link, SDK version, headset OS version, minimal test, result, and limitations. Test wear/unwear cumulative counting, cross-app notification, notification routing, tracking loss, blink and head pose thresholds, input modes, audio settings, English/Chinese, offline record retention.
