# Town Names OpenTTD — Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build and publish two Java 25 library modules — `town-names-core` (parse OpenTTD NewGRF files and generate random town names) and `town-names-bananas` (search and download NewGRF files from the OpenTTD Bananas content server with local caching).

**Architecture:** Multi-module Gradle (Kotlin DSL) project. `town-names-core` has zero runtime dependencies and exposes `TownNameGenerator` backed by a binary NewGRF parser and weighted-random engine. `town-names-bananas` depends on core and wraps the Bananas REST API with an atomic file-system cache keyed on `ContentId + version`.

**Tech Stack:** Java 25, Gradle 9.4.1 (Kotlin DSL), JUnit 5, Jackson Databind (bananas only), Spotless + Google Java Format, GitHub Actions CI, Maven Central via `maven-publish` + `signing`.

---

## Chunk 1: Project Scaffolding

### Task 1: Initialise git and Gradle wrapper

**Files:**
- Create: `.gitignore`
- Create: `gradlew`, `gradlew.bat`, `gradle/wrapper/gradle-wrapper.properties`

- [ ] **Step 1: Initialise git repository**

```bash
cd /home/edward/github/edward3h/town-names-openttd
git init
git checkout -b main
```

- [ ] **Step 2: Bootstrap Gradle wrapper**

```bash
gradle wrapper --gradle-version 9.4.1
```

Expected: `gradlew`, `gradlew.bat`, and `gradle/wrapper/` created.

- [ ] **Step 3: Create `.gitignore`**

```
.gradle/
build/
.idea/
*.iml
out/
*.class
```

- [ ] **Step 4: Commit**

```bash
git add .
git commit -m "chore: add gradle wrapper and gitignore"
```

---

### Task 2: Gradle multi-module configuration

**Files:**
- Create: `settings.gradle.kts`
- Create: `build.gradle.kts` (root)
- Create: `town-names-core/build.gradle.kts`
- Create: `town-names-bananas/build.gradle.kts`

- [ ] **Step 1: Create `settings.gradle.kts`**

```kotlin
rootProject.name = "town-names-openttd"

include("town-names-core", "town-names-bananas")
```

- [ ] **Step 2: Create root `build.gradle.kts`**

```kotlin
plugins {
    id("com.diffplug.spotless") version "7.0.3" apply false
}

subprojects {
    apply(plugin = "com.diffplug.spotless")

    configure<com.diffplug.gradle.spotless.SpotlessExtension> {
        java {
            googleJavaFormat("1.25.2")
        }
    }
}
```

- [ ] **Step 3: Create `town-names-core/build.gradle.kts`**

```kotlin
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
```

- [ ] **Step 4: Create `town-names-bananas/build.gradle.kts`**

```kotlin
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
    sign(publishing.publications["mavenJava"])
}
```

- [ ] **Step 5: Create source directory stubs**

```bash
mkdir -p town-names-core/src/main/java/io/github/edward3h/townnames/{grf,registry,engine}
mkdir -p town-names-core/src/main/resources/grf
mkdir -p town-names-core/src/test/java/io/github/edward3h/townnames/{grf,engine}
mkdir -p town-names-bananas/src/main/java/io/github/edward3h/townnames/bananas/{cache,http}
mkdir -p town-names-bananas/src/test/java/io/github/edward3h/townnames/bananas/{cache,http}
```

- [ ] **Step 6: Verify the build compiles cleanly**

```bash
./gradlew build
```

Expected: `BUILD SUCCESSFUL` (no source files yet, so just configuration is tested).

- [ ] **Step 7: Commit**

```bash
git add .
git commit -m "chore: configure multi-module Gradle build with publishing"
```

---

### Task 3: GitHub Actions CI

**Files:**
- Create: `.github/workflows/ci.yml`

- [ ] **Step 1: Create `.github/workflows/ci.yml`**

```yaml
name: CI

on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 25
        uses: actions/setup-java@v4
        with:
          java-version: '25'
          distribution: 'temurin'

      - name: Setup Gradle
        uses: gradle/actions/setup-gradle@v4

      - name: Build and test
        run: ./gradlew build
```

- [ ] **Step 2: Commit**

```bash
git add .github/
git commit -m "chore: add GitHub Actions CI workflow"
```

---

## Chunk 2: Core Module — NewGRF Parser

### Background: NewGRF Action 0F binary format

Before writing the parser, read the canonical specification:
- **Container format:** https://newgrf-specs.tt-wiki.net/wiki/GRFFile (covers the sprite container wrapping)
- **Action 0F:** https://newgrf-specs.tt-wiki.net/wiki/Action0F (town name definition)

**Summary of what to parse:**

A NewGRF file is a sequence of *sprites*. Each sprite has a length (2 bytes LE), an info byte, and a body. Sprites with info byte `0xFF` are *pseudo-sprites* (data, not graphics). Skip all other info bytes.

Within a pseudo-sprite, the first byte is the *action byte*. Skip everything except action `0x0F`.

**Action 0F layout** (all values little-endian):
```
<action=0x0F> <id:1> <num-parts:1>
  for each part (0..num-parts-1):
    <count:1>
    for each entry (0..count-1):
      <prob:1>           -- if bit7 set: this is a sub-part reference
      if bit7 clear:
        <text: null-terminated UTF-8 string>
      if bit7 set:
        <part-index:1>   -- index into this Action 0F's parts array
```

The `id` byte is the language/style identifier for this name set (e.g. `0x00` = English). A single GRF file may contain multiple Action 0F sprites with different IDs.

**Name generation algorithm:** To generate a name, pick the Action 0F with the desired `id` (or the first one found). For each part in order: pick one entry proportionally by probability weight. If the entry is a text, append it to the name. If it is a sub-part reference, recursively pick from that part. Concatenate all parts for the final name.

---

### Task 4: Data model for parsed GRF data

**Files:**
- Create: `town-names-core/src/main/java/io/github/edward3h/townnames/grf/NameEntry.java`
- Create: `town-names-core/src/main/java/io/github/edward3h/townnames/grf/NamePart.java`
- Create: `town-names-core/src/main/java/io/github/edward3h/townnames/grf/GrfData.java`

- [ ] **Step 1: Write the failing test for the data model**

```java
// town-names-core/src/test/java/io/github/edward3h/townnames/grf/GrfParserTest.java
package io.github.edward3h.townnames.grf;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import org.junit.jupiter.api.Test;

class GrfParserTest {

    @Test
    void grfDataHoldsPartsIndexedById() {
        var text = new NameEntry.Text(10, "Oakfield");
        var part = new NamePart(List.of(text));
        var data = new GrfData((byte) 0x00, List.of(part));

        assertEquals((byte) 0x00, data.id());
        assertEquals(1, data.parts().size());
        assertEquals("Oakfield", ((NameEntry.Text) data.parts().get(0).entries().get(0)).text());
    }
}
```

- [ ] **Step 2: Run test to confirm it fails**

```bash
./gradlew :town-names-core:test --tests "io.github.edward3h.townnames.grf.GrfParserTest"
```

Expected: compilation error — classes do not exist yet.

- [ ] **Step 3: Create `NameEntry.java`**

```java
package io.github.edward3h.townnames.grf;

/** A single entry within a NamePart: either a text string or a reference to another part. */
public sealed interface NameEntry permits NameEntry.Text, NameEntry.PartRef {

  /** Probability weight for this entry (1–127 for text; bit 7 set means PartRef). */
  int probability();

  /** A literal text string to append when this entry is selected. */
  record Text(int probability, String text) implements NameEntry {}

  /** A reference to another part (by index) to recurse into. */
  record PartRef(int probability, int partIndex) implements NameEntry {}
}
```

- [ ] **Step 4: Create `NamePart.java`**

```java
package io.github.edward3h.townnames.grf;

import java.util.List;

/**
 * One ordered group of weighted entries. To produce a token, select one entry
 * proportionally by probability weight.
 */
public record NamePart(List<NameEntry> entries) {}
```

- [ ] **Step 5: Create `GrfData.java`**

```java
package io.github.edward3h.townnames.grf;

import java.util.List;

/**
 * Parsed data from a single Action 0F sprite: one language/style id and its associated parts.
 * A .grf file may yield multiple GrfData instances (one per Action 0F id found).
 */
public record GrfData(byte id, List<NamePart> parts) {}
```

- [ ] **Step 6: Fix the test import and run**

Add `import java.util.List;` to `GrfParserTest.java`, then:

```bash
./gradlew :town-names-core:test --tests "io.github.edward3h.townnames.grf.GrfParserTest"
```

Expected: `PASSED`.

- [ ] **Step 7: Commit**

