import org.cadixdev.gradle.licenser.LicenseExtension
import org.gradle.plugins.ide.idea.model.IdeaModel
import org.gradle.api.file.Directory

plugins {
    `java-library`
    antlr
}

applyPlatformAndCoreConfiguration()

repositories {
    ivy {
        url = uri("https://repo.enginehub.org/language-files/")
        name = "EngineHub Language Files"
        patternLayout {
            artifact("[organisation]/[module]/[revision]/[artifact]-[revision](+[classifier])(.[ext])")
            setM2compatible(true)
        }
        metadataSources {
            artifact()
        }
    }
}

configurations {
    register("languageFiles")
}

dependencies {
    constraints {
        "implementation"( "org.yaml:snakeyaml") {
            version { require("1.33") }
            because("Bukkit provides SnakeYaml")
        }
    }

    "api"(project(":worldedit-libs:core"))
    "compileOnly"("de.schlichtherle:truezip:6.8.4")
    "implementation"("org.mozilla:rhino-runtime:1.7.13")
    "implementation"("org.yaml:snakeyaml:1.33")
    "implementation"("com.google.guava:guava")
    "compileOnlyApi"("com.google.code.findbugs:jsr305:1.3.9")
    "implementation"("com.google.code.gson:gson")
    "implementation"("org.apache.logging.log4j:log4j-api:${Versions.LOG4J}") {
        because("Mojang provides Log4J")
    }

    "implementation"("it.unimi.dsi:fastutil")

    val antlrVersion = "4.9.1"
    "antlr"("org.antlr:antlr4:$antlrVersion")
    "implementation"("org.antlr:antlr4-runtime:$antlrVersion")

    "compileOnly"(project(":worldedit-libs:core:ap"))
    "annotationProcessor"(project(":worldedit-libs:core:ap"))
    // ensure this is on the classpath for the AP
    "annotationProcessor"("com.google.guava:guava:${Versions.GUAVA}")
    "compileOnly"("com.google.auto.value:auto-value-annotations:${Versions.AUTO_VALUE}")
    "annotationProcessor"("com.google.auto.value:auto-value:${Versions.AUTO_VALUE}")

    "languageFiles"("${project.group}:worldedit-lang:7.2.6:355@zip")

    "testRuntimeOnly"("org.apache.logging.log4j:log4j-core:${Versions.LOG4J}")
}

tasks.named<Test>("test") {
    maxHeapSize = "1G"
}

tasks.withType<JavaCompile>().configureEach {
    dependsOn(":worldedit-libs:build")
    options.compilerArgs.add("-Aarg.name.key.prefix=")
}

tasks.named<AntlrTask>("generateGrammarSource").configure {
    val pkg = "com.sk89q.worldedit.antlr"
    outputDirectory = layout.buildDirectory
        .dir("generated-src/antlr/main/${pkg.replace('.', '/')}")
        .get()
        .asFile
    arguments = listOf(
        "-visitor", "-package", pkg,
        "-Xexact-output-dir"
    )
}

tasks.named("sourcesJar") {
    mustRunAfter("generateGrammarSource")
}

configure<LicenseExtension> {
    exclude {
        val buildRoot: Directory = layout.buildDirectory.get()
        it.file.startsWith(buildRoot.asFile)
    }
}

tasks.withType<Checkstyle>().configureEach {
    exclude("com/sk89q/worldedit/antlr/**/*.java")
}

// Give intellij info about where ANTLR code comes from
plugins.withId("idea") {
    configure<IdeaModel> {
        afterEvaluate {
            module.sourceDirs.add(file("src/main/antlr"))
            module.sourceDirs.add(file("build/generated-src/antlr/main"))
            module.generatedSourceDirs.add(file("build/generated-src/antlr/main"))
        }
    }
}

sourceSets.named("main") {
    java {
        srcDir("src/legacy/java")
    }
}

tasks.named<Copy>("processResources") {
    from(configurations.named("languageFiles")) {
        rename {
            "i18n.zip"
        }
        into("lang")
    }
}
