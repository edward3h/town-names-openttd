import org.gradle.api.publish.maven.MavenPublication

plugins {
    `maven-publish`
    signing
}

group = rootProject.group
version = rootProject.version

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            pom {
                name = project.name  // defaults to artifactId (project directory name); required by Maven Central
                url = "https://github.com/edward3h/town-names-openttd"
                licenses {
                    license {
                        name = "GNU General Public License v2.0 or later"
                        url = "https://www.gnu.org/licenses/gpl-2.0.html"
                    }
                }
                developers {
                    developer {
                        id = "edward3h"
                        name = "Edward Harman"
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
    // sign(publishing.publications) signs all publications lazily — safe in convention plugins.
    // The original subproject files used sign(publishing.publications["mavenJava"]) which
    // resolves the publication eagerly and is not safe when signing is in a convention plugin.
    sign(publishing.publications)
}
