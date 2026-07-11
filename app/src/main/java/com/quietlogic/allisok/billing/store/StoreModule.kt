package com.quietlogic.allisok.billing.store

import android.content.Context
import com.quietlogic.allisok.billing.play.PlayBillingProvider
import com.quietlogic.allisok.billing.play.PlayStoreConfig

object StoreModule {

    fun createStoreConfig(): StoreConfig {
        return PlayStoreConfig()
    }

    fun createBillingProvider(
        context: Context,
        listener: StoreBillingListener
    ): StoreBillingProvider {
        return PlayBillingProvider(context, listener)
    }

    fun createBillingCoordinator(
        context: Context,
        listener: StoreBillingListener
    ): BillingCoordinator {
        val provider = createBillingProvider(context, listener)
        return BillingCoordinator(provider)
    }
}
