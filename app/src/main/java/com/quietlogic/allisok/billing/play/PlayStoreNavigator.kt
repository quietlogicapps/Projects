package com.quietlogic.allisok.billing.play

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.quietlogic.allisok.billing.store.StoreNavigator

class PlayStoreNavigator : StoreNavigator {

    override fun openStorePage(context: Context) {
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://play.google.com/store/apps/developer?id=QuietLogic")
        )
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            // Store app unavailable — fail safely without crashing
        }
    }
}