```bash
git add town-names-core/src/
git commit -m "feat(core): add GrfData, NamePart, NameEntry data model"
```

---

### Task 5: NewGRF binary parser

**Files:**
- Create: `town-names-core/src/main/java/io/github/edward3h/townnames/grf/GrfParser.java`
- Modify: `town-names-core/src/test/java/io/github/edward3h/townnames/grf/GrfParserTest.java`

- [ ] **Step 1: Write failing tests for the parser**

Append the following methods to the body of `GrfParserTest` (add the new imports at the top of the file alongside the existing ones):

New imports to add at the top:
```java
import java.io.ByteArrayInputStream;
import java.io.IOException;
```

New methods to add inside the class body:

```java
// Container v1 length field includes the info byte.
// Minimal valid NewGRF pseudo-sprite with one Action 0F:
//   id=0x00, 1 part, 2 entries: "North" (prob=50), "South" (prob=50)
private static byte[] minimalGrf() {
    byte[] text1 = "North".getBytes(java.nio.charset.StandardCharsets.UTF_8);
    byte[] text2 = "South".getBytes(java.nio.charset.StandardCharsets.UTF_8);
    // body = action(1) + id(1) + num-parts(1) + count(1) + prob(1) + text1 + NUL + prob(1) + text2 + NUL
    int bodyLen = 1 + 1 + 1 + 1 + 1 + text1.length + 1 + 1 + text2.length + 1;
    // spriteLen = bodyLen + 1 because Container v1 length includes the info byte
    int spriteLen = bodyLen + 1;
    byte[] sprite = new byte[2 + 1 + bodyLen];
    sprite[0] = (byte) (spriteLen & 0xFF);       // length low (includes info byte)
    sprite[1] = (byte) ((spriteLen >> 8) & 0xFF);// length high
    sprite[2] = (byte) 0xFF;                     // info: pseudo-sprite
    int i = 3;
    sprite[i++] = 0x0F;   // action
    sprite[i++] = 0x00;   // id
    sprite[i++] = 0x01;   // num-parts
    sprite[i++] = 0x02;   // count
    sprite[i++] = 50;     // prob entry 1
    for (byte b : text1) sprite[i++] = b;
    sprite[i++] = 0x00;   // NUL
    sprite[i++] = 50;     // prob entry 2
    for (byte b : text2) sprite[i++] = b;
    sprite[i] = 0x00;     // NUL
    return sprite;
}

@Test
void parsesAction0FFromInputStream() throws IOException {
    var results = GrfParser.parse(new ByteArrayInputStream(minimalGrf()));

    assertEquals(1, results.size());
    var data = results.get(0);
    assertEquals((byte) 0x00, data.id());
    assertEquals(1, data.parts().size());
    var entries = data.parts().get(0).entries();
    assertEquals(2, entries.size());
    assertEquals("North", ((NameEntry.Text) entries.get(0)).text());
    assertEquals("South", ((NameEntry.Text) entries.get(1)).text());
}

@Test
void skipsNonPseudoSprites() throws IOException {
    // A sprite with info != 0xFF should be skipped silently
    byte[] sprite = { 0x01, 0x00, 0x01, 0x00 }; // length=1, info=0x01, body=0x00
    var results = GrfParser.parse(new ByteArrayInputStream(sprite));
    assertTrue(results.isEmpty());
}

@Test
void skipsNonAction0FPseudoSprites() throws IOException {
    // info=0xFF but action=0x08 (not 0x0F) — should be skipped
    // spriteLen=2: info byte + 1 body byte (the action byte 0x08)
    byte[] sprite = { 0x02, 0x00, (byte) 0xFF, 0x08 };
    var results = GrfParser.parse(new ByteArrayInputStream(sprite));
    assertTrue(results.isEmpty());
}
```

- [ ] **Step 2: Run tests to confirm they fail**

```bash
./gradlew :town-names-core:test --tests "io.github.edward3h.townnames.grf.GrfParserTest"
```

Expected: compilation error — `GrfParser` does not exist.

- [ ] **Step 3: Create `GrfParser.java`**

```java
package io.github.edward3h.townnames.grf;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Parses NewGRF container v1 binary data and extracts all Action 0F
 * (town name generator) pseudo-sprites.
 *
 * <p>Reference: https://newgrf-specs.tt-wiki.net/wiki/Action0F
 */
public final class GrfParser {

  private GrfParser() {}

  /** Parse all Action 0F entries from a .grf file on disk. */
  public static List<GrfData> parse(Path path) throws IOException {
    try (var in = Files.newInputStream(path)) {
      return parse(in);
    }
  }

  /** Parse all Action 0F entries from a raw stream of NewGRF container v1 sprites. */
  public static List<GrfData> parse(InputStream in) throws IOException {
    var results = new ArrayList<GrfData>();
    byte[] lengthBuf = new byte[2];

    while (true) {
      int read = in.readNBytes(lengthBuf, 0, 2);
      if (read < 2) break; // EOF

      int spriteLen = (lengthBuf[0] & 0xFF) | ((lengthBuf[1] & 0xFF) << 8);
      if (spriteLen == 0) break; // terminator

      int info = in.read();
      if (info == -1) break;

      byte[] body = in.readNBytes(spriteLen - 1); // -1 for the info byte already read

      if (info != 0xFF) continue; // not a pseudo-sprite — skip
      if (body.length == 0 || body[0] != 0x0F) continue; // not Action 0F — skip

      GrfData data = parseAction0F(body);
      if (data != null) results.add(data);
    }

    return results;
  }

  private static GrfData parseAction0F(byte[] body) {
    if (body.length < 3) return null; // action(1) + id(1) + num-parts(1)

    byte id = body[1];
    int numParts = body[2] & 0xFF;
    int pos = 3;
    var parts = new ArrayList<NamePart>();

    for (int p = 0; p < numParts && pos < body.length; p++) {
      int count = body[pos++] & 0xFF;
      var entries = new ArrayList<NameEntry>();

      for (int e = 0; e < count && pos < body.length; e++) {
        int probByte = body[pos++] & 0xFF;
        boolean isRef = (probByte & 0x80) != 0;
        int prob = probByte & 0x7F;

        if (isRef) {
          if (pos >= body.length) break;
          int partIndex = body[pos++] & 0xFF;
          entries.add(new NameEntry.PartRef(prob, partIndex));
        } else {
          // read null-terminated UTF-8 string
          int start = pos;
          while (pos < body.length && body[pos] != 0x00) pos++;
          String text = new String(body, start, pos - start, StandardCharsets.UTF_8);
          if (pos < body.length) pos++; // consume NUL
          entries.add(new NameEntry.Text(prob, text));
        }
      }

      parts.add(new NamePart(List.copyOf(entries)));
    }

    return new GrfData(id, List.copyOf(parts));
  }
}
```

- [ ] **Step 4: Run tests**

```bash
./gradlew :town-names-core:test --tests "io.github.edward3h.townnames.grf.GrfParserTest"
```

Expected: all tests `PASSED`.

- [ ] **Step 5: Commit**

```bash
git add town-names-core/src/
git commit -m "feat(core): add NewGRF binary parser for Action 0F"
```

---

## Chunk 3: Core Module — Name Generation Engine, Registry and Public API

### Task 6: Name generation engine

**Files:**
- Create: `town-names-core/src/main/java/io/github/edward3h/townnames/engine/NameGenerationEngine.java`
- Create: `town-names-core/src/test/java/io/github/edward3h/townnames/engine/NameGenerationEngineTest.java`

- [ ] **Step 1: Write failing tests**

