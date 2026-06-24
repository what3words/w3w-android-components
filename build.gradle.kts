// Top-level build file where you can add configuration options common to all sub-projects/modules.

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.dokka) apply false
    alias(libs.plugins.gradle.ktlint) apply false
    alias(libs.plugins.autonomousapps.dependency.analysis)
}

tasks.register<Delete>("clean").configure {
    delete(rootProject.layout.buildDirectory)
}
