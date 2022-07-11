pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }

    plugins {
        kotlin("jvm") version "1.7.0"
        id("org.jetbrains.compose") version "1.2.0-alpha01-dev741"
    }

    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "kotlinx-atomicfu") {
                useModule("org.jetbrains.kotlinx:atomicfu-gradle-plugin:0.18.2")
            }
        }
    }
}

rootProject.name = "schismatic"

include("app")
include("commons")
include("usb")