```java
// town-names-core/src/test/java/io/github/edward3h/townnames/engine/NameGenerationEngineTest.java
package io.github.edward3h.townnames.engine;

import static org.junit.jupiter.api.Assertions.*;
import io.github.edward3h.townnames.grf.GrfData;
import io.github.edward3h.townnames.grf.NameEntry;
import io.github.edward3h.townnames.grf.NamePart;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

class NameGenerationEngineTest {

    private static GrfData twoPartGrf() {
        // Part 0: "North" (50) | "South" (50)
        // Part 1: "field" (100)
        var part0 = new NamePart(List.of(
            new NameEntry.Text(50, "North"),
            new NameEntry.Text(50, "South")));
        var part1 = new NamePart(List.of(
            new NameEntry.Text(100, "field")));
        return new GrfData((byte) 0, List.of(part0, part1));
    }

    @Test
    void generatesConcatenatedName() {
        var engine = new NameGenerationEngine(List.of(twoPartGrf()));
        // With a fixed seed the output should be deterministic
        String name = engine.generate(new Random(42L));
        assertFalse(name.isEmpty());
        assertTrue(name.equals("Northfield") || name.equals("Southfield"),
            "Unexpected name: " + name);
    }

    @Test
    void deterministicWithSameSeed() {
        var engine = new NameGenerationEngine(List.of(twoPartGrf()));
        String a = engine.generate(new Random(99L));
        String b = engine.generate(new Random(99L));
        assertEquals(a, b);
    }

    @Test
    void bulkGenerateEquivalentToSequential() {
        var engine = new NameGenerationEngine(List.of(twoPartGrf()));
        var rng1 = new Random(7L);
        var rng2 = new Random(7L);
        List<String> bulk = engine.generate(rng1, 5);
        List<String> seq = new java.util.ArrayList<>();
        for (int i = 0; i < 5; i++) seq.add(engine.generate(rng2));
        assertEquals(seq, bulk);
    }

    @Test
    void partReferenceResolvesRecursively() {
        // Part 0: PartRef(100, 1) — delegates to Part 1, contributing "bury"
        // Part 1: "bury" (100)    — also processed directly as a top-level part, contributing "bury"
        // All parts are concatenated, so result = "bury" + "bury" = "burybury"
        // This verifies PartRef is resolved (if broken, Part 0 would contribute "" → result would be "bury")
        var part0 = new NamePart(List.of(new NameEntry.PartRef(100, 1)));
        var part1 = new NamePart(List.of(new NameEntry.Text(100, "bury")));
        var data = new GrfData((byte) 0, List.of(part0, part1));
        var engine = new NameGenerationEngine(List.of(data));
        assertEquals("burybury", engine.generate(new Random(0L)));
    }

    @Test
    void multipleSourcesEachGenerateFromOwnParts() {
        // Source A: part0="Alpha" | part1="ville" — only valid combination: "Alphaville"
        var sourceA = new GrfData((byte) 0, List.of(
            new NamePart(List.of(new NameEntry.Text(100, "Alpha"))),
            new NamePart(List.of(new NameEntry.Text(100, "ville")))));
        // Source B: part0="Beta" | part1="burg" — only valid combination: "Betaburg"
        var sourceB = new GrfData((byte) 0, List.of(
            new NamePart(List.of(new NameEntry.Text(100, "Beta"))),
            new NamePart(List.of(new NameEntry.Text(100, "burg")))));

        var engine = new NameGenerationEngine(List.of(sourceA, sourceB));

        // Over many seeds each result must be entirely from one source — never cross-mixed
        java.util.Set<String> generated = new java.util.HashSet<>();
        for (int i = 0; i < 100; i++) {
            generated.add(engine.generate(new Random(i)));
        }
        assertTrue(generated.stream().allMatch(n -> n.equals("Alphaville") || n.equals("Betaburg")),
            "Cross-source names generated: " + generated);
        assertTrue(generated.contains("Alphaville"), "Source A never selected");
        assertTrue(generated.contains("Betaburg"),   "Source B never selected");
    }
}
```

- [ ] **Step 2: Run to confirm failure**

```bash
./gradlew :town-names-core:test --tests "io.github.edward3h.townnames.engine.NameGenerationEngineTest"
```

Expected: compilation error.

- [ ] **Step 3: Create `NameGenerationEngine.java`**

```java
package io.github.edward3h.townnames.engine;

import io.github.edward3h.townnames.grf.GrfData;
import io.github.edward3h.townnames.grf.NameEntry;
import io.github.edward3h.townnames.grf.NamePart;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Generates town names by randomly selecting one GRF source and applying weighted random
 * selection across its parts. PartRef indices are local to the selected source.
 */
public final class NameGenerationEngine {

  private final List<GrfData> sources;

  public NameGenerationEngine(List<GrfData> sources) {
    if (sources.isEmpty()) throw new IllegalArgumentException("At least one GRF source is required");
    this.sources = List.copyOf(sources);
  }

  /** Generate a single name using the provided RNG. */
  public String generate(Random rng) {
    // Pick one source at random; PartRef indices are local to that source
    GrfData source = sources.get(rng.nextInt(sources.size()));
    List<NamePart> parts = source.parts();
    if (parts.isEmpty()) return "";

    var sb = new StringBuilder();
    for (NamePart part : parts) {
      sb.append(selectFromPart(part, parts, rng));
    }
    return sb.toString();
  }

  /** Generate {@code count} names. Equivalent to calling {@link #generate(Random)} count times. */
  public List<String> generate(Random rng, int count) {
    var names = new ArrayList<String>(count);
    for (int i = 0; i < count; i++) names.add(generate(rng));
    return List.copyOf(names);
  }

  private String selectFromPart(NamePart part, List<NamePart> localParts, Random rng) {
    int total = part.entries().stream().mapToInt(NameEntry::probability).sum();
    if (total == 0) return "";

    int pick = rng.nextInt(total);
    int cumulative = 0;
    for (NameEntry entry : part.entries()) {
      cumulative += entry.probability();
      if (pick < cumulative) {
        return switch (entry) {
          case NameEntry.Text t -> t.text();
          case NameEntry.PartRef r -> {
            if (r.partIndex() < localParts.size()) {
              yield selectFromPart(localParts.get(r.partIndex()), localParts, rng);
            }
            yield "";
          }
        };
      }
    }
    return "";
  }
}
```

- [ ] **Step 4: Run tests**

```bash
./gradlew :town-names-core:test --tests "io.github.edward3h.townnames.engine.NameGenerationEngineTest"
```

Expected: all `PASSED`.

- [ ] **Step 5: Commit**

```bash
git add town-names-core/src/
git commit -m "feat(core): add name generation engine with weighted random selection"
```

---

### Task 7: Bundled GRF registry

**Files:**
- Create: `town-names-core/src/main/java/io/github/edward3h/townnames/registry/BundledGrfRegistry.java`
- Create: `town-names-core/build.gradle.kts` (add `downloadBundledGrfs` task)

- [ ] **Step 0: Curate bundled GRFs** *(human gate — must be completed before Step 1)*

  Manually browse https://bananas.openttd.org, find 2–4 town name NewGRF packs whose licences permit redistribution (e.g. GPL, CC-BY, MIT). For each, record:
  - The display name (e.g. `uk-towns`)
  - The Bananas CDN download URL
  - The licence name and URL

  Write these into the `bundledGrfs` list in Step 1 below (replacing the placeholder comment).
  Also add an entry to `town-names-core/src/main/resources/grf/NOTICE.txt` for each file.

  **If no suitable GRFs are found**, leave `bundledGrfs` empty — the task still runs cleanly and produces an empty `grf/` resource directory.

- [ ] **Step 1: Add `downloadBundledGrfs` task to `town-names-core/build.gradle.kts`**

Add to the end of `town-names-core/build.gradle.kts`:

```kotlin
// Configuration for bundled GRFs to download at build time.
// Each entry: Pair(filename, downloadUrl)
// URLs are from the Bananas CDN; replace with real content after curation.
val bundledGrfs = listOf(
    // Example — REPLACE with real curated entries:
    // "uk-towns.grf" to "https://binaries.bananas.openttd.org/...",
)

val grfOutputDir = layout.buildDirectory.dir("bundled-grfs")

val downloadBundledGrfs by tasks.registering {
    outputs.dir(grfOutputDir)
    doLast {
        val dir = grfOutputDir.get().asFile
        dir.mkdirs()
        bundledGrfs.forEach { (filename, url) ->
            val dest = dir.resolve(filename)
            if (!dest.exists()) {
                println("Downloading $filename from $url")
                java.net.URI(url).toURL().openStream().use { input ->
                    dest.outputStream().use { output -> input.copyTo(output) }
                }
            }
        }
    }
}

sourceSets["main"].resources.srcDir(grfOutputDir)
tasks.named("processResources") { dependsOn(downloadBundledGrfs) }
```

- [ ] **Step 2: Write the failing test**

```java
// town-names-core/src/test/java/io/github/edward3h/townnames/BundledGrfRegistryTest.java
package io.github.edward3h.townnames;

import static org.junit.jupiter.api.Assertions.*;
import io.github.edward3h.townnames.registry.BundledGrfRegistry;
import org.junit.jupiter.api.Test;

class BundledGrfRegistryTest {

    @Test
    void registryReportsAvailableNames() {
        var registry = BundledGrfRegistry.getInstance();
        // When no GRFs are bundled yet (empty bundledGrfs list), the list is empty but non-null
        assertNotNull(registry.availableNames());
    }

    @Test
    void registryIsASingleton() {
        assertSame(BundledGrfRegistry.getInstance(), BundledGrfRegistry.getInstance());
    }
}
```

- [ ] **Step 3: Run to confirm failure**

```bash
./gradlew :town-names-core:test --tests "io.github.edward3h.townnames.BundledGrfRegistryTest"
```

