pluginManagement {
    includeBuild("build-logic")
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

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "NavigationInteropLab"

include(":app")
include(":lab-catalog")
include(":lab-contracts")
include(":lab-engine")
include(":lab-host-fragment")
include(":lab-host-nav2")
include(":lab-host-nav3")
include(":lab-deeplink")
include(":lab-back")
include(":lab-results")
include(":lab-testkit")
include(":lab-recipes")
