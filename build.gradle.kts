plugins {
    id("com.diffplug.spotless") version "8.4.0" apply false
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
