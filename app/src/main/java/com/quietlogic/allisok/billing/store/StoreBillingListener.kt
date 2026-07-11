package com.quietlogic.allisok.billing.store

interface StoreBillingListener {
    fun onStoreReady()
    fun onStoreDisconnected()
    fun onProductReady()
    fun onPurchaseSuccess()
    fun onPurchaseCancelled()
    fun onPurchasePending()
    fun onRestoreResult(hasPurchase: Boolean)
    fun onStoreError(message: String)
}
