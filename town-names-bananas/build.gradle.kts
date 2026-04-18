plugins {
    id("java-convention")
    id("publishing-convention")
}

description = "Download OpenTTD NewGRF town name files from the Bananas content server"

dependencies {
    api(project(":town-names-core"))
    implementation("com.fasterxml.jackson.core:jackson-databind:2.21.2")
}
