plugins {
    kotlin("jvm")
}

group = "org.schism"
version = "1.0-SNAPSHOT"

dependencies {
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.2")
    api("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.3.5")

    implementation(kotlin("reflect"))
    implementation("org.ow2.asm:asm:9.3")
}

kotlin {
    explicitApi()
}