Expected: compilation error — `BundledGrfRegistry` does not exist yet.

- [ ] **Step 4: Create `BundledGrfRegistry.java`**

```java
package io.github.edward3h.townnames.registry;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/**
 * Provides the set of NewGRF files bundled inside this JAR.
 *
 * <p>Populated at runtime by scanning the {@code /grf/} directory on the classpath.
 * The stem of each {@code .grf} filename (without extension) is the name exposed to callers.
 */
public final class BundledGrfRegistry {

  private static final BundledGrfRegistry INSTANCE = new BundledGrfRegistry();

  private final List<String> names;

  private BundledGrfRegistry() {
    names = List.copyOf(discoverNames());
  }

  public static BundledGrfRegistry getInstance() {
    return INSTANCE;
  }

  /** Returns the names of all bundled GRF files (without the .grf extension). */
  public List<String> availableNames() {
    return names;
  }

  /**
   * Opens an input stream for a bundled GRF by name.
   *
   * @throws IllegalArgumentException if the name is not in {@link #availableNames()}
   */
  public InputStream open(String name) {
    var stream = getClass().getResourceAsStream("/grf/" + name + ".grf");
    if (stream == null) {
      throw new IllegalArgumentException("No bundled GRF found for name: " + name);
    }
    return stream;
  }

  private static List<String> discoverNames() {
    var result = new ArrayList<String>();
    try {
      Enumeration<URL> resources =
          BundledGrfRegistry.class.getClassLoader().getResources("grf");
      while (resources.hasMoreElements()) {
        URL url = resources.nextElement();
        // For jar: URLs, list entries; for file: URLs, list directory
        if ("jar".equals(url.getProtocol())) {
          result.addAll(listFromJar(url));
        } else {
          result.addAll(listFromDirectory(url));
        }
      }
    } catch (IOException e) {
      throw new UncheckedIOException("Failed to scan bundled GRF resources", e);
    }
    Collections.sort(result);
    return result;
  }

  private static List<String> listFromJar(URL url) throws IOException {
    var names = new ArrayList<String>();
    String spec = url.toString();
    // e.g. jar:file:/path/to/jar.jar!/grf
    String jarPath = spec.substring("jar:".length(), spec.indexOf("!/"));
    try (var jar = new java.util.jar.JarFile(new java.io.File(new java.net.URI(jarPath)))) {
      jar.stream()
          .filter(e -> e.getName().startsWith("grf/") && e.getName().endsWith(".grf"))
          .map(e -> stem(e.getName().substring("grf/".length())))
          .forEach(names::add);
    } catch (java.net.URISyntaxException e) {
      throw new IOException("Malformed jar URL: " + jarPath, e);
    }
    return names;
  }

  private static List<String> listFromDirectory(URL url) throws IOException {
    var dir = new java.io.File(url.getPath());
    if (!dir.isDirectory()) return List.of();
    var files = dir.listFiles((d, name) -> name.endsWith(".grf"));
    if (files == null) return List.of();
    return java.util.Arrays.stream(files)
        .map(f -> stem(f.getName()))
        .toList();
  }

  private static String stem(String filename) {
    int dot = filename.lastIndexOf('.');
    return dot >= 0 ? filename.substring(0, dot) : filename;
  }
}
```

- [ ] **Step 5: Run tests**

```bash
./gradlew :town-names-core:test --tests "io.github.edward3h.townnames.BundledGrfRegistryTest"
```

Expected: `PASSED`.

- [ ] **Step 6: Commit**

```bash
git add town-names-core/
git commit -m "feat(core): add BundledGrfRegistry with classpath scanning"
```

---

### Task 8: Public API — TownNameGenerator

**Files:**
- Create: `town-names-core/src/main/java/io/github/edward3h/townnames/BuildResult.java`
- Create: `town-names-core/src/main/java/io/github/edward3h/townnames/TownNameGenerator.java`
- Create: `town-names-core/src/test/java/io/github/edward3h/townnames/TownNameGeneratorTest.java`

- [ ] **Step 1: Write failing tests**

```java
// town-names-core/src/test/java/io/github/edward3h/townnames/TownNameGeneratorTest.java
package io.github.edward3h.townnames;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

class TownNameGeneratorTest {

    @Test
    void builderWithNoBundledOrFileSourcesFails() {
        assertThrows(IllegalArgumentException.class, () ->
            TownNameGenerator.builder().build());
    }

    @Test
    void builderWithUnknownBundledNameFails() {
        assertThrows(IllegalArgumentException.class, () ->
            TownNameGenerator.builder().withBundled("no-such-grf").build());
    }

    @Test
    void generatesNonEmptyNameFromFile() throws Exception {
        // Use the test resource GRF bundled in the test classpath
        Path testGrf = Path.of(
            TownNameGeneratorTest.class.getResource("/test-names.grf").toURI());
        var result = TownNameGenerator.builder().withGrf(testGrf).build();
        assertTrue(result.skippedSources().isEmpty());
        String name = result.generator().generate();
        assertFalse(name.isBlank());
    }

    @Test
    void seededGeneratorIsDeterministic() throws Exception {
        Path testGrf = Path.of(
            TownNameGeneratorTest.class.getResource("/test-names.grf").toURI());
        var g1 = TownNameGenerator.builder().withGrf(testGrf).withSeed(42L).build().generator();
        var g2 = TownNameGenerator.builder().withGrf(testGrf).withSeed(42L).build().generator();
        assertEquals(g1.generate(), g2.generate());
    }

    @Test
    void badFileSourceIsSkipped() throws Exception {
        Path good = Path.of(
            TownNameGeneratorTest.class.getResource("/test-names.grf").toURI());
        Path bad = Path.of("/nonexistent/path.grf");
        var result = TownNameGenerator.builder().withGrf(good).withGrf(bad).build();
        assertEquals(List.of(bad), result.skippedSources());
        assertFalse(result.generator().generate().isBlank());
    }

    @Test
    void allBadSourcesThrows() {
        Path bad = Path.of("/nonexistent/path.grf");
        assertThrows(IllegalArgumentException.class, () ->
            TownNameGenerator.builder().withGrf(bad).build());
    }

    @Test
    void generateBulkEquivalentToSequential() throws Exception {
        Path testGrf = Path.of(
            TownNameGeneratorTest.class.getResource("/test-names.grf").toURI());
        var g = TownNameGenerator.builder().withGrf(testGrf).withSeed(7L).build().generator();
        var h = TownNameGenerator.builder().withGrf(testGrf).withSeed(7L).build().generator();
        assertEquals(g.generate(5), List.of(h.generate(), h.generate(), h.generate(), h.generate(), h.generate()));
    }
}
```

> **Note:** `test-names.grf` is a minimal synthetic GRF file used for testing. Create it in the next step.

- [ ] **Step 2: Create a minimal test GRF resource**

Create `town-names-core/src/test/resources/test-names.grf` programmatically using the same byte layout as the `minimalGrf()` helper in `GrfParserTest`. Write a small Java utility or use a Gradle task to generate it, or write the raw bytes directly. The simplest approach: add a `@BeforeAll` helper class that writes the file into a temp location, or — even simpler — use the existing `GrfParserTest.minimalGrf()` helper by moving it to a shared test utility.

Extract `minimalGrf()` from `GrfParserTest.java` into a new test utility class:

```java
// town-names-core/src/test/java/io/github/edward3h/townnames/TestGrfFactory.java
package io.github.edward3h.townnames;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class TestGrfFactory {

  private TestGrfFactory() {}

  /** Creates a minimal valid GRF file at the given path. */
  public static Path writeMinimalGrf(Path path) throws IOException {
    byte[] bytes = buildGrfBytes();
    Files.write(path, bytes);
    return path;
  }

  public static byte[] buildGrfBytes() {
    byte[] text1 = "North".getBytes(java.nio.charset.StandardCharsets.UTF_8);
    byte[] text2 = "South".getBytes(java.nio.charset.StandardCharsets.UTF_8);
    // body = action(1) + id(1) + num-parts(1) + count(1) + prob(1) + text1 + NUL + prob(1) + text2 + NUL
    int bodyLen = 1 + 1 + 1 + 1 + 1 + text1.length + 1 + 1 + text2.length + 1;
    // spriteLen includes the info byte (Container v1 format)
    int spriteLen = bodyLen + 1;
    byte[] sprite = new byte[2 + 1 + bodyLen];
    sprite[0] = (byte) (spriteLen & 0xFF);        // length low (includes info byte)
    sprite[1] = (byte) ((spriteLen >> 8) & 0xFF); // length high
    sprite[2] = (byte) 0xFF;
    int i = 3;
    sprite[i++] = 0x0F;
    sprite[i++] = 0x00;
    sprite[i++] = 0x01;
    sprite[i++] = 0x02;
    sprite[i++] = 50;
    for (byte b : text1) sprite[i++] = b;
    sprite[i++] = 0x00;
    sprite[i++] = 50;
    for (byte b : text2) sprite[i++] = b;
    sprite[i] = 0x00;
    return sprite;
  }
}
```

