import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.21"
}

group = "org.schism"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.2")
}

kotlin {
    jvmToolchain {
        (this as JavaToolchainSpec).languageVersion.set(JavaLanguageVersion.of(19))
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "18"
        freeCompilerArgs += listOf("-opt-in=kotlin.RequiresOptIn")
    }
}
