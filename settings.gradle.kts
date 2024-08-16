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
        google()
        mavenCentral()
        maven(url="https://s01.oss.sonatype.org/content/repositories/comwhat3words-1448")
        maven(url="https://s01.oss.sonatype.org/content/repositories/snapshots/")
    }
}

rootProject.name = "what3words-components"
include(":lib")
include(":testing")