Update `GrfParserTest` to delegate its `minimalGrf()` helper to `TestGrfFactory.buildGrfBytes()`. Also add the import at the top of `GrfParserTest.java`:
```java
import io.github.edward3h.townnames.TestGrfFactory;
```

```java
private static byte[] minimalGrf() {
    return TestGrfFactory.buildGrfBytes();
}
```

> **Note on test GRF generation:** Do NOT use `JavaExec` to generate test resources — it creates a circular dependency (`testClasses` → `processTestResources` → `generateTestGrf` → `testClasses`). Instead, write the bytes directly from the Gradle DSL task below.

Add to `town-names-core/build.gradle.kts`:

```kotlin
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
        bytes += 0xFF.toByte()    // info: pseudo-sprite
        bytes += 0x0F.toByte()    // action
        bytes += 0x00.toByte()    // id
        bytes += 0x01.toByte()    // num-parts
        bytes += 0x02.toByte()    // count
        bytes += 50.toByte()      // prob entry 1
        text1.forEach { bytes += it }
        bytes += 0x00.toByte()    // NUL
        bytes += 50.toByte()      // prob entry 2
        text2.forEach { bytes += it }
        bytes += 0x00.toByte()    // NUL
        file.writeBytes(bytes.toByteArray())
    }
}
sourceSets["test"].resources.srcDir(layout.buildDirectory.dir("test-resources"))
tasks.named("processTestResources") { dependsOn(generateTestGrf) }
```

- [ ] **Step 3: Create `BuildResult.java`**

```java
package io.github.edward3h.townnames;

import java.nio.file.Path;
import java.util.List;

/**
 * The result of building a {@link TownNameGenerator}.
 *
 * @param generator     the configured generator, ready to use
 * @param skippedSources file paths that were unreadable or contained no valid Action 0F data;
 *                       empty when all sources loaded successfully
 */
public record BuildResult(TownNameGenerator generator, List<Path> skippedSources) {}
```

- [ ] **Step 4: Create `TownNameGenerator.java`**

```java
package io.github.edward3h.townnames;

import io.github.edward3h.townnames.engine.NameGenerationEngine;
import io.github.edward3h.townnames.grf.GrfData;
import io.github.edward3h.townnames.grf.GrfParser;
import io.github.edward3h.townnames.registry.BundledGrfRegistry;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * Generates random town names by combining one or more NewGRF sources.
 *
 * <p>Use {@link #builder()} to configure sources and an optional seed, then call
 * {@link Builder#build()} to get a {@link BuildResult} containing the generator and any
 * sources that could not be loaded.
 */
public final class TownNameGenerator {

  private final NameGenerationEngine engine;
  private final Random rng;

  private TownNameGenerator(NameGenerationEngine engine, Optional<Long> seed) {
    this.engine = engine;
    // Single Random instance created at construction time so sequential generate() calls
    // advance through the sequence rather than restarting from the same seed each time.
    this.rng = seed.map(Random::new).orElseGet(Random::new);
  }

  /** Returns a new builder. */
  public static Builder builder() {
    return new Builder();
  }

  /** Generate a single random town name. */
  public String generate() {
    return engine.generate(rng);
  }

  /**
   * Generate {@code count} random town names.
   * Equivalent to calling {@link #generate()} {@code count} times.
   *
   * @throws IllegalArgumentException if count <= 0
   */
  public List<String> generate(int count) {
    if (count <= 0) throw new IllegalArgumentException("count must be > 0, got: " + count);
    return engine.generate(rng, count);
  }

  /** Builder for {@link TownNameGenerator}. */
  public static final class Builder {

    private final List<String> bundledNames = new ArrayList<>();
    private final List<Path> filePaths = new ArrayList<>();
    private Optional<Long> seed = Optional.empty();

    private Builder() {}

    /** Add a bundled GRF by name (e.g. {@code "uk-towns"}). */
    public Builder withBundled(String name) {
      bundledNames.add(name);
      return this;
    }

    /** Add a GRF file from the file system. Unreadable files are skipped (see {@link BuildResult}). */
    public Builder withGrf(Path path) {
      filePaths.add(path);
      return this;
    }

    /** Fix the random seed for deterministic output. */
    public Builder withSeed(long seed) {
      this.seed = Optional.of(seed);
      return this;
    }

    /**
     * Build the generator.
     *
     * @throws IllegalArgumentException if no bundled name is unknown, or if all file sources
     *                                  are invalid and no bundled sources were specified
     */
    public BuildResult build() {
      var registry = BundledGrfRegistry.getInstance();

      // Validate bundled names eagerly — unknown name is always a developer error
      for (String name : bundledNames) {
        if (!registry.availableNames().contains(name)) {
          throw new IllegalArgumentException(
              "Unknown bundled GRF name '" + name + "'. Available: " + registry.availableNames());
        }
      }

      if (bundledNames.isEmpty() && filePaths.isEmpty()) {
        throw new IllegalArgumentException("At least one GRF source must be specified");
      }

      var allData = new ArrayList<GrfData>();
      var skipped = new ArrayList<Path>();

      // Load bundled GRFs
      for (String name : bundledNames) {
        try (var stream = registry.open(name)) {
          allData.addAll(GrfParser.parse(stream));
        } catch (IOException e) {
          // Bundled GRFs should never fail — treat as fatal
          throw new IllegalStateException("Failed to load bundled GRF '" + name + "'", e);
        }
      }

      // Load file GRFs; skip unreadable ones
      for (Path path : filePaths) {
        try {
          List<GrfData> data = GrfParser.parse(path);
          if (data.isEmpty()) {
            skipped.add(path); // readable but no Action 0F data
          } else {
            allData.addAll(data);
          }
        } catch (IOException e) {
          skipped.add(path);
        }
      }

      if (allData.isEmpty()) {
        throw new IllegalArgumentException(
            "No valid GRF data found. All file sources were invalid: " + filePaths);
      }

      var engine = new NameGenerationEngine(allData);
      return new BuildResult(new TownNameGenerator(engine, seed), List.copyOf(skipped));
    }
  }
}
```

- [ ] **Step 5: Run all core tests**

```bash
./gradlew :town-names-core:test
```

Expected: all tests `PASSED`. (Spotless may flag formatting — run `./gradlew :town-names-core:spotlessApply` if needed.)

- [ ] **Step 6: Commit**

```bash
git add town-names-core/src/
git commit -m "feat(core): add TownNameGenerator public API with builder and BuildResult"
```

---

## Chunk 4: Bananas Module

### Task 9: ContentId, BananasEntry, GrfDownloadResult value types

**Files:**
- Create: `town-names-bananas/src/main/java/io/github/edward3h/townnames/bananas/ContentId.java`
- Create: `town-names-bananas/src/main/java/io/github/edward3h/townnames/bananas/BananasEntry.java`
- Create: `town-names-bananas/src/main/java/io/github/edward3h/townnames/bananas/GrfDownloadResult.java`

- [ ] **Step 1: Write the failing test**

```java
// town-names-bananas/src/test/java/io/github/edward3h/townnames/bananas/BananasEntryTest.java
package io.github.edward3h.townnames.bananas;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class BananasEntryTest {

    @Test
    void contentIdEquality() {
        var a = new ContentId("ABCD1234", "abc123");
        var b = new ContentId("ABCD1234", "abc123");
        assertEquals(a, b);
    }

    @Test
    void bananasEntryHoldsMetadata() {
        var id = new ContentId("ABCD", "def0");
        var entry = new BananasEntry(id, "1.0", "UK Towns", null, "GPL v2", "Author");
        assertEquals("UK Towns", entry.name());
        assertNull(entry.description());
    }

    @Test
    void grfDownloadResultHoldsPath() {
        var result = new GrfDownloadResult(java.nio.file.Path.of("/tmp/test.grf"));
        assertEquals(java.nio.file.Path.of("/tmp/test.grf"), result.localPath());
    }
}
```

- [ ] **Step 2: Run to confirm failure**

```bash
./gradlew :town-names-bananas:test --tests "io.github.edward3h.townnames.bananas.BananasEntryTest"
```

Expected: compilation error.

- [ ] **Step 3: Create `ContentId.java`**

