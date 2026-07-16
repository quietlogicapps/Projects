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

            val matchingPurchases = purchases.filter { purchase ->
                purchase.products.contains(PRODUCT_ID) &&
                        purchase.purchaseState == Purchase.PurchaseState.PURCHASED
            }

            if (matchingPurchases.isEmpty()) {
                listener.onPurchaseRestored(false)
                return@queryPurchasesAsync
            }

            var remaining = matchingPurchases.size
            var anySuccess = false

            matchingPurchases.forEach { purchase ->
                handlePurchase(purchase, emitPurchaseSuccess = false) { success ->
                    if (success) {
                        anySuccess = true
                    }
                    remaining -= 1
                    if (remaining == 0) {
                        listener.onPurchaseRestored(anySuccess)
                    }
                }
            }
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

                handlePurchase(purchase, emitPurchaseSuccess = true)
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

    private fun handlePurchase(
        purchase: Purchase,
        emitPurchaseSuccess: Boolean,
        onProcessed: ((Boolean) -> Unit)? = null
    ) {
        if (!purchase.products.contains(PRODUCT_ID)) {
            onProcessed?.invoke(false)
            return
        }

        when (purchase.purchaseState) {
            Purchase.PurchaseState.PURCHASED -> {
                if (purchase.isAcknowledged) {
                    if (emitPurchaseSuccess) {
                        listener.onPurchaseSuccess()
                    }
                    onProcessed?.invoke(true)
                    return
                }

                val params = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()

                billingClient.acknowledgePurchase(params) { billingResult ->
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        if (emitPurchaseSuccess) {
                            listener.onPurchaseSuccess()
                        }
                        onProcessed?.invoke(true)
                    } else {
                        listener.onBillingError(
                            "Acknowledge error: ${billingResult.debugMessage}"
                        )
                        onProcessed?.invoke(false)
                    }
                }
            }

            Purchase.PurchaseState.PENDING -> {
                listener.onPurchasePending()
                onProcessed?.invoke(false)
            }

            else -> {
                listener.onBillingError("Purchase state is not valid.")
                onProcessed?.invoke(false)
            }
        }
    }

    fun endConnection() {
        if (billingClient.isReady) {
            billingClient.endConnection()
        }
    }
}