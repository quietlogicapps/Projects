package com.quietlogic.allisok

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryProductDetailsResult
import com.android.billingclient.api.QueryPurchasesParams

class BillingManager(
    private val context: Context,
    private val listener: Listener
) : PurchasesUpdatedListener {

    interface Listener {
        fun onBillingReady()
        fun onBillingDisconnected()
        fun onProductLoaded(productDetails: ProductDetails)
        fun onPurchaseSuccess()
        fun onPurchaseCancelled()
        fun onPurchasePending()
        fun onPurchaseRestored(hasPurchase: Boolean)
        fun onBillingError(message: String)
    }

    companion object {
        const val PRODUCT_ID = "allisok_lifetime"
    }

    private val billingClient: BillingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder().enableOneTimeProducts().build()
        )
        .build()

    private var currentProductDetails: ProductDetails? = null

    fun startConnection() {
        if (billingClient.isReady) {
            listener.onBillingReady()
            queryProductDetails()
            return
        }

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    listener.onBillingReady()
                    queryProductDetails()
                } else {
                    listener.onBillingError(
                        "Billing setup error: ${billingResult.debugMessage}"
                    )
                }
            }

            override fun onBillingServiceDisconnected() {
                listener.onBillingDisconnected()
            }
        })
    }

    fun queryProductDetails() {
        if (!billingClient.isReady) {
            listener.onBillingError("Billing is not ready.")
            return
        }

        val product = QueryProductDetailsParams.Product.newBuilder()
            .setProductId(PRODUCT_ID)
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(listOf(product))
            .build()

        billingClient.queryProductDetailsAsync(params) { billingResult, queryProductDetailsResult ->
            if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
                listener.onBillingError(
                    "Product query error: ${billingResult.debugMessage}"
                )
                return@queryProductDetailsAsync
            }

            val details = queryProductDetailsResult.productDetailsList
                .firstOrNull { it.productId == PRODUCT_ID }

            if (details == null) {
                listener.onBillingError("Product not found in Play Console.")
                return@queryProductDetailsAsync
            }

            currentProductDetails = details
            listener.onProductLoaded(details)
        }
    }

    fun launchPurchase(activity: Activity) {
        if (!billingClient.isReady) {
            listener.onBillingError("Billing is not ready.")
            return
        }

        val details = currentProductDetails

        if (details == null) {
            listener.onBillingError("Product details are not loaded.")
            return
        }

        val productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(details)
            .build()

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productDetailsParams))
            .build()

        billingClient.launchBillingFlow(activity, billingFlowParams)
    }

    fun restorePurchases() {
        if (!billingClient.isReady) {
            listener.onBillingError("Billing is not ready.")
            return
        }

        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        billingClient.queryPurchasesAsync(params) { billingResult, purchases ->
            if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
                listener.onBillingError(
                    "Restore error: ${billingResult.debugMessage}"
                )
                return@queryPurchasesAsync
            }

            val hasValidPurchase = purchases.any { purchase ->
                purchase.products.contains(PRODUCT_ID) &&
                        purchase.purchaseState == Purchase.PurchaseState.PURCHASED
            }

            listener.onPurchaseRestored(hasValidPurchase)
        }
    }

    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchases: MutableList<Purchase>?
    ) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                val purchase = purchases
                    ?.firstOrNull { it.products.contains(PRODUCT_ID) }

                if (purchase == null) {
                    listener.onBillingError("Purchase completed but purchase is missing.")
                    return
                }

                handlePurchase(purchase)
            }

            BillingClient.BillingResponseCode.USER_CANCELED -> {
                listener.onPurchaseCancelled()
            }

            else -> {
                listener.onBillingError(
                    "Purchase error: ${billingResult.debugMessage}"
                )
            }
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        when (purchase.purchaseState) {
            Purchase.PurchaseState.PURCHASED -> {
                if (purchase.isAcknowledged) {
                    listener.onPurchaseSuccess()
                    return
                }

                val params = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()

                billingClient.acknowledgePurchase(params) { billingResult ->
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        listener.onPurchaseSuccess()
                    } else {
                        listener.onBillingError(
                            "Acknowledge error: ${billingResult.debugMessage}"
                        )
                    }
                }
            }

            Purchase.PurchaseState.PENDING -> {
                listener.onPurchasePending()
            }

            else -> {
                listener.onBillingError("Purchase state is not valid.")
            }
        }
    }

    fun endConnection() {
        if (billingClient.isReady) {
            billingClient.endConnection()
        }
    }
}