```java
package io.github.edward3h.townnames.bananas;

/**
 * Uniquely identifies a NewGRF file on the Bananas content server.
 *
 * @param grfid   the 8-character GRF identifier hex string
 * @param md5sum  the MD5 checksum hex string of the file content
 */
public record ContentId(String grfid, String md5sum) {}
```

- [ ] **Step 4: Create `BananasEntry.java`**

```java
package io.github.edward3h.townnames.bananas;

/**
 * Metadata for a single NewGRF file available on the Bananas content server.
 *
 * <p>Note: the field is named {@code licence} (British English); the Bananas REST API
 * uses {@code license} — the HTTP client maps the API field to this name.
 *
 * @param contentId    unique content identifier
 * @param version      version string (e.g. "1.0.0")
 * @param name         human-readable display name
 * @param description  optional description
 * @param licence      licence under which the GRF is distributed
 * @param author       original author name
 */
public record BananasEntry(
    ContentId contentId,
    String version,
    String name,
    String description,
    String licence,
    String author) {}
```

- [ ] **Step 5: Create `GrfDownloadResult.java`**

```java
package io.github.edward3h.townnames.bananas;

import java.nio.file.Path;

/**
 * The result of a successful {@link BananasClient#download} call.
 *
 * @param localPath the absolute path on disk to the downloaded (or cached) GRF file
 */
public record GrfDownloadResult(Path localPath) {}
```

- [ ] **Step 6: Run tests**

```bash
./gradlew :town-names-bananas:test --tests "io.github.edward3h.townnames.bananas.BananasEntryTest"
```

Expected: `PASSED`.

- [ ] **Step 7: Commit**

```bash
git add town-names-bananas/src/
git commit -m "feat(bananas): add ContentId, BananasEntry, GrfDownloadResult value types"
```

---

### Task 10: GRF cache

**Files:**
- Create: `town-names-bananas/src/main/java/io/github/edward3h/townnames/bananas/cache/GrfCache.java`
- Create: `town-names-bananas/src/test/java/io/github/edward3h/townnames/bananas/cache/GrfCacheTest.java`

**Cache file naming scheme:** `{grfid}_{md5sum}_{version}.grf`  
Example: `ABCD1234_abc123def456_1.0.0.grf`

- [ ] **Step 1: Write failing tests**

```java
// town-names-bananas/src/test/java/io/github/edward3h/townnames/bananas/cache/GrfCacheTest.java
package io.github.edward3h.townnames.bananas.cache;

import static org.junit.jupiter.api.Assertions.*;
import io.github.edward3h.townnames.bananas.BananasEntry;
import io.github.edward3h.townnames.bananas.ContentId;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class GrfCacheTest {

    private static final ContentId ID = new ContentId("ABCD1234", "abc123def456");
    private static final BananasEntry ENTRY =
        new BananasEntry(ID, "1.0.0", "Test GRF", null, "GPL v2", "Author");

    @Test
    void missCacheLookupReturnsEmpty(@TempDir Path cacheDir) {
        var cache = new GrfCache(cacheDir);
        assertTrue(cache.lookup(ENTRY).isEmpty());
    }

    @Test
    void hitAfterStore(@TempDir Path cacheDir) throws IOException {
        var cache = new GrfCache(cacheDir);
        byte[] content = {0x01, 0x02, 0x03};
        Path stored = cache.store(ENTRY, content);
        var hit = cache.lookup(ENTRY);
        assertTrue(hit.isPresent());
        assertEquals(stored, hit.get());
        assertArrayEquals(content, Files.readAllBytes(stored));
    }

    @Test
    void cacheKeyIsContentIdAndVersion(@TempDir Path cacheDir) throws IOException {
        var cache = new GrfCache(cacheDir);
        var otherEntry = new BananasEntry(
            new ContentId("ABCD1234", "abc123def456"), "2.0.0",
            "Same GRF, different version", null, "GPL v2", "Author");
        cache.store(ENTRY, new byte[]{0x01});
        // Different version = different cache key
        assertTrue(cache.lookup(otherEntry).isEmpty());
    }

    @Test
    void cacheFilenameIsCorrect(@TempDir Path cacheDir) throws IOException {
        var cache = new GrfCache(cacheDir);
        Path stored = cache.store(ENTRY, new byte[]{0x42});
        assertEquals("ABCD1234_abc123def456_1.0.0.grf", stored.getFileName().toString());
    }

    @Test
    void storeIsAtomic(@TempDir Path cacheDir) throws IOException {
        // Verify no .tmp file remains after store
        var cache = new GrfCache(cacheDir);
        cache.store(ENTRY, new byte[]{0x01});
        try (var stream = Files.list(cacheDir)) {
            assertTrue(stream.noneMatch(p -> p.toString().endsWith(".tmp")));
        }
    }

    @Test
    void populatesFromExistingDirectoryOnConstruction(@TempDir Path cacheDir) throws IOException {
        // Pre-populate the directory as if a previous run had downloaded a file
        String filename = "ABCD1234_abc123def456_1.0.0.grf";
        Files.write(cacheDir.resolve(filename), new byte[]{0x42});
        var cache = new GrfCache(cacheDir);
        assertTrue(cache.lookup(ENTRY).isPresent());
    }
}
```

- [ ] **Step 2: Run to confirm failure**

```bash
./gradlew :town-names-bananas:test --tests "io.github.edward3h.townnames.bananas.cache.GrfCacheTest"
```

Expected: compilation error.

- [ ] **Step 3: Create `GrfCache.java`**

```java
package io.github.edward3h.townnames.bananas.cache;

import io.github.edward3h.townnames.bananas.BananasEntry;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * File-system cache for downloaded GRF files.
 *
 * <p>Files are keyed by {@code {grfid}_{md5sum}_{version}.grf}.
 * On construction, the cache directory is scanned and any existing files are indexed.
 * {@link #store} writes atomically (write to temp, then move).
 * The cache is grow-only: existing entries are never removed or overwritten.
 */
public final class GrfCache {

  private final Path cacheDirectory;
  private final Map<String, Path> index;

  public GrfCache(Path cacheDirectory) {
    try {
      Files.createDirectories(cacheDirectory);
    } catch (IOException e) {
      throw new UncheckedIOException("Cannot create cache directory: " + cacheDirectory, e);
    }
    this.cacheDirectory = cacheDirectory;
    this.index = new ConcurrentHashMap<>(scan(cacheDirectory));
  }

  /** Returns the cached path for this entry, if present. */
  public Optional<Path> lookup(BananasEntry entry) {
    return Optional.ofNullable(index.get(cacheKey(entry)));
  }

  /**
   * Store bytes for an entry in the cache, atomically.
   *
   * @return the path where the file was stored
   */
  public Path store(BananasEntry entry, byte[] content) throws IOException {
    String key = cacheKey(entry);
    Path target = cacheDirectory.resolve(key);
    Path temp = cacheDirectory.resolve(key + ".tmp");
    Files.write(temp, content);
    Files.move(temp, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
    index.put(key, target);
    return target;
  }

  private static String cacheKey(BananasEntry entry) {
    return entry.contentId().grfid()
        + "_" + entry.contentId().md5sum()
        + "_" + entry.version()
        + ".grf";
  }

  private static Map<String, Path> scan(Path dir) {
    var result = new ConcurrentHashMap<String, Path>();
    try (var stream = Files.list(dir)) {
      stream
          .filter(p -> p.getFileName().toString().endsWith(".grf"))
          .forEach(p -> result.put(p.getFileName().toString(), p));
    } catch (IOException e) {
      throw new UncheckedIOException("Failed to scan cache directory: " + dir, e);
    }
    return result;
  }
}
```

- [ ] **Step 4: Run tests**

```bash
./gradlew :town-names-bananas:test --tests "io.github.edward3h.townnames.bananas.cache.GrfCacheTest"
```

Expected: all `PASSED`.

- [ ] **Step 5: Commit**

```bash
git add town-names-bananas/src/
git commit -m "feat(bananas): add GrfCache with atomic write and directory scan"
```

---

### Task 11: Bananas HTTP client

**Files:**
- Create: `town-names-bananas/src/main/java/io/github/edward3h/townnames/bananas/http/BananasHttpClient.java`
- Create: `town-names-bananas/src/test/java/io/github/edward3h/townnames/bananas/http/BananasHttpClientTest.java`

> **Before implementing:** Check the actual Bananas REST API endpoints by reading the OpenTTD Bananas API source at https://github.com/OpenTTD/bananas-api. The key endpoints are likely:
> - `GET https://bananas-api.openttd.org/package/newgrf?q={query}` — search
> - Download URLs come from the search response (field name TBD — check the API)
>
> Adjust the implementation to match actual field names in the JSON response.

- [ ] **Step 1: Write failing tests using a mock HTTP server**

