package com.quietlogic.allisok.data.local.dao

import androidx.room.*
import com.quietlogic.allisok.data.local.entity.CareItemEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface CareItemDao {

    @Query("SELECT * FROM care_items WHERE isArchived = 0 ORDER BY id DESC")
    fun getAllActive(): Flow<List<CareItemEntity>>

    @Query("SELECT * FROM care_items WHERE endDate < :today AND isArchived = 0")
    suspend fun getExpiredItems(today: LocalDate): List<CareItemEntity>

    @Query("SELECT * FROM care_items WHERE isArchived = 1 ORDER BY id DESC")
    fun getAllArchived(): Flow<List<CareItemEntity>>

    @Query("DELETE FROM care_items WHERE isArchived = 1 AND id NOT IN (SELECT id FROM care_items WHERE isArchived = 1 ORDER BY id DESC LIMIT 50)")
    suspend fun trimArchivedTo50()

    @Query("SELECT * FROM care_items")
    suspend fun getAllDirect(): List<CareItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: CareItemEntity): Long

    @Update
    suspend fun update(item: CareItemEntity)

    @Delete
    suspend fun delete(item: CareItemEntity)
}