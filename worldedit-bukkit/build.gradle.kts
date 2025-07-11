import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import io.papermc.paperweight.userdev.attribute.Obfuscation

plugins {
    `java-library`
	 id("io.papermc.paperweight.userdev") version "2.0.0-beta.17"
}

applyPlatformAndCoreConfiguration()
applyShadowConfiguration()

repositories {
    mavenCentral()
    maven { url = uri("https://hub.spigotmc.org/nexus/content/groups/public") }
    maven { url = uri("https://repo.papermc.io/repository/maven-public/") }
}

val localImplementation = configurations.create("localImplementation") {
    description = "Dependencies used locally, but provided by the runtime Bukkit implementation"
    isCanBeConsumed = false
    isCanBeResolved = false
}
configurations["compileOnly"].extendsFrom(localImplementation)

val adapters = configurations.create("adapters") {
    description = "Adapters to include in the JAR"
    isCanBeConsumed = false
    isCanBeResolved = true
    shouldResolveConsistentlyWith(configurations["runtimeClasspath"])
    attributes {
        attribute(Obfuscation.OBFUSCATION_ATTRIBUTE, objects.named(Obfuscation.OBFUSCATED))
    }
}

dependencies {
    "api"(project(":worldedit-core"))
    "api"(project(":worldedit-libs:bukkit"))
    paperweight.paperDevBundle("1.19.4-R0.1-SNAPSHOT")
    "localImplementation"(platform("org.apache.logging.log4j:log4j-bom:${Versions.LOG4J}") {
        because("Spigot provides Log4J (sort of, not in API, implicitly part of server)")
    })
    "localImplementation"("org.apache.logging.log4j:log4j-api")
    "compileOnly"("org.jetbrains:annotations:20.1.0")
    "implementation"("io.papermc:paperlib:1.0.7")
    "compileOnly"("com.sk89q:dummypermscompat:1.10")
    "implementation"("org.bstats:bstats-bukkit:2.2.1")
    "implementation"("it.unimi.dsi:fastutil")
	project.project(":worldedit-bukkit:adapters").subprojects.forEach {
        "adapters"(project(it.path))
    }
}

tasks.named<Copy>("processResources") {
    val internalVersion = project.ext["internalVersion"]
    inputs.property("internalVersion", internalVersion)
    filesMatching("plugin.yml") {
        expand("internalVersion" to internalVersion)
    }
}

addJarManifest(WorldEditKind.Plugin, includeClasspath = true)

tasks.named<ShadowJar>("shadowJar") {
    dependsOn(project.project(":worldedit-bukkit:adapters").subprojects.map { it.tasks.named("assemble") })
    from(Callable {
        adapters.resolve()
            .map { f ->
                zipTree(f).matching {
                    exclude("META-INF/")
                }
            }
    })
    dependencies {
        // In tandem with not bundling log4j, we shouldn't relocate base package here.
        // relocate("org.apache.logging", "com.sk89q.worldedit.log4j")
        relocate("org.antlr.v4", "com.sk89q.worldedit.antlr4")
        // Purposefully not included, we assume (even though no API exposes it) that Log4J will be present at runtime
        // If it turns out not to be true for Spigot/Paper, our only two official platforms, this can be uncommented.
        // include(dependency("org.apache.logging.log4j:log4j-api"))
        include(dependency("org.antlr:antlr4-runtime"))
        include(dependency("org.bstats:"))
        include(dependency("io.papermc:paperlib"))
        include(dependency("it.unimi.dsi:fastutil"))
        relocate("org.bstats", "com.sk89q.worldedit.bstats")
        relocate("io.papermc.lib", "com.sk89q.worldedit.bukkit.paperlib")
        relocate("it.unimi.dsi.fastutil", "com.sk89q.worldedit.bukkit.fastutil")
    }
}

tasks.named("assemble").configure {
    dependsOn("shadowJar")
}

tasks.register("runCopyJarScript", Exec::class) {
    group = "build"
    description = "Runs the copyjar.sh script after build completion."
    workingDir(rootDir)
    commandLine("sh", "copyjar.sh", project.version.toString())
}

tasks.named("build") {
    finalizedBy("runCopyJarScript")
}

