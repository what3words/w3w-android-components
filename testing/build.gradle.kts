import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    id("jacoco")
}

jacoco {
    toolVersion = libs.versions.jacoco.core.get()
}

android {
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.what3words.testing"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.1"

        buildConfigField("String", "W3W_PRE_PROD_URL", "\"${findProperty("W3W_PRE_PROD_URL")}\"")
        buildConfigField("String", "W3W_API_KEY", "\"${findProperty("PRE_PROD_API_KEY")}\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    testOptions {
        execution = "ANDROIDX_TEST_ORCHESTRATOR"
        unitTests.isReturnDefaultValues = true
    }

    buildTypes {
        named("debug") {
            enableUnitTestCoverage = true
        }
        named("release") {
            isMinifyEnabled = true
            setProguardFiles(listOf(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"))
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(libs.versions.jvmToolchain.get())
        targetCompatibility = JavaVersion.toVersion(libs.versions.jvmToolchain.get())
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
    namespace = "com.what3words.testing"
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.fromTarget(libs.versions.jvmToolchain.get())
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.android.material)
    implementation(libs.w3w.android.wrapper)
    implementation(project(":lib"))

    // testing
    androidTestImplementation(libs.androidx.test.ext.junit)
    implementation(libs.androidx.test.espresso.contrib)
    debugImplementation(libs.androidx.test.rules)
    debugImplementation(libs.androidx.test.ext.junit)
    implementation(libs.androidx.test.espresso.core)
    debugImplementation(libs.androidx.test.runner)
    androidTestUtil(libs.androidx.test.orchestrator)
}
