rootProject.name = "worldedit"

include("worldedit-libs")

listOf("legacy", "1.18.2", "1.19", "1.19.4").forEach {
    include("worldedit-bukkit:adapters:adapter-$it")
}

listOf("bukkit", "core","cli").forEach {
    include("worldedit-libs:$it")
    include("worldedit-$it")
}
include("worldedit-libs:core:ap")

include("worldedit-core:doctools")
