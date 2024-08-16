plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-android")
    id("jacoco")
}

android {
    compileSdk = 34

    defaultConfig {
        applicationId = "com.what3words.testing"
        minSdk = 23
        targetSdk = 34
        versionCode = 1
        versionName = "1.1"

        buildConfigField("String", "W3W_PRE_PROD_URL", "\"${findProperty("W3W_PRE_PROD_URL")}\"")
        buildConfigField("String", "W3W_API_KEY", "\"${findProperty("PRE_PROD_API_KEY")}\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

       // testInstrumentationRunnerArguments = "true"
       // testInstrumentationRunnerArguments = "true"
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
    namespace = "com.what3words.testing"
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.android.material:material:1.12.0")
    implementation("com.what3words:w3w-android-wrapper:4.0.2")
    implementation(project(":lib"))

    // testing
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    implementation("androidx.test.espresso:espresso-contrib:3.6.1")
    debugImplementation("androidx.test:rules:1.6.1")
    debugImplementation("androidx.test.ext:junit:1.2.1")
    implementation("androidx.test.espresso:espresso-core:3.6.1")
    debugImplementation("androidx.test:runner:1.6.1")
    androidTestUtil("androidx.test:orchestrator:1.5.0")
}