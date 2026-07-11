package com.quietlogic.allisok.billing.store

import android.app.Activity

interface StoreBillingProvider {
    fun connect()
    fun queryProduct()
    fun purchase(activity: Activity)
    fun restore()
    fun disconnect()
}
