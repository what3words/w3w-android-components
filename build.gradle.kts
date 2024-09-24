// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    repositories {
        mavenCentral()
        google()
        maven(url = "https://plugins.gradle.org/m2/")
    }
}

plugins {
    id("org.jetbrains.kotlin.android") version "1.8.20" apply false
    id("com.android.application") version "8.5.0" apply false
    id("org.jlleitschuh.gradle.ktlint") version "10.1.0" apply true
    id("com.autonomousapps.dependency-analysis") version "1.20.0" apply true
}

tasks.register<Delete>("clean").configure {
    delete(rootProject.layout.buildDirectory)
}
