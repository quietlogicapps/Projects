package com.quietlogic.allisok.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.time.LocalTime

@Entity(
    tableName = "care_times",
    foreignKeys = [
        ForeignKey(
            entity = CareItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["careItemId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class CareTimeEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val careItemId: Long,

    val time: LocalTime
)