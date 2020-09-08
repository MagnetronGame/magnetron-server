package org.magnetron.magnetronserver

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.lang.IllegalStateException
import kotlin.random.Random


data class Lobby(
        val playerCount: Int = 4,
        val players: MutableList<String> = mutableListOf()
)


@Service
class GameLobbyService {

    private val lobbies = mutableMapOf<String, Lobby>()

    fun createLobby(pin: GamePin): Lobby {
        val lobby = Lobby()
        lobbies[pin] = lobby
        return lobby
    }

    fun lobbyExists(pin: String): Boolean = lobbies.containsKey(pin)

    fun isLobbyFull(pin: String): Boolean {
        val lobby = lobbies[pin] ?: throw IllegalStateException("Lobby does not exist")
        return lobby.players.size >= lobby.playerCount
    }

    fun joinLobby(pin: String, playerName: String): Pair<Lobby, Int> {
        val lobby = lobbies[pin] ?: throw IllegalStateException("Lobby does not exist")
        if (!isLobbyFull(pin)) {
            lobby.players.add(playerName)
            return Pair(lobby, lobby.players.lastIndex)
        } else throw IllegalStateException("Cannot join full lobby")
    }

    fun getLobbyPlayers(pin: String): List<String> {
        val lobby = lobbies[pin] ?: throw IllegalStateException("Lobby does not exist")
        return lobby.players.toList()
    }

    fun getLobby(pin: String): Lobby? {
        val lobby = lobbies[pin]
        return lobby
    }

    fun removeLobby(pin: String): Lobby? {
        val lobby = lobbies.remove(pin)
        return lobby
    }


}