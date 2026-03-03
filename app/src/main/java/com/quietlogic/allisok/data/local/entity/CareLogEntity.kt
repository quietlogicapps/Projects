package com.quietlogic.allisok.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalTime

@Entity(
    tableName = "care_logs",
    foreignKeys = [
        ForeignKey(
            entity = CareItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["careItemId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class CareLogEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val careItemId: Long,

    val date: LocalDate,

    val scheduledTime: LocalTime
)