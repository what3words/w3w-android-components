pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenLocal()
        google()
        mavenCentral()

        maven(url = "https://s01.oss.sonatype.org/content/repositories/snapshots/")
        maven(url = "https://central.sonatype.com/repository/maven-snapshots/")
    }
    versionCatalogs {
        create("libs") {
            from("com.what3words:android-version-catalog:2026.06.01")

            // ---- Local overrides ----
            // This library supports a lower minSdk than the shared catalog default.
            version("minSdk", "23")
        }
    }
}

rootProject.name = "what3words-components"
include(":lib")
include(":testing")
