//includeBuild("../sol_champ") {
//    dependencySubstitution {
//        substitute(module("com.github.sol-ai:sol_champ/sol_game:-SNAPSHOT")).with(project(":sol_game"))
//        substitute(module("com.github.sol-ai:sol_champ/sol_engine:-SNAPSHOT")).with(project(":sol_engine"))
//    }
//}

includeBuild("../../magnetron_kotlin") {
    dependencySubstitution {
        substitute(module("org.magnetron:magnetron_kotlin:-SNAPSHOT")).with(project(":"))
    }
}

rootProject.name = "magnetron-server"
