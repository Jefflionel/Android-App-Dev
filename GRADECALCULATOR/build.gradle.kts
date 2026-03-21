plugins {
    kotlin("jvm") version "2.1.10"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation(kotlin("stdlib"))
    implementation("org.apache.poi:poi:5.2.3")
    implementation("org.apache.poi:poi-ooxml:5.2.3")
}

kotlin {
    // This sets the toolchain for Kotlin compilation AND aligns Java compilation
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(21)) // Use a stable version like 17, 21
    }
    // Or the shorter syntax:
    // jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}