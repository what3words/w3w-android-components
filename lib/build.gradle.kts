import java.net.URI

plugins {
    id("com.android.library")
    id("kotlin-android")
    id("maven-publish")
    id("signing")
    id("org.jetbrains.dokka") version "1.5.0"
}

/**
 * IS_SNAPSHOT_RELEASE property will be automatically added to the root gradle.properties file by the CI pipeline, depending on the GitHub branch.
 * A snapshot release is generated for every pull request merged or commit made into an epic branch.
 */
val isSnapshotRelease = findProperty("IS_SNAPSHOT_RELEASE") == "true"
version =
    if (isSnapshotRelease) "${findProperty("LIBRARY_VERSION")}-SNAPSHOT" else "${findProperty("LIBRARY_VERSION")}"


android {
    compileSdk = 34

    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        viewBinding = true
        compose = true
        buildConfig = true
    }

    defaultConfig {
        minSdkVersion(23)
        targetSdkVersion(34)
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
        buildConfigField("String", "VERSION_NAME", "\"${version}\"")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    testOptions.unitTests.apply {
        isReturnDefaultValues = true
    }

    buildTypes {
        named("debug") {
            enableUnitTestCoverage = false
        }
        named("release") {
            isMinifyEnabled = false
            setProguardFiles(listOf(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"))
        }
    }

    publishing {
        multipleVariants {
            withSourcesJar()
            withJavadocJar()
            allVariants()
        }
    }

    tasks.dokkaGfm.configure {
        suppressObviousFunctions.set(true)
        suppressInheritedMembers.set(true)
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.6"
    }
    namespace = "com.what3words.components"
}

dependencies {
    api("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("com.google.android.material:material:1.12.0")

    // kotlin
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")


    // what3words wrapper
    api("com.what3words:w3w-android-wrapper:4.0.2")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.airbnb.android:lottie:6.1.0")

    // compose
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.constraintlayout:constraintlayout-compose:1.0.1")

    // testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("androidx.test:core:1.6.1")
    testImplementation("com.google.truth:truth:1.4.2")
    testImplementation("io.mockk:mockk:1.13.5")
    testImplementation("org.json:json:20230618")
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}

//region publishing

val ossrhUsername = findProperty("OSSRH_USERNAME") as String?
val ossrhPassword = findProperty("OSSRH_PASSWORD") as String?
val signingKey = findProperty("SIGNING_KEY") as String?
val signingKeyPwd = findProperty("SIGNING_KEY_PWD") as String?

publishing {
    repositories {
        maven {
            name = "sonatype"
            val releasesRepoUrl =
                "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
            val snapshotsRepoUrl =
                "https://s01.oss.sonatype.org/content/repositories/snapshots/"
            url = if (version.toString()
                    .endsWith("SNAPSHOT")
            ) URI.create(snapshotsRepoUrl) else URI.create(releasesRepoUrl)

            credentials {
                username = ossrhUsername
                password = ossrhPassword
            }
        }
        publications {
            create<MavenPublication>("Maven") {
                artifactId = "w3w-android-components"
                groupId = "com.what3words"
                version = project.version.toString()
                afterEvaluate {
                    from(components["release"])
                }
            }
            withType(MavenPublication::class.java) {
                val publicationName = name
                val dokkaJar =
                    project.tasks.register("${publicationName}DokkaJar", Jar::class) {
                        group = JavaBasePlugin.DOCUMENTATION_GROUP
                        description = "Assembles Kotlin docs with Dokka into a Javadoc jar"
                        archiveClassifier.set("javadoc")
                        from(tasks.named("dokkaHtml"))

                        // Each archive name should be distinct, to avoid implicit dependency issues.
                        // We use the same format as the sources Jar tasks.
                        // https://youtrack.jetbrains.com/issue/KT-46466
                        archiveBaseName.set("${archiveBaseName.get()}-$publicationName")
                    }
                artifact(dokkaJar)
                pom {
                    name.set("w3w-android-components")
                    description.set("Android autosuggest components for what3words API and SDK.")
                    url.set("https://github.com/what3words/w3w-android-components")
                    licenses {
                        license {
                            name.set("The MIT License (MIT)")
                            url.set("https://github.com/what3words/w3w-android-components/blob/master/LICENSE")
                        }
                    }
                    developers {
                        developer {
                            id.set("what3words")
                            name.set("what3words")
                            email.set("development@what3words.com")
                        }
                    }
                    scm {
                        connection.set("scm:git:git://github.com/what3words/w3w-android-components.git")
                        developerConnection.set("scm:git:ssh://git@github.com:what3words/w3w-android-components.git")
                        url.set("https://github.com/what3words/w3w-android-components")
                    }
                }
            }
        }
    }
}

signing {
    useInMemoryPgpKeys(signingKey, signingKeyPwd)
    sign(publishing.publications)
}

//endregion