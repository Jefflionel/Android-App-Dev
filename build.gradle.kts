import org.gradle.api.Plugin
import org.gradle.api.Project

plugins {
    id("com.android.application") version "8.1.4" apply false
    kotlin("android") version "2.1.10" apply false
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
