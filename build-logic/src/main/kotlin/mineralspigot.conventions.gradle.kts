import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

plugins {
    java
    `java-library`
}

repositories {
    mavenCentral()

    //maven(url = "https://oss.sonatype.org/content/groups/public")
    maven(url = "https://hub.spigotmc.org/nexus/content/groups/public")
}

group = "gg.mineral.mineralspigot"
version = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))

tasks {
    withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    val prepareTestServerDir by registering {
        doLast {
            mkdir("$buildDir/test-server")
        }
    }

    test {
        workingDir = file("$buildDir/test-server/")

        dependsOn(prepareTestServerDir)
    }
}
