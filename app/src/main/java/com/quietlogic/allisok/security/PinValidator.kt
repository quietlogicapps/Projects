package com.quietlogic.allisok.security

object PinValidator {

    fun isValidFormat(pin: String): Boolean {
        if (pin.length != 4) return false
        return pin.all { it.isDigit() }
    }

    fun isDifferentFromAdmin(
        userPin: String,
        adminPinHash: String?
    ): Boolean {

        if (adminPinHash == null) return true

        val userHash = PinHasher.hash(userPin)

        return userHash != adminPinHash
    }

    fun isDifferentFromUser(
        adminPin: String,
        userPinHash: String?
    ): Boolean {

        if (userPinHash == null) return true

        val adminHash = PinHasher.hash(adminPin)

        return adminHash != userPinHash
    }
}