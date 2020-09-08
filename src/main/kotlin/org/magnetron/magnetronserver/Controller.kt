package org.magnetron.magnetronserver


import magnetron_game_kotlin.MagAction
import magnetron_game_kotlin.MagState
import magnetron_game_kotlin.MagStatePlayerView
import org.magnetron.magnetronserver.error.InvalidAccessException
import org.magnetron.magnetronserver.error.NotExistingException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.messaging.simp.annotation.SubscribeMapping
import org.springframework.web.bind.annotation.*

data class CreateGameResponse(
        val pin: String,
        val accessToken: String
)

data class JoinGameResponse(
        val pin: String,
        val accessToken: String,
        val playerIndex: Int
)


@RestController()
class Controller {

    @Autowired
    lateinit var authorization: AuthenticationService

    @Autowired
    lateinit var gameSessions: GameSessionsService


    @Autowired
    lateinit var simpMessagingTemplate: SimpMessagingTemplate

    @GetMapping("api/hello")
    fun hello() = "hello"

    @SubscribeMapping("/**")
    fun onSubscribe() = "subscribed"

    @PostMapping("api/createLobby")
    fun createLobby(): CreateGameResponse {
        val accessToken: AccessToken = authorization.createAccessToken()
        val gamePin = gameSessions.createLobby()
        authorization.registerHost(accessToken, gamePin)
        return CreateGameResponse(
                pin = gamePin,
                accessToken = accessToken
        )
    }

    @PostMapping("api/startGame/{pin}")
    fun startGame(
            @RequestHeader("Authorization") accessToken: String,
            @PathVariable pin: String
    ): Boolean {
        authorization.validate(accessToken) { permissions ->
            permissions.pin == pin && permissions.startGame
        }
        return if (gameSessions.startGameFromLobby(pin)) {
            simpMessagingTemplate.convertAndSend("/notify/game/started/$pin", true)
            true
        } else false
    }

    @PostMapping("api/joinLobby/{pin}")
    fun joinLobby(
            @PathVariable pin: String,
            @RequestBody name: String
    ): JoinGameResponse {
        val playerIndex = gameSessions.joinLobby(pin, name)
        simpMessagingTemplate.convertAndSend("/notify/lobby/$pin", true)
        if (gameSessions.isLobbyReady(pin)) {
            simpMessagingTemplate.convertAndSend("/notify/lobby/ready/$pin", true)
        }
        val accessToken = authorization.createAccessToken()
        authorization.registerClient(accessToken, pin, playerIndex)
        return JoinGameResponse(
                pin = pin,
                accessToken = accessToken,
                playerIndex = playerIndex
        )
    }

    @GetMapping("api/lobby/{pin}")
    fun getLobby(
            @RequestHeader("Authorization") accessToken: String,
            @PathVariable pin: String
    ): Lobby {
        authorization.validate(accessToken) {
            it.pin == pin
        }
        return gameSessions.getLobby(pin) ?: throw NotExistingException()
    }

    @GetMapping("api/lobbyExists/{pin}")
    fun lobbyExists(
            @RequestHeader("Authorization") accessToken: String,
            @PathVariable pin: String
    ): Boolean {
        authorization.validate(accessToken) {
            it.pin == pin
        }
        return gameSessions.hasLobby(pin)
    }

    @GetMapping("api/gameExists/{pin}")
    fun gameExists(
            @RequestHeader("Authorization") accessToken: String,
            @PathVariable pin: String
    ): Boolean {
        authorization.validate(accessToken) {
            it.pin == pin
        }
        return gameSessions.hasGame(pin)
    }

    @GetMapping("api/gameState/{pin}/{playerIndex}")
    fun gameStateForPlayer(
            @RequestHeader("Authorization") accessToken: String,
            @PathVariable pin: String,
            @PathVariable playerIndex: Int
    ): MagStatePlayerView {
        authorization.validate(accessToken) {
            it.pin == pin && it.readGameForPlayer.contains(playerIndex)
        }
        return gameSessions.getGameStatePlayerView(pin, playerIndex)
    }

    @GetMapping("api/gameState/{pin}")
    fun gameState(
            @RequestHeader("Authorization") accessToken: String,
            @PathVariable pin: String
    ): MagState {
        authorization.validate(accessToken) {
            it.pin == pin && it.readGameAll
        }
        return gameSessions.getGameState(pin)
    }

    @GetMapping("api/possibleActions/{pin}")
    fun possibleActions(
            @RequestHeader("Authorization") accessToken: String,
            @PathVariable pin: String
    ): List<MagAction> {
        authorization.validate(accessToken) {
            it.pin == pin
        }
        return gameSessions.getPossibleActions(pin)
    }

    @PostMapping("api/performAction/{pin}")
    fun performAction(
            @RequestHeader("Authorization") accessToken: String,
            @PathVariable pin: String,
            @RequestBody action: MagAction
    ): MagState {
        authorization.validate(accessToken) {
            it.pin == pin && it.updateGameForPlayer.contains(gameSessions.getCurrentPlayerIndex(pin))
        }
        val newState = gameSessions.performAction(pin, action)
        simpMessagingTemplate.convertAndSend("/notify/game/state/$pin", true)
        return newState
    }

    @DeleteMapping("api/removeGame/{pin}")
    fun removeGame(
            @RequestHeader("Authorization") accessToken: String,
            @PathVariable pin: String
    ) {
        authorization.validate(accessToken) {
            it.pin == pin && it.removeGame
        }
        gameSessions.removeGame(pin)
    }

}