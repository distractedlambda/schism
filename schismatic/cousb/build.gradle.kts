plugins {
    kotlin("jvm")
}

dependencies {
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.1")
    implementation(project(":bytes"))
}
