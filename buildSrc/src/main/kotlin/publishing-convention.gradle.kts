plugins {
    java
    id("maven-publish")
    id("signing")
    id("com.vanniktech.maven.publish")
}

group = rootProject.group
version = rootProject.version

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()
    pom {
        name = project.name
        description = project.description
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

signing {
    val signingKey = findProperty("signingKey").toString()
    val signingPassword = findProperty("signingPassword").toString()
    useInMemoryPgpKeys(signingKey, signingPassword)
}
