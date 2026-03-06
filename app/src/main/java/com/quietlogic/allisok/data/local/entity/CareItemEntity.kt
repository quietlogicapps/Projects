package com.quietlogic.allisok.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "care_items")
data class CareItemEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val name: String,

    val instruction: String,

    val startDate: LocalDate,

    val endDate: LocalDate,

    val repeatType: String,

    val isArchived: Boolean = false
)