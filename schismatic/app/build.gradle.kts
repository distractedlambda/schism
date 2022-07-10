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
    implementation(compose.desktop.currentOs)
}

kotlin {
    explicitApi()
}

compose.desktop {
    application {
        mainClass = "org.schism.schismatic.Schismatic"
    }
}
