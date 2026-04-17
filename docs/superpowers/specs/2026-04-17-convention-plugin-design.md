# Convention Plugin Migration Design

## Context

The root `build.gradle.kts` uses a `subprojects` block to share Spotless configuration across the two subprojects (`town-names-core`, `town-names-bananas`). Both subprojects also contain large blocks of duplicated configuration: identical `group`/`version`, Java 25 toolchain, `maven-publish`/`signing` setup, Sonatype OSS repository, POM licence/developer/SCM content, and JUnit test dependencies.

The modern Gradle approach is to move shared configuration into convention plugins inside a `buildSrc` directory. This matches the pattern already used in the related `kiwiproc` project.

## Goal

Replace the `subprojects` block and the duplicated per-subproject configuration with two convention plugins, leaving each subproject build file containing only what is unique to that subproject.

## Files to Create

### `buildSrc/settings.gradle.kts`
```kotlin
rootProject.name = "buildSrc"
```
Required for Gradle 9 to avoid a deprecation warning.

### `buildSrc/build.gradle.kts`
```kotlin
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
    implementation("com.diffplug.spotless:spotless-plugin-gradle:7.2.1")
}
```

`kotlin-dsl` + `java-gradle-plugin` enable convention plugin development. Spotless is declared here as a dependency so the convention plugin can apply it. The Spotless version is owned here — this is why the root `build.gradle.kts` `plugins` block (which previously declared the Spotless version with `apply false`) can be removed entirely.

### `buildSrc/src/main/kotlin/java-convention.gradle.kts`
Responsibilities:
- Apply `java-library` and `com.diffplug.spotless`
- Configure `mavenCentral()` repository
- Set Java 25 toolchain
- Call `java { withJavadocJar(); withSourcesJar() }` (here rather than in `publishing-convention` since `java-library` is applied in this plugin)
- Add JUnit Jupiter test dependencies (`junit-jupiter:5.14.3`, `junit-platform-launcher`)
- Configure `useJUnitPlatform()` on the `test` task
- Configure Spotless with `palantirJavaFormat("2.90.0")`

### `buildSrc/src/main/kotlin/publishing-convention.gradle.kts`
Responsibilities:
- Apply `maven-publish` and `signing`
- Set `group = rootProject.group` and `version = rootProject.version` (requires root to declare both — see root file below)
- Create `mavenJava` `MavenPublication`:
  - `from(components["java"])` — wires compiled jar, sources jar, and javadoc jar into the publication (requires `java-convention` to be applied first in the subproject)
  - `artifactId` defaults to the project directory name (`town-names-core`, `town-names-bananas`) — no explicit override needed
  - Shared POM content: `url`, GPLv2+ licence block, developer block (`id = "edward3h"`, `email = "jaq@ethelred.org"`), SCM block (GitHub git/ssh/https)
  - `pom.name` is omitted and defaults to the `artifactId` — acceptable for Maven Central
  - `pom.description` is omitted here; each subproject sets it via `publications.named<MavenPublication>("mavenJava") { pom { description = "…" } }`
- Configure Sonatype OSS repository (snapshot vs release URL based on `version`, `ossrhUsername`/`ossrhPassword` Gradle properties)
- Configure signing:
  ```kotlin
  signing {
      val signingKey = System.getenv("SIGNING_SECRET_KEY")
      if (signingKey != null) {
          useInMemoryPgpKeys(
              System.getenv("SIGNING_KEY_ID"),
              signingKey,
              System.getenv("SIGNING_PASSWORD")
          )
      }
      sign(publishing.publications)  // signs all publications lazily — safe in convention plugins
  }
  ```
  Using `sign(publishing.publications)` (not `sign(publishing.publications["mavenJava"])`) avoids eager resolution of the publication container, which is required when signing is configured in a convention plugin.

## Files to Modify

### `build.gradle.kts` (root)
Remove the `plugins` block (Spotless version is now in `buildSrc`) and the `subprojects` block. The file becomes:
```kotlin
group = "red.ethel"
version = "0.1.0-SNAPSHOT"
```
Both `group` and `version` must be retained here — `publishing-convention` reads them via `rootProject.group` and `rootProject.version`.

### `town-names-core/build.gradle.kts`
```kotlin
plugins {
    id("java-convention")
    id("publishing-convention")
}

publishing {
    publications.named<MavenPublication>("mavenJava") {
        pom {
            description = "Generate random town names from OpenTTD NewGRF files"
        }
    }
}

// downloadBundledGrfs and generateTestGrf custom tasks remain here, unchanged
```

### `town-names-bananas/build.gradle.kts`
```kotlin
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
```

## What Each Subproject Retains

| Concern | Stays in subproject? |
|---|---|
| Unique dependencies (jackson, project ref) | Yes |
| POM `description` | Yes |
| Custom tasks (downloadBundledGrfs, generateTestGrf) | Yes (core only) |
| Everything else | Moved to convention plugins |

## Verification

1. `./gradlew build` — all subprojects compile and tests pass
2. `./gradlew spotlessCheck` — formatting check passes on both subprojects
3. `./gradlew publishToMavenLocal` — both subprojects publish locally with correct POM (licence, developer, SCM present; correct description per subproject; sources and javadoc jars present)
4. `./gradlew :town-names-core:tasks --all | grep -E "sign|publish"` and same for `town-names-bananas` — confirm `signMavenJavaPublication` and `publishMavenJavaPublicationToMavenCentralRepository` tasks are present
