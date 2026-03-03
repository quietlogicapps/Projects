package com.quietlogic.allisok.data.local.dao

import androidx.room.*
import com.quietlogic.allisok.data.local.entity.CareItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CareItemDao {

    @Query("SELECT * FROM care_items ORDER BY id DESC")
    fun getAllActive(): Flow<List<CareItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: CareItemEntity): Long

    @Update
    suspend fun update(item: CareItemEntity)

    @Delete
    suspend fun delete(item: CareItemEntity)
}