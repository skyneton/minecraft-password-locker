plugins {
    id("java")
}

group = "net.mpoisv.locker"
version = "1.3-SNAPSHOT"

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    compileOnly("org.spigotmc:spigot:1.19.4-R0.1-SNAPSHOT")
    compileOnly("org.xerial:sqlite-jdbc:3.43.2.2")
}

tasks{
    processResources{
        from(sourceSets.main.get().resources.srcDirs) {
            filesMatching("plugin.yml") {
                expand(
                    "name" to rootProject.name,
                    "version" to version,
                    "main" to "net.mpoisv.locker",
                )
            }
            duplicatesStrategy = DuplicatesStrategy.INCLUDE
        }
    }
    test {
        useJUnitPlatform()
    }
}