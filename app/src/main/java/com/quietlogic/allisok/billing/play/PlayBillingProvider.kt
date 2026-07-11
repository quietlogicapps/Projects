package com.quietlogic.allisok.billing.play

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.ProductDetails
import com.quietlogic.allisok.BillingManager
import com.quietlogic.allisok.billing.store.StoreBillingListener
import com.quietlogic.allisok.billing.store.StoreBillingProvider

class PlayBillingProvider(
    context: Context,
    private val storeListener: StoreBillingListener
) : StoreBillingProvider, BillingManager.Listener {

    private val billingManager = BillingManager(context, this)

    override fun connect() {
        billingManager.startConnection()
    }

    override fun queryProduct() {
        billingManager.queryProductDetails()
    }

    override fun purchase(activity: Activity) {
        billingManager.launchPurchase(activity)
    }

    override fun restore() {
        billingManager.restorePurchases()
    }

    override fun disconnect() {
        billingManager.endConnection()
    }

    override fun onBillingReady() {
        storeListener.onStoreReady()
    }

    override fun onBillingDisconnected() {
        storeListener.onStoreDisconnected()
    }

    override fun onProductLoaded(productDetails: ProductDetails) {
        storeListener.onProductReady()
    }

    override fun onPurchaseSuccess() {
        storeListener.onPurchaseSuccess()
    }

    override fun onPurchaseCancelled() {
        storeListener.onPurchaseCancelled()
    }

    override fun onPurchasePending() {
        storeListener.onPurchasePending()
    }

    override fun onPurchaseRestored(hasPurchase: Boolean) {
        storeListener.onRestoreResult(hasPurchase)
    }

    override fun onBillingError(message: String) {
        storeListener.onStoreError(message)
    }
}
