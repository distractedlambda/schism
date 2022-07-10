import kotlinx.atomicfu.plugin.gradle.AtomicFUPluginExtension

plugins {
    kotlin("jvm")
}

apply(plugin = "kotlinx-atomicfu")

dependencies {
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.3")
    api("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.3.5")

    implementation(kotlin("reflect"))
    implementation("org.ow2.asm:asm:9.3")
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
