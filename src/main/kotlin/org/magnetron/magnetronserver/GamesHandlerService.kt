package org.magnetron.magnetronserver

import magnetron_kotlin.MagAction
import magnetron_kotlin.MagState
import magnetron_kotlin.Magnetron
import org.springframework.stereotype.Service
import java.lang.IllegalArgumentException
import kotlin.random.Random

@Service
class GamesHandlerService {

    private val runningGames: MutableMap<String, Magnetron> = mutableMapOf()

    fun isRunningGame(pin: String) = runningGames.containsKey(pin)

    fun createGame(): String {
        val game = Magnetron()
        game.start()
        val pin = generatePin()
        runningGames[pin] = game
        return pin
    }

    fun getGameState(pin: String): MagState =
            runningGames[pin]?.currentState ?: throw IllegalArgumentException("Invalid pin")

    fun getPossibleActions(pin: String): List<MagAction> =
        runningGames[pin]?.possibleActions ?: listOf()

    fun performAction(pin: String, action: MagAction): MagState {
        return runningGames[pin]?.performAction(action) ?: throw IllegalArgumentException("Illegal action")
    }

    fun removeGame(pin: String): Boolean =
        runningGames.remove(pin) != null


    private fun generatePin(): String {
        while (true) {
            val pin = (0 until 4).joinToString("") { Random.nextInt(0, 9).toString() }
            if (!runningGames.containsKey(pin)) {
                return pin
            }
        }

    }
}