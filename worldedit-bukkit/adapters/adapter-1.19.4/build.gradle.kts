applyPaperweightAdapterConfiguration()

plugins {
  id("io.papermc.paperweight.userdev")
}

dependencies {
  paperweight.paperDevBundle("1.19.4-R0.1-SNAPSHOT")
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public")
    maven("https://maven.enginehub.org/repo")
    maven("https://hub.spigotmc.org/nexus/content/groups/public/")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
}

