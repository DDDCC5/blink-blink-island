# Cloud build entry repair — 2026-11-13

The submitted commit8a4da7bbdf090dc80eafd1aae2e649fda101fb00 put its Gradle root under native/. VibeLab executed assembleRelease at the repository root, found neither gradlew nor settings.gradle.kts, and fell back to systemGradle8.11.1. Build failed before compiling app code. Locally reproduced the same missing-build error from the root (cloud-root-before.log). The previous delivery verified native/ debug builds but did not verify the platform's root Release entry.

Added root gradlew/gradlew.bat, complete gradle/wrapper, settings.gradle.kts, build.gradle.kts and gradle.properties. Root build reuses native/app sources and native/gradle/libs.versions.toml; output is conventional root app/build/outputs/apk/release/app-release-unsigned.apk. Original native/ standalone build remains unchanged. Wrapper8.13 matches existing AGP8.13.2; no SDK or business/UI change. No signing keys added. Source packager now includes the root entry and wrapper too.

Verification and final published commit recorded below after commands complete. The exact VibeLab init script is not available locally; reproduction uses an equivalent init script injecting versionCode1 through the Android DSL. Actual VibeLab rerun remains to be observed.

## Verification completed
- Repository-root Wrapper assembleRelease with equivalent versionCode1 init script: PASS,49tasks executed. APK metadata package com.example.blinkblinkisland.dev, version1.0/code1.
- Fresh tracked-source export (git checkout-index, no .git, local.properties, existing project build output or host integration): root assembleRelease testDebugUnitTest with same injection PASS,73tasks executed;108tests0failures/errors. Android SDK supplied through ANDROID_HOME, JDK21. Dependency cache may be reused; no claim of a fully uncached download or actual Jenkins run.
- Root gradlew recorded executable100755; source-ZIP packaging checks root settings/build/wrapper entries and executable bit.
- Logs retained locally: cloud-root-release.log and cloud-clean-release-tests.log. Cloud service must rerun on the new published commit; old8a4da7b still has the broken root.
