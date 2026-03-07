package com.quietlogic.allisok.security

import java.security.MessageDigest

object PinHasher {

    fun hash(pin: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val bytes = md.digest(pin.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun verify(pin: String, storedHash: String?): Boolean {
        if (storedHash == null) return false
        val hash = hash(pin)
        return hash == storedHash
    }

}