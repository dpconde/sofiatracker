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

rootProject.name = "Sofia Tracker"
include(":app")
include(":core:ui")
include(":core:designsystem")
include(":feature:statistics")
include(":core:datastore")
include(":core:database")
include(":core:network")
include(":feature:home")
include(":feature:settings")
include(":core:domain")
include(":core:data")
include(":core:model")
