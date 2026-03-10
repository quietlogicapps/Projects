package com.quietlogic.allisok.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.quietlogic.allisok.data.local.entity.EmergencyInfoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EmergencyInfoDao {

    @Query("SELECT * FROM emergency_info WHERE id = 1")
    fun get(): Flow<EmergencyInfoEntity?>

    @Query("SELECT * FROM emergency_info WHERE id = 1")
    suspend fun getDirect(): EmergencyInfoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(info: EmergencyInfoEntity)

    @Query("DELETE FROM emergency_info")
    suspend fun clear()
}