package org.magnetron.magnetronserver

import org.springframework.stereotype.Service
import java.lang.IllegalStateException
import kotlin.random.Random


data class Lobby(
        val playerCount: Int = 4,
        val players: MutableList<String> = mutableListOf()
)


@Service
class GameLobbyService {

    private val usedPins = mutableSetOf<String>()
    private val lobbies = mutableMapOf<String, Lobby>()

    fun createLobby(): String {
        val pin = generatePin()
        lobbies[pin] = Lobby()
        return pin
    }

    fun lobbyExists(pin: String): Boolean = lobbies.containsKey(pin)

    fun isLobbyFull(pin: String): Boolean {
        val lobby = lobbies[pin] ?: throw IllegalStateException("Lobby does not exist")
        return lobby.players.size < lobby.playerCount
    }

    fun joinLobby(pin: String, playerName: String): Int {
        val lobby = lobbies[pin] ?: throw IllegalStateException("Lobby does not exist")
        if (!isLobbyFull(pin)) {
            lobby.players.add(playerName)
            return lobby.players.lastIndex
        } else throw IllegalStateException("Cannot join full lobby")
    }

    fun getLobbyPlayers(pin: String): List<String> {
        val lobby = lobbies[pin] ?: throw IllegalStateException("Lobby does not exist")
        return lobby.players.toList()
    }

    fun removeLobby(pin: String): Lobby {
        val lobby = lobbies.remove(pin) ?: throw IllegalStateException("Lobby does not exist")
        return lobby
    }

    private fun generatePin(): String {
        while (true) {
            val pin = (0 until 4).joinToString("") { Random.nextInt(0, 9).toString() }
            if (!usedPins.contains(pin)) {
                usedPins.add(pin)
                return pin
            }
        }
    }
}