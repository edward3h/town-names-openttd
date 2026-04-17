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
    inputs.property("bundledGrfs", bundledGrfs.map { "${it.first}=${it.second}" })
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
