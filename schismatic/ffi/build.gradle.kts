plugins {
    kotlin("jvm")
}

group = "org.schism"
version = "1.0-SNAPSHOT"

dependencies {
    implementation(project(":math"))
    implementation(project(":memory"))
    implementation(kotlin("reflect"))
    implementation("org.ow2.asm:asm:9.3")
}

kotlin {
    explicitApi()
}
