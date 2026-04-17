# Convention Plugin Migration Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the root `subprojects` block and per-subproject duplicated configuration with two `buildSrc` convention plugins (`java-convention` and `publishing-convention`).

**Architecture:** Create a `buildSrc` included build with `kotlin-dsl` that hosts two precompiled script plugins. Each subproject applies both plugins and retains only its unique dependencies, description, and custom tasks.

**Tech Stack:** Gradle 9.4.1, Kotlin DSL, Spotless 7.2.1 (palantirJavaFormat), maven-publish, signing

**Spec:** `docs/superpowers/specs/2026-04-17-convention-plugin-design.md`

---

## File Map

| Action | Path | Responsibility |
|---|---|---|
| Create | `buildSrc/settings.gradle.kts` | Declares buildSrc root project name |
| Create | `buildSrc/build.gradle.kts` | kotlin-dsl + spotless dependency for convention plugins |
| Create | `buildSrc/src/main/kotlin/java-convention.gradle.kts` | Shared Java/test/formatting config |
| Create | `buildSrc/src/main/kotlin/publishing-convention.gradle.kts` | Shared publish/sign/POM config |
| Modify | `build.gradle.kts` | Remove plugins + subprojects blocks; keep group/version |
| Modify | `town-names-core/build.gradle.kts` | Apply convention plugins; keep custom tasks + description |
| Modify | `town-names-bananas/build.gradle.kts` | Apply convention plugins; keep unique deps + description |

---

## Chunk 1: Create buildSrc

### Task 0: Create feature branch

- [ ] **Step 1: Create and switch to branch**

```bash
git checkout -b refactor/convention-plugins
```

Expected: Switched to a new branch 'refactor/convention-plugins'

---

### Task 1: Create buildSrc scaffold

**Files:**
- Create: `buildSrc/settings.gradle.kts`
- Create: `buildSrc/build.gradle.kts`

- [ ] **Step 1: Create `buildSrc/settings.gradle.kts`**

```kotlin
rootProject.name = "buildSrc"
```

- [ ] **Step 2: Create `buildSrc/build.gradle.kts`**

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

Note: both `kotlin-dsl` and `java-gradle-plugin` are declared here — this matches the pattern used in the reference `kiwiproc` project's buildSrc.

---

### Task 2: Create `java-convention.gradle.kts`

**Files:**
- Create: `buildSrc/src/main/kotlin/java-convention.gradle.kts`

- [ ] **Step 1: Create the file**

```kotlin
plugins {
    `java-library`
    id("com.diffplug.spotless")
}

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
    withJavadocJar()
    withSourcesJar()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.14.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

spotless {
    java {
        palantirJavaFormat("2.90.0")
    }
}
```

- [ ] **Step 2: Verify buildSrc compiles (this is the first meaningful check — Kotlin sources now exist)**

Run: `./gradlew help`

Expected: BUILD SUCCESSFUL

---

### Task 3: Create `publishing-convention.gradle.kts`

**Files:**
- Create: `buildSrc/src/main/kotlin/publishing-convention.gradle.kts`

- [ ] **Step 1: Create the file**

```kotlin
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
```

- [ ] **Step 2: Verify buildSrc compiles cleanly with both plugins**

Run: `./gradlew help`

Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit buildSrc**

```bash
git add buildSrc/
git commit -m "build: add buildSrc with java-convention and publishing-convention plugins"
```

---

## Chunk 2: Migrate build files

### Task 4: Simplify root `build.gradle.kts`

**Files:**
- Modify: `build.gradle.kts`

- [ ] **Step 1: Replace root build file**

The entire file content becomes:

```kotlin
group = "red.ethel"
version = "0.1.0-SNAPSHOT"
```

The `plugins` block (which declared the Spotless version with `apply false`) and the `subprojects` block are both removed. Spotless version is now owned by `buildSrc/build.gradle.kts`. Both `group` and `version` must stay here — `publishing-convention` reads them via `rootProject.group` and `rootProject.version`.

- [ ] **Step 2: Verify the root build is valid**

Run: `./gradlew :tasks`

Expected: BUILD SUCCESSFUL. The subprojects still have their own inline `plugins` blocks at this point, so their tasks are unchanged — but this step only checks the root project.

---

### Task 5: Migrate `town-names-core/build.gradle.kts`

**Files:**
- Modify: `town-names-core/build.gradle.kts`

- [ ] **Step 1: Replace the file**

