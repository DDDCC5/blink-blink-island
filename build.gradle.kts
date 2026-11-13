// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
}

// Cloud builders discover APKs at the conventional repository-root app/build path.
project(":app") {
    layout.buildDirectory.set(rootProject.layout.projectDirectory.dir("app/build"))
}
