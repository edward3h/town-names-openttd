plugins {
    id("com.diffplug.spotless") version "7.2.1" apply false
}

subprojects {
    repositories {
        mavenCentral()
    }

    apply(plugin = "com.diffplug.spotless")

    configure<com.diffplug.gradle.spotless.SpotlessExtension> {
        java {
            palantirJavaFormat("2.90.0")
        }
    }
}
