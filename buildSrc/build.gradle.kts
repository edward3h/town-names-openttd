plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
}

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)  // buildSrc only needs to compile convention plugins, not produce Java 25 artefacts
    }
}

dependencies {
    implementation("com.diffplug.spotless:spotless-plugin-gradle:8.4.0")
    implementation("com.vanniktech:gradle-maven-publish-plugin:0.36.0")
}
