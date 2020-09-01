package org.magnetron.magnetronserver

import magnetron_game_kotlin.MagAction
import magnetron_game_kotlin.MagState
import magnetron_game_kotlin.Magnetron
import org.springframework.stereotype.Service
import java.lang.IllegalArgumentException
import kotlin.random.Random

@Service
class GamesHandlerService {

    private val runningGames: MutableMap<String, Magnetron> = mutableMapOf()


    fun isRunningGame(pin: String) = runningGames.containsKey(pin)

    fun createGame(pin: String): String {
        val game = Magnetron()
        game.start()
        runningGames[pin] = game
        return pin
    }

    fun getCurrentPlayerIndex(pin: String) =
            runningGames[pin]?.currentState?.avatarTurnIndex ?: throw IllegalArgumentException("Invalid pin")

    fun getGameState(pin: String): MagState =
            runningGames[pin]?.currentState ?: throw IllegalArgumentException("Invalid pin")

    fun getGameStatePlayerView(pin: String, playerIndex: Int) =
            runningGames[pin]?.currentStateForPlayer(playerIndex) ?: throw IllegalArgumentException("Invalid pin")

    fun getPossibleActions(pin: String): List<MagAction> =
        runningGames[pin]?.possibleActions ?: listOf()

    fun performAction(pin: String, action: MagAction): MagState {
        return runningGames[pin]?.performAction(action) ?: throw IllegalArgumentException("Illegal action")
    }

    fun removeGame(pin: String): Boolean =
        runningGames.remove(pin) != null



}