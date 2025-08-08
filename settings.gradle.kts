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
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "SofiaTracker"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

includeBuild("build-logic")

include(":app")
include(":core:ui")
include(":core:designsystem")
include(":core:datastore")
include(":core:database")
include(":core:network")
include(":core:domain")
include(":core:data")
include(":core:model")
include(":core:common")

include(":feature:home")
include(":feature:settings")
include(":feature:statistics")
