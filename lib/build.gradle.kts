import java.net.URI
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.dokka)
    id("maven-publish")
    id("signing")
}

/**
 * IS_SNAPSHOT_RELEASE property will be automatically added to the root gradle.properties file by the CI pipeline, depending on the GitHub branch.
 * A snapshot release is generated for every pull request merged or commit made into an epic branch.
 */
val isSnapshotRelease = findProperty("IS_SNAPSHOT_RELEASE") == "true"
version =
    if (isSnapshotRelease) "${findProperty("LIBRARY_VERSION")}-SNAPSHOT" else "${findProperty("LIBRARY_VERSION")}"


android {
    compileSdk = libs.versions.compileSdk.get().toInt()

    buildFeatures {
        viewBinding = true
        compose = true
        buildConfig = true
    }

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
        buildConfigField("String", "VERSION_NAME", "\"${version}\"")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(libs.versions.jvmToolchain.get())
        targetCompatibility = JavaVersion.toVersion(libs.versions.jvmToolchain.get())
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
        singleVariant("release") {
            withSourcesJar()
        }
    }

    namespace = "com.what3words.components"
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.fromTarget(libs.versions.jvmToolchain.get())
    }
}

dependencies {
    api(libs.androidx.recyclerview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.android.material)

    // kotlin
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)


    // what3words wrapper
    api(libs.w3w.android.wrapper)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.lottie)

    // compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.constraintlayout.compose)

    // testing
    testImplementation(libs.junit)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.truth)
    testImplementation(libs.mockk)
    testImplementation(libs.org.json)
    testImplementation(libs.androidx.arch.core.testing)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
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
                        from(tasks.named("dokkaGeneratePublicationHtml"))

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
