package org.magnetron.magnetronserver

import org.springframework.stereotype.Service

typealias GamePin = String

@Service
class GamePinService {
    private val usedPins = mutableSetOf<GamePin>()
    private val pinPossibleCharacters = listOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')

    fun generatePin(): GamePin {
        var pin: GamePin
        do {
            pin = (0 until 4).joinToString("") { pinPossibleCharacters.random().toString() }
        } while (!usedPins.add(pin))
        return pin
    }

    fun throwPin(pin: GamePin) {
        usedPins.remove(pin)
    }
}