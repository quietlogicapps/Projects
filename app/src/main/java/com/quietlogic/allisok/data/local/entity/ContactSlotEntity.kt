package com.quietlogic.allisok.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contact_slots")
data class ContactSlotEntity(

    @PrimaryKey
    val slotId: Int, // 1..4 fixed slots

    val label: String,

    val phoneNumber: String?,

    val iconType: String
)