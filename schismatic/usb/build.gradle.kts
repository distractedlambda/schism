plugins {
    kotlin("jvm")
}

group = "org.schism"
version = "1.0-SNAPSHOT"

dependencies {
    implementation(project(":ffi"))

    api(project(":memory"))
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.2")
    api("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.3.5")
}

kotlin {
    explicitApi()
}
