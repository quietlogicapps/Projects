package com.quietlogic.allisok.ui.pin

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.quietlogic.allisok.security.PinHasher
import com.quietlogic.allisok.security.PinPrefs

class PinSetupActivity : AppCompatActivity() {

    private lateinit var pinPrefs: PinPrefs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        pinPrefs = PinPrefs(this)
    }

    fun saveUserPin(pin: String) {

        val hash = PinHasher.hash(pin)

        pinPrefs.setUserPin(hash)

        finish()
    }

    fun saveAdminPin(pin: String) {

        val hash = PinHasher.hash(pin)

        pinPrefs.setAdminPin(hash)

        finish()
    }
}