import de.undercouch.gradle.tasks.download.Download

plugins {
    kotlin("jvm")
    id("de.undercouch.download")
}

dependencies {
    implementation(project(":bytes"))
    api(project(":concurrent"))
}

val downloadUsbIds by tasks.registering(Download::class) {
    src("http://www.linux-usb.org/usb.ids")
    dest("$buildDir/usb.ids")
    onlyIfModified(true)
}

tasks.processResources {
    dependsOn(downloadUsbIds)

    from(downloadUsbIds.get().outputFiles.single()) {
        into("org/schism/usb/")
    }
}
