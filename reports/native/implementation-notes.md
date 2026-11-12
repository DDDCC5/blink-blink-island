# Implementation notes

- Initial managed codegen run failed with SPATIAL_CODEGEN_BUILD_INPUT_CHANGED: app/build.gradle.kts. Cause: the caller modified app build configuration while completing the codegen handoff. No SDK/Editor failure is inferred. Configuration now finalized; a new managed run will verify the fixed native scene source under the stable configuration.
- Domain test correction: early closure and interruption tests initially used >250ms sample gaps, conflicting with the explicit stale-sample rule. Replaced those fixtures with50ms observations, preserving their actual targeted assertions; dedicated stale-gap test remains.
- SDK6.1.9 native app, matching6.1 knowledge loaded in an ephemeral MCP server without editing root setup selector.
- Second managed run failed SCENE_ENTRY_MISMATCH: returned handoff had an implicit outputPath app/src/main/java/SpatialGeneratedScene.kt. The supplied sceneEntry did not match that default. Next request explicitly sets outputPath and uses native/ as the workspace boundary, so every Kotlin path is project-relative. No backend code or managed state was edited.