The top of the file changes to use convention plugins; the custom tasks section at the bottom is preserved unchanged. Note: `artifactId` is not set explicitly — it defaults to the project directory name `town-names-core`, which is the correct value.

```kotlin
import java.net.URL
import org.gradle.api.publish.maven.MavenPublication

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
tasks.named("sourcesJar") { dependsOn(downloadBundledGrfs) }

val generateTestGrf by tasks.registering {
    val outFile = layout.buildDirectory.file("test-resources/test-names.grf")
    outputs.file(outFile)
    doLast {
        val file = outFile.get().asFile
        file.parentFile.mkdirs()
        // Minimal Action 0F NewGRF: 1 part, 2 entries — "North" (50) | "South" (50)
        // Container v1: spriteLen = bodyLen + 1 (includes info byte)
        val text1 = "North".toByteArray(Charsets.UTF_8)
        val text2 = "South".toByteArray(Charsets.UTF_8)
        val bodyLen = 1 + 1 + 1 + 1 + 1 + text1.size + 1 + 1 + text2.size + 1
        val spriteLen = bodyLen + 1
        val bytes = mutableListOf<Byte>()
        bytes += (spriteLen and 0xFF).toByte()
        bytes += ((spriteLen shr 8) and 0xFF).toByte()
        bytes += 0xFF.toByte() // info: pseudo-sprite
        bytes += 0x0F.toByte() // action
        bytes += 0x00.toByte() // id
        bytes += 0x01.toByte() // num-parts
        bytes += 0x02.toByte() // count
        bytes += 50.toByte() // prob entry 1
        text1.forEach { bytes += it }
        bytes += 0x00.toByte() // NUL
        bytes += 50.toByte() // prob entry 2
        text2.forEach { bytes += it }
        bytes += 0x00.toByte() // NUL
        file.writeBytes(bytes.toByteArray())
    }
}
sourceSets["test"].resources.srcDir(layout.buildDirectory.dir("test-resources"))
tasks.named("processTestResources") { dependsOn(generateTestGrf) }
```

- [ ] **Step 2: Build and test town-names-core**

Run: `./gradlew :town-names-core:build`

Expected: BUILD SUCCESSFUL, tests pass

- [ ] **Step 3: Verify Spotless is active on town-names-core**

Run: `./gradlew :town-names-core:spotlessCheck`

Expected: BUILD SUCCESSFUL

---

### Task 6: Migrate `town-names-bananas/build.gradle.kts`

**Files:**
- Modify: `town-names-bananas/build.gradle.kts`

- [ ] **Step 1: Replace the file**

Note: `artifactId` defaults to the project directory name `town-names-bananas` — correct value, no explicit override needed.

```kotlin
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
```

- [ ] **Step 2: Build and test town-names-bananas**

Run: `./gradlew :town-names-bananas:build`

Expected: BUILD SUCCESSFUL, tests pass

---

### Task 7: Full verification and commit

- [ ] **Step 1: Full build**

Run: `./gradlew build`

Expected: BUILD SUCCESSFUL, all tests pass across both subprojects

- [ ] **Step 2: Spotless check across all subprojects**

Run: `./gradlew spotlessCheck`

Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Verify publishing tasks exist on both subprojects**

Run the following in a shell (pipe requires a terminal, not a Gradle console):

```bash
./gradlew :town-names-core:tasks --all | grep -E "sign|publish"
./gradlew :town-names-bananas:tasks --all | grep -E "sign|publish"
```

Expected output includes: `signMavenJavaPublication`, `publishMavenJavaPublicationToMavenCentralRepository`, `publishToMavenLocal`

- [ ] **Step 4: Verify local publish produces correct POM for both subprojects**

Run: `./gradlew publishToMavenLocal`

Expected: BUILD SUCCESSFUL

Check core POM (the glob matches the timestamped snapshot filename):

```bash
cat ~/.m2/repository/red/ethel/town-names-core/0.1.0-SNAPSHOT/*.pom
```

Confirm: `<name>town-names-core</name>`, licence block, developer block, SCM block, and `<description>Generate random town names from OpenTTD NewGRF files</description>` are present.

Check bananas POM:

```bash
cat ~/.m2/repository/red/ethel/town-names-bananas/0.1.0-SNAPSHOT/*.pom
```

Confirm: `<description>Download OpenTTD NewGRF town name files from the Bananas content server</description>` is present.

- [ ] **Step 5: Commit migrated build files**

```bash
git add build.gradle.kts town-names-core/build.gradle.kts town-names-bananas/build.gradle.kts
git commit -m "build: migrate subprojects block to convention plugins"
```
