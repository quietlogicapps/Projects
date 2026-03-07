package com.quietlogic.allisok.security

data class PinState(

    val userPinEnabled: Boolean,

    val userPinHash: String?,

    val adminPinEnabled: Boolean,

    val adminPinHash: String?

)