import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("java-library")
    id("com.gradleup.shadow") version "9.6.1"
    id("io.github.ben-manes.versions") version "0.67.0"
}

java { toolchain.languageVersion.set(JavaLanguageVersion.of(25)) }

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://maven.enginehub.org/repo/")
}

dependencies {
    // --- Provided by Paper / Server ---
    compileOnly("io.papermc.paper:paper-api:26.3.build.+")
    compileOnly("com.fastasyncworldedit:FastAsyncWorldEdit-Core")
    compileOnly("com.sk89q.worldedit:worldedit-bukkit:7.4.6")

    // --- Bundled & Relocated Dependencies ---
    implementation("com.zaxxer:HikariCP:7.1.0")
    implementation("com.google.code.gson:gson:2.15.0")
    implementation("org.incendo:cloud-paper:2.0.1")
    implementation("org.incendo:cloud-annotations:2.1.0")

    // --- Bundled Dependencies ---
    implementation(platform("com.intellectualsites.bom:bom-newest:1.56"))
}

tasks {
    jar {
        enabled = false
    }

    val extraJarEnv: String? = System.getenv("EXTRA_JAR")
    val targetOutputDirectory: File = if (!extraJarEnv.isNullOrEmpty()) {
        file(extraJarEnv)
    } else {
        layout.projectDirectory.dir("OutputJarFile").asFile
    }

    processResources {
        val props = mapOf(
            "version" to project.version,
            "description" to project.description
        )
        inputs.properties(props)
        filteringCharset = "UTF-8"
        filesMatching("plugin.yml") {
            expand(props)
        }
    }

    build { dependsOn(shadowJar) }
    named<ShadowJar>("shadowJar") {
        from(project.rootDir) {
            include("LICENSE*", "THIRD-PARTY-LICENSES*", "README*")
        }
        archiveClassifier.set("")
        destinationDirectory.set(targetOutputDirectory)

        // Relocate bundled dependencies.
        relocate("com.zaxxer.hikari", "io.github.khristophere.topherstools.libs.hikari")
        relocate("com.google.gson", "io.github.khristophere.topherstools.libs.gson")
        relocate("org.incendo", "io.github.khristophere.topherstools.libs.incendo")

        finalizedBy("ifAlternateLocationCopyToOutputJarFile")
    }
    register<Copy>("ifAlternateLocationCopyToOutputJarFile") {
        description = ""
        if (!extraJarEnv.isNullOrEmpty()) {
            val shadowJarTask = named<ShadowJar>("shadowJar")
            dependsOn(shadowJarTask)
            from(shadowJarTask.flatMap { it.archiveFile })
            into(layout.projectDirectory.dir("OutputJarFile"))
        }
    }

}