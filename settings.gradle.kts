rootProject.name = "nomoreplugins"

include(":nomorewintertodt")
include(":nomoreagility")
include(":nomorecombat")
include(":nomoreskilling")
include(":nomoregrounditems")
include(":nomoregroundmarkers")
include(":nomoreinventorytags")
include(":nomorenpchighlight")
include(":nomoreobjectindicators")
include(":amiscplugin")
include(":aplugintutorial")
include(":chatbox")
include(":nomoremenuindicators")

for (project in rootProject.children) {
    project.apply {
        projectDir = file(name)
        buildFileName = "${name.toLowerCase()}.gradle.kts"

        require(projectDir.isDirectory) { "Project '${project.path} must have a $projectDir directory" }
        require(buildFile.isFile) { "Project '${project.path} must have a $buildFile build script" }
    }
}
