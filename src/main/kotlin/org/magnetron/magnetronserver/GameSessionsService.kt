package org.magnetron.magnetronserver

import magnetron_game_kotlin.MagAction
import magnetron_game_kotlin.MagState
import magnetron_game_kotlin.MagStatePlayerView
import mu.KotlinLogging
import org.magnetron.magnetronserver.error.NotExistingException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.lang.IllegalStateException


@Service
class GameSessionsService {

    private val logger = KotlinLogging.logger {  }

    @Autowired
    lateinit var pinService: GamePinService

    @Autowired
    lateinit var gameLobbies: GameLobbyService

    @Autowired
    lateinit var gameHandler: GamesHandlerService


    fun hasLobby(pin: GamePin) = gameLobbies.lobbyExists(pin)

    fun hasGame(pin: GamePin) = gameHandler.isRunningGame(pin)

    fun hasGameSession(pin: GamePin) = hasLobby(pin) || hasGame(pin)

    fun getLobby(pin: GamePin): Lobby? = gameLobbies.getLobby(pin)

    fun getGameState(pin: GamePin): MagState = gameHandler.getGameState(pin)

    fun getGameStatePlayerView(pin: GamePin, playerIndex: Int): MagStatePlayerView =
            gameHandler.getGameStatePlayerView(pin, playerIndex)

    fun getPossibleActions(pin: GamePin): List<MagAction> = gameHandler.getPossibleActions(pin)

    fun getCurrentPlayerIndex(pin: GamePin): Int = gameHandler.getCurrentPlayerIndex(pin)

    fun performAction(pin: GamePin, action: MagAction): MagState {
        val newState = gameHandler.performAction(pin, action)
        if (newState.isTerminal) {
            removeGame(pin)
        }
        return newState
    }

    fun createLobby(): GamePin {
        val pin = pinService.generatePin()
        gameLobbies.createLobby(pin)
        return pin
    }

    fun joinLobby(pin: GamePin, playerName: String): Int {
        if (gameLobbies.lobbyExists(pin)) {
            if (!gameLobbies.isLobbyFull(pin)) {
                val (_, playerIndex) = gameLobbies.joinLobby(pin, playerName)
                return playerIndex
            } else throw IllegalAccessException()
        } else throw NotExistingException()
    }

    fun isLobbyReady(pin: GamePin) = gameLobbies.isLobbyFull(pin)

    fun startGameFromLobby(pin: GamePin): Boolean {
        return if (gameLobbies.isLobbyFull(pin)) {
            gameLobbies.removeLobby(pin)?.let { lobby ->
                gameHandler.createGame(pin)?.let { game ->
                    true
                } ?: run {
                    logger.warn { "Trying to create a game with a pin already in use: $pin" }
                    false
                }
            } ?: run {
                logger.warn { "Trying to start a game from a non-existing lobby: $pin" }
                false
            }
        } else {
            logger.warn { "Trying to start a game from a lobby that is not full: $pin" }
            false
        }
    }

    fun removeLobby(pin: GamePin) {
        gameLobbies.removeLobby(pin)?.run { pinService.throwPin(pin) }

    }

    fun removeGame(pin: GamePin) {
        gameHandler.removeGame(pin)?.run { pinService.throwPin(pin) }
    }
}