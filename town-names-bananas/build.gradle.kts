plugins {
    `java-library`
    `maven-publish`
    signing
}

group = "io.github.edward3h"
version = "0.1.0-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
    withJavadocJar()
    withSourcesJar()
}

dependencies {
    api(project(":town-names-core"))
    implementation("com.fasterxml.jackson.core:jackson-databind:2.18.3")
    testImplementation("org.junit.jupiter:junit-jupiter:5.12.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = "town-names-bananas"
            from(components["java"])
            pom {
                name = "town-names-bananas"
                description = "Download OpenTTD NewGRF town name files from the Bananas content server"
                url = "https://github.com/edward3h/town-names-openttd"
                licenses {
                    license {
                        name = "MIT License"
                        url = "https://opensource.org/licenses/MIT"
                    }
                }
                developers {
                    developer {
                        id = "edward3h"
                        email = "jaq@ethelred.org"
                    }
                }
                scm {
                    connection = "scm:git:git://github.com/edward3h/town-names-openttd.git"
                    developerConnection = "scm:git:ssh://github.com/edward3h/town-names-openttd.git"
                    url = "https://github.com/edward3h/town-names-openttd"
                }
            }
        }
    }
    repositories {
        maven {
            name = "MavenCentral"
            url = uri(
                if (version.toString().endsWith("SNAPSHOT"))
                    "https://s01.oss.sonatype.org/content/repositories/snapshots/"
                else
                    "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
            )
            credentials {
                username = providers.gradleProperty("ossrhUsername").orNull
                password = providers.gradleProperty("ossrhPassword").orNull
            }
        }
    }
}

signing {
    val signingKey = System.getenv("SIGNING_SECRET_KEY")
    if (signingKey != null) {
        useInMemoryPgpKeys(
            System.getenv("SIGNING_KEY_ID"),
            signingKey,
            System.getenv("SIGNING_PASSWORD")
        )
    }
    sign(publishing.publications["mavenJava"])
}
