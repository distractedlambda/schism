plugins {
    kotlin("jvm")
}

group = "org.schism"
version = "1.0-SNAPSHOT"

dependencies {
    implementation(project(":math"))
    implementation(project(":memory"))
}

kotlin {
    explicitApi()
}
