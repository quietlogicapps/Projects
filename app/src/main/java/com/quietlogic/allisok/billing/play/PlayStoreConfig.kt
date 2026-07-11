package com.quietlogic.allisok.billing.play

import com.quietlogic.allisok.BillingManager
import com.quietlogic.allisok.billing.store.StoreConfig

class PlayStoreConfig : StoreConfig {
    override val productId: String
        get() = BillingManager.PRODUCT_ID

    override val storeName: String
        get() = "Google Play"
}
