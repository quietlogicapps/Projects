package com.quietlogic.allisok.security

object AdminSession {

    private var active: Boolean = false

    fun isActive(): Boolean = active

    fun start() { active = true }

    fun stop() { active = false }
}