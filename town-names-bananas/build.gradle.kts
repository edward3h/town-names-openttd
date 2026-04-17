import org.gradle.api.publish.maven.MavenPublication

plugins {
    id("java-convention")
    id("publishing-convention")
}

dependencies {
    api(project(":town-names-core"))
    implementation("com.fasterxml.jackson.core:jackson-databind:2.21.2")
}

publishing {
    publications.named<MavenPublication>("mavenJava") {
        pom {
            description = "Download OpenTTD NewGRF town name files from the Bananas content server"
        }
    }
}
