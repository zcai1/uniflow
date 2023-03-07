import org.jetbrains.gradle.ext.compiler
import org.jetbrains.gradle.ext.settings
import org.checkerframework.gradle.plugin.CheckerFrameworkExtension

plugins {
    id("java")
    id("checkstyle")
    id("com.diffplug.spotless") version Versions.SPOTLESS
    id("org.jetbrains.gradle.plugin.idea-ext") version Versions.IDEA_EXT
    id("com.github.johnrengelman.shadow") version Versions.SHADOW
    id("org.checkerframework") version Versions.CF_PLUGIN
}


apply(plugin = "org.checkerframework")

group = "org.uniflow"
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
    "--add-exports", "jdk.compiler/com.sun.tools.javac.comp=ALL-UNNAMED",
    "--add-exports", "jdk.compiler/com.sun.tools.javac.resources=ALL-UNNAMED",
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
    implementation("io.github.eisop:javacutil:${Versions.CHECKER_FRAMEWORK}")
    implementation("io.github.eisop:dataflow:${Versions.CHECKER_FRAMEWORK}")
    implementation("io.github.eisop:checker-qual:${Versions.CHECKER_FRAMEWORK}")

    implementation("com.google.guava:guava:${Versions.GUAVA}") {
        exclude(group = "org.checkerframework", module = "checker-qual")
    }

    annotationProcessor("com.google.auto.value:auto-value:${Versions.AUTO_VALUE}")
    compileOnly("com.google.auto.value:auto-value-annotations:${Versions.AUTO_VALUE}")

    annotationProcessor("com.google.auto.service:auto-service:${Versions.AUTO_SERVICE}")
    compileOnly("com.google.auto.service:auto-service-annotations:${Versions.AUTO_SERVICE}")

    implementation("info.picocli:picocli:${Versions.PICOCLI}")

    implementation("ch.qos.logback:logback-core:${Versions.LOGBACK}")
    implementation("ch.qos.logback:logback-classic:${Versions.LOGBACK}")
    implementation("org.slf4j:slf4j-api:${Versions.SLF4J}")

    // AFU is an "includedBuild" imported in settings.gradle.kts, so the version number doesn"t matter.
    // https://docs.gradle.org/current/userguide/composite_builds.html#settings_defined_composite
    implementation("io.github.eisop:annotation-file-utilities:*") {
        exclude(group = "com.google.errorprone", module = "javac")
    }

    implementation("org.ow2.sat4j:org.ow2.sat4j.core:${Versions.SAT4J}")
    implementation("org.ow2.sat4j:org.ow2.sat4j.maxsat:${Versions.SAT4J}")

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

    tasks.withType<Test> {
        val args = jvmArgs ?: mutableListOf()
        args.addAll(compilerArgsForJavacModules)
        jvmArgs = args
    }
}

tasks.shadowJar {
    description = "Creates a fat JAR"

    dependencies {
        exclude(dependency("junit:.*:.*"))
    }

    manifest {
        attributes["Description"] = "CFG-based Inference"
        attributes["Main-Class"] = "org.uniflow.InferenceMain"
    }
    archiveFileName.set("uniflow.jar")
    destinationDirectory.set(file("${projectDir}/bin"))
}

idea.project.settings {
    compiler {
        javac {
            // forces Intellij IDEA to recognize the extra compiler args
            javacAdditionalOptions = compilerArgsForJavacModules.joinToString(" ")
        }
    }
}

// NOTE: make sure to run spotless under java 17 or later
spotless {
    format("misc") {
        // define the files to apply `misc` to
        target("*.gradle", "*.md", ".gitignore")

        trimTrailingWhitespace()
        indentWithSpaces()
        endWithNewline()
    }
    java {
        googleJavaFormat(Versions.GOOGLE_JAVA_FORMAT).aosp().reflowLongStrings()
        importOrder("com", "jdk", "lib", "org", "java", "javax")
    }
}

checkstyle {
    toolVersion = Versions.CHECKSTYLE
}

tasks.withType<Checkstyle> {
    reports {
        // disable reporting to files
        xml.required.set(false)
        html.required.set(false)
    }
}

configure<CheckerFrameworkExtension> {
    excludeTests = true
    // temporarily disable
    checkers = listOf(
//        "org.checkerframework.checker.nullness.NullnessChecker"
    )
    extraJavacArgs = listOf(
        "-AskipDefs=^.*AutoValue_.*$" // don't check generated AutoValues
    )
}

tasks.test {
    useJUnitPlatform()
}

// TODO: find cyclic dependencies between classes that are stored in Context, as they may cause infinite recursion
