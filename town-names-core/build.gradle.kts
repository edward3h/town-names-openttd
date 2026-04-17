import java.net.URL

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
    testImplementation("org.junit.jupiter:junit-jupiter:5.12.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = "town-names-core"
            from(components["java"])
            pom {
                name = "town-names-core"
                description = "Generate random town names from OpenTTD NewGRF files"
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
    sign(publishing.publications["mavenJava"])
}

// Configuration for bundled GRFs to download at build time.
// Each entry: Pair(filename, downloadUrl)
// URLs are from the Bananas CDN; replace with real content after curation.
val bundledGrfs: List<Pair<String, String>> = listOf(
    // Example — REPLACE with real curated entries:
    // "uk-towns.grf" to "https://binaries.bananas.openttd.org/...",
)

val grfOutputDir = layout.buildDirectory.dir("bundled-grfs")

val downloadBundledGrfs by tasks.registering {
    outputs.dir(grfOutputDir)
    doLast {
        val dir = grfOutputDir.get().asFile
        dir.mkdirs()
        for (entry in bundledGrfs) {
            val filename = entry.first
            val url = entry.second
            val dest = dir.resolve(filename)
            if (!dest.exists()) {
                println("Downloading $filename from $url")
                @Suppress("DEPRECATION")
                val bytes = URL(url).readBytes()
                dest.writeBytes(bytes)
            }
        }
    }
}

sourceSets["main"].resources.srcDir(grfOutputDir)
tasks.named("processResources") { dependsOn(downloadBundledGrfs) }
