package com.quietlogic.allisok.billing.store

import android.app.Activity

class BillingCoordinator(
    private val provider: StoreBillingProvider
) {

    fun connect() {
        provider.connect()
    }

    fun queryProduct() {
        provider.queryProduct()
    }

    fun purchase(activity: Activity) {
        provider.purchase(activity)
    }

    fun restore() {
        provider.restore()
    }

    fun disconnect() {
        provider.disconnect()
    }
}
