plugins {
    id("com.diffplug.spotless") version "7.0.3" apply false
}

subprojects {
    repositories {
        mavenCentral()
    }

    apply(plugin = "com.diffplug.spotless")

    configure<com.diffplug.gradle.spotless.SpotlessExtension> {
        java {
            googleJavaFormat("1.25.2")
        }
    }
}
