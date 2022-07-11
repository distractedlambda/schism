plugins {
    kotlin("jvm")
}

dependencies {
    api(project(":commons"))
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(19))
    }

    explicitApi()
}
