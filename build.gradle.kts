import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("java-library")
    id("com.gradleup.shadow") version "8.3.5"
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven ("https://maven.enginehub.org/repo/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:26.3.build.+")

    implementation(platform("com.intellectualsites.bom:bom-newest:1.56"))
    compileOnly("com.fastasyncworldedit:FastAsyncWorldEdit-Core")
    compileOnly("com.sk89q.worldedit:worldedit-bukkit:7.3.0")

    implementation("com.google.code.gson:gson:2.11.0")

    implementation("org.incendo:cloud-paper:2.0.1")
    implementation("org.incendo:cloud-annotations:2.0.1")

    implementation("com.mysql:mysql-connector-j:26.10.0")

    constraints {
        implementation("org.apache.commons:commons-lang3:3.18.0") {
            because("Fixes CVE-2025-48924 stack overflow vulnerability in ClassUtils")
        }
    }
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(25)
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

    build {
        dependsOn(shadowJar)
    }
    named<ShadowJar>("shadowJar") {
        from(project.rootDir) {
            include("LICENSE*", "THIRD-PARTY-LICENSES*", "README*")
        }
        archiveClassifier.set("")
        destinationDirectory.set(targetOutputDirectory)

        finalizedBy("ifAlternateLocationCopyToOutputJarFile")
    }
    register<Copy>("ifAlternateLocationCopyToOutputJarFile") {
        description = ""
        if (!extraJarEnv.isNullOrEmpty()){
            val shadowJarTask = named<ShadowJar>("shadowJar")
            dependsOn(shadowJarTask)
            from(shadowJarTask.flatMap { it.archiveFile })
            into(layout.projectDirectory.dir("OutputJarFile"))
        }
    }

}