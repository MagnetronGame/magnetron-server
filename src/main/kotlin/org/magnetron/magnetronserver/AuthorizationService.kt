package org.magnetron.magnetronserver

import org.springframework.stereotype.Service
import java.util.*


typealias AccessToken = String


data class GamePermissions(
        val pin: String,
        val startGame: Boolean = false,
        val readGameAll: Boolean = false,
        val readGameForPlayer: List<Int> = listOf(),
        val updateGameForPlayer: List<Int> = listOf(),
        val removeGame: Boolean = false
)

@Service
class AuthenticationService {

    private val permissionsByHost
            = mutableMapOf<String, GamePermissions>()

    fun createAccessToken(): String = uuid()

    fun registerHost(token: AccessToken, gamePin: String): GamePermissions {
        val hostPermissions = GamePermissions(
                pin = gamePin,
                startGame = true,
                readGameAll = true,
                removeGame = true
        )
        permissionsByHost[token] = hostPermissions
        return hostPermissions
    }

    fun registerClient(token: AccessToken, gamePin: String, playerIndex: Int): GamePermissions {
        val clientPermission = GamePermissions(
                pin = gamePin,
                readGameForPlayer = listOf(playerIndex),
                updateGameForPlayer = listOf(playerIndex)
        )
        permissionsByHost[token] = clientPermission
        return clientPermission
    }

    fun getPermissionsFor(token: AccessToken): GamePermissions
        = permissionsByHost[token] ?: GamePermissions("-1")


    private fun uuid() = UUID.randomUUID().toString()
}