```java
// town-names-bananas/src/test/java/io/github/edward3h/townnames/bananas/http/BananasHttpClientTest.java
package io.github.edward3h.townnames.bananas.http;

import static org.junit.jupiter.api.Assertions.*;
import com.sun.net.httpserver.HttpServer;
import io.github.edward3h.townnames.bananas.BananasEntry;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BananasHttpClientTest {

    // Minimal JSON matching the shape expected from the Bananas API.
    // IMPORTANT: verify field names against the actual API before finalising.
    private static final String SEARCH_RESPONSE = """
        [
          {
            "content-id": "ABCD1234",
            "md5sum": "abc123",
            "version": "1.0.0",
            "name": "UK Town Names",
            "description": "British place names",
            "license": "GPL v2",
            "author": "TestAuthor"
          }
        ]
        """;

    private static final byte[] FAKE_GRF = {0x01, 0x02, 0x03};

    private HttpServer mockServer;
    private int port;
    private BananasHttpClient client;

    @BeforeEach
    void setUp() throws IOException {
        mockServer = HttpServer.create(new InetSocketAddress(0), 0);
        port = mockServer.getAddress().getPort();

        mockServer.createContext("/package/newgrf", exchange -> {
            byte[] body = SEARCH_RESPONSE.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });

        mockServer.createContext("/download/ABCD1234", exchange -> {
            exchange.sendResponseHeaders(200, FAKE_GRF.length);
            exchange.getResponseBody().write(FAKE_GRF);
            exchange.close();
        });

        mockServer.start();
        client = new BananasHttpClient("http://localhost:" + port);
    }

    @AfterEach
    void tearDown() {
        mockServer.stop(0);
    }

    @Test
    void searchReturnsParsedEntries() throws IOException, InterruptedException {
        List<BananasEntry> results = client.search("uk");
        assertEquals(1, results.size());
        assertEquals("UK Town Names", results.get(0).name());
        assertEquals("GPL v2", results.get(0).licence());
    }

    @Test
    void downloadReturnsByteArray() throws IOException, InterruptedException {
        BananasEntry entry = client.search("uk").get(0);
        byte[] bytes = client.download(entry);
        assertArrayEquals(FAKE_GRF, bytes);
    }
}
```

- [ ] **Step 2: Run to confirm failure**

```bash
./gradlew :town-names-bananas:test --tests "io.github.edward3h.townnames.bananas.http.BananasHttpClientTest"
```

Expected: compilation error.

- [ ] **Step 3: Create `BananasHttpClient.java`**

```java
package io.github.edward3h.townnames.bananas.http;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.edward3h.townnames.bananas.BananasEntry;
import io.github.edward3h.townnames.bananas.ContentId;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * HTTP client for the OpenTTD Bananas REST API.
 *
 * <p>Filters results to NewGRF town-name content only.
 * Network errors propagate as {@link IOException} or {@link InterruptedException}.
 */
public final class BananasHttpClient {

  private static final String DEFAULT_BASE_URL = "https://bananas-api.openttd.org";

  private final String baseUrl;
  private final HttpClient http;
  private final ObjectMapper mapper;

  /** Create a client against the production Bananas API. */
  public BananasHttpClient() {
    this(DEFAULT_BASE_URL);
  }

  /** Create a client against a custom base URL (used in tests). */
  public BananasHttpClient(String baseUrl) {
    this.baseUrl = baseUrl;
    this.http = HttpClient.newHttpClient();
    this.mapper = new ObjectMapper();
  }

  /**
   * Search for NewGRF town-name packages matching the query.
   * An empty query returns all available town-name GRFs.
   */
  public List<BananasEntry> search(String query) throws IOException, InterruptedException {
    String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8);
    // NOTE: verify the actual Bananas API path and query parameter name
    var request = HttpRequest.newBuilder()
        .uri(URI.create(baseUrl + "/package/newgrf?q=" + encoded))
        .GET()
        .build();

    var response = http.send(request, HttpResponse.BodyHandlers.ofString());
    if (response.statusCode() != 200) {
      throw new IOException("Bananas search failed with status " + response.statusCode());
    }

    JsonNode root = mapper.readTree(response.body());
    var results = new ArrayList<BananasEntry>();
    for (JsonNode node : root) {
      results.add(parseEntry(node));
    }
    return List.copyOf(results);
  }

  /**
   * Download the binary content of a NewGRF file.
   * Returns the raw bytes; the caller is responsible for caching.
   *
   * <p>NOTE: The actual download URL structure must be verified against the Bananas API docs.
   */
  public byte[] download(BananasEntry entry) throws IOException, InterruptedException {
    // NOTE: verify the actual download endpoint path from the API response or docs
    String url = baseUrl + "/download/" + entry.contentId().grfid();
    var request = HttpRequest.newBuilder()
        .uri(URI.create(url))
        .GET()
        .build();

    var response = http.send(request, HttpResponse.BodyHandlers.ofByteArray());
    if (response.statusCode() != 200) {
      throw new IOException("Bananas download failed with status " + response.statusCode());
    }
    return response.body();
  }

  private static BananasEntry parseEntry(JsonNode node) {
    // NOTE: verify JSON field names against the actual Bananas API response
    var id = new ContentId(
        node.path("content-id").asText(),
        node.path("md5sum").asText());
    return new BananasEntry(
        id,
        node.path("version").asText(),
        node.path("name").asText(),
        node.has("description") && !node.get("description").isNull()
            ? node.get("description").asText() : null,
        node.path("license").asText(), // API uses "license" (US), we store as "licence"
        node.path("author").asText());
  }
}
```

- [ ] **Step 4: Run tests**

```bash
./gradlew :town-names-bananas:test --tests "io.github.edward3h.townnames.bananas.http.BananasHttpClientTest"
```

Expected: all `PASSED`.

- [ ] **Step 5: Commit**

```bash
git add town-names-bananas/src/
git commit -m "feat(bananas): add BananasHttpClient for search and download"
```

---

### Task 12: BananasClient public API

**Files:**
- Create: `town-names-bananas/src/main/java/io/github/edward3h/townnames/bananas/BananasClient.java`
- Create: `town-names-bananas/src/test/java/io/github/edward3h/townnames/bananas/BananasClientTest.java`

- [ ] **Step 1: Write failing tests**

```java
// town-names-bananas/src/test/java/io/github/edward3h/townnames/bananas/BananasClientTest.java
package io.github.edward3h.townnames.bananas;

import static org.junit.jupiter.api.Assertions.*;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class BananasClientTest {

    private static final String SEARCH_RESPONSE = """
        [{"content-id":"ABCD1234","md5sum":"abc123","version":"1.0.0",
          "name":"UK Town Names","license":"GPL v2","author":"TestAuthor"}]
        """;
    private static final byte[] FAKE_GRF = {0x01, 0x02, 0x03};

    private HttpServer mockServer;
    private int port;

    @BeforeEach
    void setUp() throws IOException {
        mockServer = HttpServer.create(new InetSocketAddress(0), 0);
        port = mockServer.getAddress().getPort();
        mockServer.createContext("/package/newgrf", exchange -> {
            byte[] body = SEARCH_RESPONSE.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        mockServer.createContext("/download/ABCD1234", exchange -> {
            exchange.sendResponseHeaders(200, FAKE_GRF.length);
            exchange.getResponseBody().write(FAKE_GRF);
            exchange.close();
        });
        mockServer.start();
    }

    @AfterEach
    void tearDown() { mockServer.stop(0); }

    @Test
    void searchDelegatesToApi(@TempDir Path cacheDir) throws Exception {
        var client = BananasClient.builder()
            .cacheDir(cacheDir)
            .baseUrl("http://localhost:" + port)
            .build();
        List<BananasEntry> results = client.search("uk");
        assertEquals(1, results.size());
        assertEquals("UK Town Names", results.get(0).name());
    }

    @Test
    void downloadCachesOnFirstCall(@TempDir Path cacheDir) throws Exception {
        var client = BananasClient.builder()
            .cacheDir(cacheDir)
            .baseUrl("http://localhost:" + port)
            .build();
        BananasEntry entry = client.search("").get(0);
        GrfDownloadResult result = client.download(entry);
        assertTrue(Files.exists(result.localPath()));
        assertArrayEquals(FAKE_GRF, Files.readAllBytes(result.localPath()));
    }

    @Test
    void downloadReturnsFromCacheOnSecondCall(@TempDir Path cacheDir) throws Exception {
        var client = BananasClient.builder()
            .cacheDir(cacheDir)
            .baseUrl("http://localhost:" + port)
            .build();
        BananasEntry entry = client.search("").get(0);
        GrfDownloadResult first = client.download(entry);
        // Stop mock server — second download must not make a network call
        mockServer.stop(0);
        GrfDownloadResult second = client.download(entry);
        assertEquals(first.localPath(), second.localPath());
    }

    @Test
    void defaultCacheDirIsTildeCache() {
        // This test creates ~/.cache/town-names-openttd as a side effect because GrfCache
        // calls Files.createDirectories eagerly. This is safe and idempotent on all standard
        // CI runners (GitHub Actions ubuntu-latest has a writable home directory).
        var client = BananasClient.builder().build();
        Path expected = Path.of(System.getProperty("user.home"), ".cache", "town-names-openttd");
        assertEquals(expected, client.cacheDirectory());
    }
}
```

