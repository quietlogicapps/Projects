package com.quietlogic.allisok.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "care_items")
data class CareItemEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val name: String,

    val instruction: String?, // None / Before food / After food

    val startDate: LocalDate,

    val endDate: LocalDate,

    val repeatType: String,
    // DAILY or SPECIFIC_DAYS

    val specificDays: String?
    // comma separated values if SPECIFIC_DAYS (e.g. "1,3,5")
)