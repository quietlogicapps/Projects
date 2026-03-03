package com.quietlogic.allisok.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "emergency_info")
data class EmergencyInfoEntity(

    @PrimaryKey
    val id: Int = 1, // single row table

    val bloodType: String?,

    val allergies: String?,

    val conditions: String?,

    val notes: String?
)