import kotlinx.atomicfu.plugin.gradle.AtomicFUPluginExtension

plugins {
    kotlin("jvm")
}

apply(plugin = "kotlinx-atomicfu")

dependencies {
    api(project(":commons"))
}

kotlin {
    explicitApi()
}

extensions["atomicfu"].delegateClosureOf<AtomicFUPluginExtension> {
    jvmVariant = "VH"
}()
