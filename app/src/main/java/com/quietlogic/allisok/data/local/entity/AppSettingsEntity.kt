package com.quietlogic.allisok.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_settings")
data class AppSettingsEntity(

    @PrimaryKey
    val id: Int = 1, // single row table

    val appPinHash: String?,

    val adminPinHash: String?,

    val trialStartTimestamp: Long?,

    val isTrialUsed: Boolean = false,

    val languageCode: String = "en"
)