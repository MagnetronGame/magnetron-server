package org.magnetron.magnetronserver


import magnetron_kotlin.MagAction
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

@RestController()
class Controller {

    @Autowired
    lateinit var gamesHandlerService: GamesHandlerService

    @GetMapping("api/hello")
    fun hello() = "hello"

    @PostMapping("api/createGame")
    fun createGame(): String = "\"${gamesHandlerService.createGame()}\""

    @GetMapping("api/gameState/{pin}")
    fun gameState(@PathVariable pin: String) = gamesHandlerService.getGameState(pin)

    @GetMapping("api/possibleActions/{pin}")
    fun possibleActions(@PathVariable pin: String) = gamesHandlerService.getPossibleActions(pin)

    @PostMapping("api/performAction/{pin}")
    fun performAction(@PathVariable pin: String, @RequestBody action: MagAction) =
            gamesHandlerService.performAction(pin, action)

    @DeleteMapping("api/removeGame/{pin}")
    fun removeGame(@PathVariable pin: String) = gamesHandlerService.removeGame(pin)
}