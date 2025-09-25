rootProject.name = "worldedit"

include("worldedit-libs")

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://repo.papermc.io/repository/maven-public/")
    }
    plugins {
        id("io.papermc.paperweight.userdev") version "2.0.0-beta.18"
    }
}

listOf("1.19.4").forEach {
    include("worldedit-bukkit:adapters:adapter-$it")
}

listOf("bukkit", "core","cli").forEach {
    include("worldedit-libs:$it")
    include("worldedit-$it")
}
include("worldedit-libs:core:ap")

include("worldedit-core:doctools")