- [ ] **Step 2: Run to confirm failure**

```bash
./gradlew :town-names-bananas:test --tests "io.github.edward3h.townnames.bananas.BananasClientTest"
```

Expected: compilation error.

- [ ] **Step 3: Create `BananasClient.java`**

```java
package io.github.edward3h.townnames.bananas;

import io.github.edward3h.townnames.bananas.cache.GrfCache;
import io.github.edward3h.townnames.bananas.http.BananasHttpClient;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * Downloads and caches NewGRF town-name files from the OpenTTD Bananas content server.
 *
 * <p>Use {@link #builder()} to configure the cache directory and optional custom base URL.
 *
 * <p>Thread safety: not thread-safe. Callers requiring concurrent access must synchronise
 * externally.
 */
public final class BananasClient {

  private static final Path DEFAULT_CACHE_DIR =
      Path.of(System.getProperty("user.home"), ".cache", "town-names-openttd");

  private final GrfCache cache;
  private final BananasHttpClient http;
  private final Path cacheDirectory;

  private BananasClient(Path cacheDirectory, String baseUrl) {
    this.cacheDirectory = cacheDirectory;
    this.cache = new GrfCache(cacheDirectory);
    this.http = new BananasHttpClient(baseUrl);
  }

  /** Returns a new builder. */
  public static Builder builder() {
    return new Builder();
  }

  /** The resolved cache directory path (tilde already expanded). */
  public Path cacheDirectory() {
    return cacheDirectory;
  }

  /**
   * Search for NewGRF town-name packages matching the query.
   * An empty query returns all available town-name GRFs.
   *
   * @throws IOException          on network or parse error
   * @throws InterruptedException if the thread is interrupted while waiting
   */
  public List<BananasEntry> search(String query) throws IOException, InterruptedException {
    return http.search(query);
  }

  /**
   * Download a GRF file. Returns immediately from the local cache if already present;
   * otherwise fetches from the Bananas server and caches the result.
   *
   * @throws IOException          on network or I/O error
   * @throws InterruptedException if interrupted while downloading
   */
  public GrfDownloadResult download(BananasEntry entry) throws IOException, InterruptedException {
    Optional<Path> cached = cache.lookup(entry);
    if (cached.isPresent()) {
      return new GrfDownloadResult(cached.get());
    }
    byte[] bytes = http.download(entry);
    Path stored = cache.store(entry, bytes);
    return new GrfDownloadResult(stored);
  }

  /** Builder for {@link BananasClient}. */
  public static final class Builder {

    private Path cacheDir = DEFAULT_CACHE_DIR;
    private String baseUrl = "https://bananas-api.openttd.org";

    private Builder() {}

    /** Set a custom cache directory (tilde is NOT expanded by this method; pass an absolute path). */
    public Builder cacheDir(Path cacheDir) {
      this.cacheDir = cacheDir;
      return this;
    }

    /** Override the Bananas API base URL (used in tests). */
    public Builder baseUrl(String baseUrl) {
      this.baseUrl = baseUrl;
      return this;
    }

    public BananasClient build() {
      return new BananasClient(cacheDir, baseUrl);
    }
  }
}
```

- [ ] **Step 4: Run all bananas tests**

```bash
./gradlew :town-names-bananas:test
```

Expected: all `PASSED`.

- [ ] **Step 5: Run full build**

```bash
./gradlew build
```

Expected: `BUILD SUCCESSFUL`. Run `./gradlew spotlessApply` first if formatting violations are reported.

- [ ] **Step 6: Commit**

```bash
git add town-names-bananas/src/
git commit -m "feat(bananas): add BananasClient public API"
```

---

## Chunk 5: Attribution and Publishing

### Task 13: NOTICE file for bundled GRFs

**Files:**
- Create: `town-names-core/src/main/resources/NOTICE.txt`

> This step requires the bundled GRF curation (from Task 7) to be completed first.
> For each bundled GRF, record: source URL, content ID, version, author, and licence.

- [ ] **Step 1: Create `NOTICE.txt`**

```
town-names-openttd — Third-Party Notices
=========================================

This library bundles NewGRF files sourced from the OpenTTD Bananas
content server (https://bananas.openttd.org). Each file is used and
redistributed in accordance with its stated licence.

No NewGRF files are bundled in this version.
Once curation is complete (see Task 7 note), each entry will be listed here:

  Name:       (display name)
  Content ID: (grfid)/(md5sum)
  Version:    (version string)
  Author:     (author name)
  Licence:    (licence name)
  Source:     https://bananas.openttd.org/package/newgrf/(grfid)
```

- [ ] **Step 2: Commit**

```bash
git add town-names-core/src/main/resources/NOTICE.txt
git commit -m "chore: add NOTICE.txt placeholder for bundled GRF attribution"
```

---

### Task 14: Maven Central publishing verification

**Files:**
- Modify: `~/.gradle/gradle.properties` (user's local machine — not committed)

- [ ] **Step 1: Verify GPG signing is set up**

Ensure `~/.gradle/gradle.properties` contains:
```properties
signing.keyId=<last 8 chars of GPG key>
signing.password=<passphrase>
signing.secretKeyRingFile=/home/edward/.gnupg/secring.gpg

ossrhUsername=<Sonatype OSSRH username>
ossrhPassword=<Sonatype OSSRH password>
```

- [ ] **Step 2: Publish a snapshot to verify credentials** *(manual human step — skip in automated runs)*

> This step requires live Sonatype OSSRH credentials and a GPG key. Run it manually after the credentials in Step 1 are in place. An automated agent should skip this step.

```bash
./gradlew publishMavenJavaPublicationToMavenCentralRepository
```

Expected: uploads to `s01.oss.sonatype.org` snapshots without error.

- [ ] **Step 3: Add a GitHub Actions release workflow**

Create `.github/workflows/release.yml`:

```yaml
name: Release

on:
  push:
    tags:
      - 'v*'

jobs:
  release:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 25
        uses: actions/setup-java@v4
        with:
          java-version: '25'
          distribution: 'temurin'

      - name: Setup Gradle
        uses: gradle/actions/setup-gradle@v4

      - name: Publish to Maven Central
        run: ./gradlew publish
        env:
          OSSRH_USERNAME: ${{ secrets.OSSRH_USERNAME }}
          OSSRH_PASSWORD: ${{ secrets.OSSRH_PASSWORD }}
          SIGNING_KEY_ID: ${{ secrets.SIGNING_KEY_ID }}
          SIGNING_PASSWORD: ${{ secrets.SIGNING_PASSWORD }}
          SIGNING_SECRET_KEY: ${{ secrets.SIGNING_SECRET_KEY }}
```

Update `build.gradle.kts` files to read signing credentials from environment variables when present (for CI):

Add to each module's `build.gradle.kts`:
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
    sign(publishing.publications["mavenJava"])
}
```

- [ ] **Step 4: Commit**

```bash
git add .github/workflows/release.yml town-names-core/build.gradle.kts town-names-bananas/build.gradle.kts
git commit -m "chore: add release workflow and in-memory signing for CI"
```

---

### Task 15: Final integration smoke test

- [ ] **Step 1: Run the complete build with all checks**

```bash
./gradlew clean build spotlessCheck
```

Expected: `BUILD SUCCESSFUL`, no formatting violations.

- [ ] **Step 2: Verify JAR contents include NOTICE (and bundled GRFs once curated)**

```bash
jar tf town-names-core/build/libs/town-names-core-0.1.0-SNAPSHOT.jar | grep -E "grf/|NOTICE"
```

Expected: `NOTICE.txt` is present. The `grf/` entries will only appear once bundled GRFs have been curated (Task 7 note). If no GRFs are bundled yet, only `NOTICE.txt` will appear — that is correct for this stage.

- [ ] **Step 3: Verify Javadoc builds cleanly**

```bash
./gradlew javadoc
```

Expected: no errors. Warnings about missing `@param`/`@return` on records are acceptable.

- [ ] **Step 4: Final commit**

```bash
git add .
git commit -m "chore: verified complete build — ready for v0.1.0"
```
