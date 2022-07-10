import kotlinx.atomicfu.plugin.gradle.AtomicFUPluginExtension

plugins {
    kotlin("jvm")
}

apply(plugin = "kotlinx-atomicfu")

dependencies {
    api(project(":commons"))
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(19))
    }

    explicitApi()
}

extensions["atomicfu"].delegateClosureOf<AtomicFUPluginExtension> {
    jvmVariant = "VH"
}()
