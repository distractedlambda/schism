plugins {
    kotlin("jvm")
    id("kotlinx-atomicfu")
}

dependencies {
    api(kotlin("reflect"))
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.3")
    api("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.3.5")
    api("org.ow2.asm:asm:9.3")
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(19))
    }

    explicitApi()
}

atomicfu {
    jvmVariant = "VH"
}
