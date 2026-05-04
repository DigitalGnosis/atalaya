/*
 * Atalaya — top-level Gradle settings.
 * Patterns ported from Now in Android (commit 7d45eae) and Bitwarden Android (commit 6ba5159).
 */

pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
    }
}

rootProject.name = "atalaya"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(":apps:node")
include(":packages:core-protocol")
include(":packages:core-onboarding")
include(":packages:core-rules")
include(":packages:core-ml")
include(":packages:core-sensors")
include(":packages:core-observability")
include(":packages:core-ui-base")
include(":packages:core-ui-theme")
include(":packages:core-ui-components")

check(JavaVersion.current().isCompatibleWith(JavaVersion.VERSION_17)) {
    """
    Atalaya requires JDK 17 or later but is currently using JDK ${JavaVersion.current()}.
    AGP 9.x compiles with JDK 21 by default; install JDK 21 (Temurin or similar).
    Java Home: [${System.getProperty("java.home")}]
    """.trimIndent()
}
