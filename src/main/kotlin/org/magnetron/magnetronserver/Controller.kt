package org.magnetron.magnetronserver


import magnetron_game_kotlin.MagAction
import magnetron_game_kotlin.MagState
import magnetron_game_kotlin.MagStatePlayerView
import org.magnetron.magnetronserver.error.InvalidAccessException
import org.magnetron.magnetronserver.error.NoExistingGameException
import org.springframework.beans.factory.annotation.Autowired
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
    lateinit var gameLobbies: GameLobbyService

    @Autowired
    lateinit var gamesHandlerService: GamesHandlerService


    @GetMapping("api/hello")
    fun hello() = "hello"

    @PostMapping("api/createGame")
    fun createGame(): CreateGameResponse {
        val accessToken: AccessToken = authorization.createAccessToken()
        val gamePin = gameLobbies.createLobby()
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
    ) {
        val permissions = authorization.getPermissionsFor(accessToken)
        if (permissions.pin == pin && permissions.startGame) {
            if (gameLobbies.lobbyExists(pin) && gameLobbies.isLobbyFull(pin)) {
                val lobby = gameLobbies.removeLobby(pin)
                gamesHandlerService.createGame(pin)
            }
        }

    }

    @PostMapping("api/joinGame/{pin}")
    fun joinGame(
            @PathVariable pin: String,
            @RequestBody name: String
    ): JoinGameResponse {
        if (!gameLobbies.lobbyExists(pin)) throw NoExistingGameException()
        else if (!gameLobbies.isLobbyFull(pin)) throw NoExistingGameException()
        else {
            val accessToken = authorization.createAccessToken()
            val playerIndex = gameLobbies.joinLobby(pin, name)
            authorization.registerClient(accessToken, pin, playerIndex)
            return JoinGameResponse(
                    pin = pin,
                    accessToken = accessToken,
                    playerIndex = playerIndex
            )
        }
    }

    @GetMapping("api/gameState/{pin}/{playerIndex}")
    fun gameStateForPlayer(
            @RequestHeader("Authorization") accessToken: String,
            @PathVariable pin: String,
            @PathVariable playerIndex: Int
    ): MagStatePlayerView {
        val permissions = authorization.getPermissionsFor(accessToken)
        return if (permissions.pin == pin && permissions.readGameForPlayer.contains(playerIndex)) {
            gamesHandlerService.getGameStatePlayerView(pin, playerIndex)
        } else throw InvalidAccessException()
    }

    @GetMapping("api/gameState/{pin}")
    fun gameState(
            @RequestHeader("Authorization") accessToken: String,
            @PathVariable pin: String
    ): MagState {
        val permissions = authorization.getPermissionsFor(accessToken)
        if (permissions.pin == pin && permissions.readGameAll) {
            return gamesHandlerService.getGameState(pin)
        } else throw InvalidAccessException()
    }

    @GetMapping("api/possibleActions/{pin}")
    fun possibleActions(
            @RequestHeader("Authorization") accessToken: String,
            @PathVariable pin: String
    ): List<MagAction> {
        val permissions = authorization.getPermissionsFor(accessToken)
        if (permissions.pin == pin) {
            return gamesHandlerService.getPossibleActions(pin)
        } else throw InvalidAccessException()
    }

    @PostMapping("api/performAction/{pin}")
    fun performAction(
            @RequestHeader("Authorization") accessToken: String,
            @PathVariable pin: String,
            @RequestBody action: MagAction
    ): MagState {
        val permissions = authorization.getPermissionsFor(accessToken)
        if (
                permissions.pin == pin
                && permissions.updateGameForPlayer.contains(gamesHandlerService.getCurrentPlayerIndex(pin))
        ) {
            return gamesHandlerService.performAction(pin, action)
        } else throw InvalidAccessException()
    }

    @DeleteMapping("api/removeGame/{pin}")
    fun removeGame(
            @RequestHeader("Authorization") accessToken: String,
            @PathVariable pin: String
    ) {
        val permissions = authorization.getPermissionsFor(accessToken)
        if (permissions.pin == pin && permissions.removeGame) {
            gamesHandlerService.removeGame(pin)
        }
    }

}