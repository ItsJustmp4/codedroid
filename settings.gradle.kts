pluginManagement {
    repositories {
        google(); mavenCentral(); gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositories {
        google(); mavenCentral()
    }
}
rootProject.name = "CodeDroid"
include(":app")
