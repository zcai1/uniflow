import org.jetbrains.gradle.ext.compiler
import org.jetbrains.gradle.ext.settings

plugins {
    id("java")
    id("com.diffplug.spotless") version Versions.SPOTLESS
    id("org.jetbrains.gradle.plugin.idea-ext") version Versions.IDEA_EXT
    id("com.github.johnrengelman.shadow") version Versions.SHADOW
}

group = "org.cfginference"
version = "1.0-SNAPSHOT"

val compilerArgsForJavacModules by extra(arrayOf(
    // These are required in Java 16+ because the --illegal-access option is set to deny
    // by default.  None of these packages are accessed via reflection, so the module
    // only needs to be exported, but not opened.
    "--add-exports", "jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED",
    "--add-exports", "jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED",
    "--add-exports", "jdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED",
    "--add-exports", "jdk.compiler/com.sun.tools.javac.main=ALL-UNNAMED",
    "--add-exports", "jdk.compiler/com.sun.tools.javac.model=ALL-UNNAMED",
    "--add-exports", "jdk.compiler/com.sun.tools.javac.processing=ALL-UNNAMED",
    "--add-exports", "jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED",
    "--add-exports", "jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED",
))

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.checkerframework:javacutil:${Versions.CHECKER_FRAMEWORK}")
    implementation("org.checkerframework:dataflow:${Versions.CHECKER_FRAMEWORK}")
    implementation("com.google.guava:guava:${Versions.GUAVA}")

    annotationProcessor("com.google.auto.value:auto-value:${Versions.AUTO_VALUE}")
    compileOnly("com.google.auto.value:auto-value-annotations:${Versions.AUTO_VALUE}")

    implementation("com.beust:jcommander:${Versions.JCOMMANDER}")

    // AFU is an "includedBuild" imported in settings.gradle.kts, so the version number doesn"t matter.
    // https://docs.gradle.org/current/userguide/composite_builds.html#settings_defined_composite
    implementation("org.checkerframework:annotation-file-utilities:*") {
        exclude(group = "com.google.errorprone", module = "javac")
    }

    testImplementation("org.junit.jupiter:junit-jupiter-api:${Versions.JUNIT}")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${Versions.JUNIT}")
}

allprojects {
    tasks.withType<JavaCompile> {
        // Setting `sourceCompatibility` as a temporary fix for
        // an unsolved gradle issue: https://github.com/gradle/gradle/issues/18824
        sourceCompatibility = "17"
        options.compilerArgs.addAll(compilerArgsForJavacModules)
        options.isFork = true
        options.forkOptions.jvmArgs = (options.forkOptions.jvmArgs ?: listOf()).plus(compilerArgsForJavacModules)
    }
}

tasks.shadowJar {
    archiveBaseName.set("cfg-inf")

    manifest {
        attributes["Description"] = "CFG-based Inference"
        attributes["Main-Class"] = "org.cfginference.InferenceMain"
    }
}

idea.project.settings {
    compiler {
        javac {
            // forces Intellij IDEA to recognize the extra compiler args
            javacAdditionalOptions = compilerArgsForJavacModules.joinToString(" ")
        }
    }
}

spotless {
    format("misc") {
        // define the files to apply `misc` to
        target("*.gradle", "*.md", ".gitignore")

        trimTrailingWhitespace()
        indentWithSpaces()
        endWithNewline()
    }
    java {
        importOrder()
        removeUnusedImports()
        googleJavaFormat(Versions.GOOGLE_JAVA_FORMAT).reflowLongStrings()

        val regex = Regex("import\\s+[^\\*\\s]+\\.\\*;(\\r\\n|\\r|\\n)")
        custom("No wildcard imports") { raw ->
            assert(!raw.matches(regex))
            raw
        }
    }
}

tasks.test {
    useJUnitPlatform()
}
