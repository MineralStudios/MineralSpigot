rootProject.name = "mineralspigot"

includeBuild("build-logic")

this.setupSubproject("mineralspigot-server", "MineralSpigot-Server")
this.setupSubproject("mineralspigot-api", "MineralSpigot-API")

fun setupSubproject(name: String, dir: String) {
    include(":$name")
    project(":$name").projectDir = file(dir)
}
