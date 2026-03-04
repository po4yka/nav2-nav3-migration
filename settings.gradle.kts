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
include(":lab-contracts")
include(":lab-engine")
include(":lab-host-fragment")
include(":lab-host-nav2")
include(":lab-host-nav3")
include(":lab-deeplink")
include(":lab-back")
include(":lab-results")
include(":lab-testkit")
