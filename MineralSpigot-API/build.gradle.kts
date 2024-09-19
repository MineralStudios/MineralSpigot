import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

plugins {
    id("mineralspigot.conventions")
    `maven-publish`
}

java {
    withSourcesJar()
}

dependencies {
    api("commons-lang:commons-lang:2.6")
    api("com.googlecode.json-simple:json-simple:1.1.1")
    api("org.yaml:snakeyaml:1.15")
    api("net.md-5:bungeecord-chat:1.8-SNAPSHOT")
    compileOnlyApi("net.sf.trove4j:trove4j:3.0.3") // provided by server

    // bundled with Minecraft, should be kept in sync
    api("com.google.guava:guava:18.0")
    api("com.google.code.gson:gson:2.2.4")
    api("org.slf4j:slf4j-api:1.7.35") // PandaSpigot - Add SLF4J Logger

    // testing
    testImplementation("junit:junit:4.12")
    testImplementation("org.hamcrest:hamcrest-library:1.3")
    testImplementation("net.sf.trove4j:trove4j:3.0.3") // required by tests
}

tasks {
    java {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    
    val generateApiVersioningFile by registering {
        inputs.property("version", project.version)
        val pomProps = layout.buildDirectory.file("pom.properties")
        outputs.file(pomProps)
        doLast {
            pomProps.get().asFile.writeText("version=${project.version}")
        }
    }

    jar {
        from(generateApiVersioningFile.map { it.outputs.files.singleFile }) {
            into("META-INF/maven/${project.group}/${project.name.lowercase()}")
        }

        manifest {
            attributes(
                "Automatic-Module-Name" to "org.bukkit"
            )
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifact(tasks.jar)
            versionMapping {
                usage("java-api") {
                    fromResolutionOf("runtimeClasspath")
                }
            }

            pom {
                url.set("https://github.com/MineralStudios/MineralSpigot")
                description.set(project.description)
                name.set(project.name)
                version = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HH.mm.ss"))

                developers {
                    developer {
                        id.set("jaiden")
                        name.set("Jaiden")
                        email.set("jaiden@mineral.gg")
                    }
                }

                
            }
        }
    }

    repositories {
        maven {
            name = "mineral-dev-private"
            url = uri("https://repo.mineral.gg/private")

            credentials {
                username = (project.findProperty("gpr.user") as String?) ?: System.getenv("USERNAME")
                password = (project.findProperty("gpr.token") as String?) ?: System.getenv("TOKEN")
            }
        }
    }
}
