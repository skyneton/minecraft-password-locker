plugins {
    id("java")
}

group = "net.mpoisv.locker"
version = "1.9-SNAPSHOT"

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    compileOnly("io.papermc.paper:paper-api:1.20.4-R0.1-SNAPSHOT")
    compileOnly("org.xerial:sqlite-jdbc:3.43.2.2")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

tasks {
    processResources {
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