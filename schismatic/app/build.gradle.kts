import org.jetbrains.compose.compose

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
}

repositories {
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

dependencies {
    implementation(project(":usb"))
    implementation(compose.desktop.currentOs)
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(19))
    }
}

compose.desktop {
    application {
        mainClass = "org.schism.schismatic.Schismatic"
    }
}
