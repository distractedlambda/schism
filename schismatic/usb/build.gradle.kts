plugins {
    kotlin("jvm")
}

group = "org.schism"
version = "1.0-SNAPSHOT"

dependencies {
    api(project(":commons"))
}

kotlin {
    explicitApi()
}
