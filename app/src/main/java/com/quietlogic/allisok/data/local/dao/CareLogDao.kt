package com.quietlogic.allisok.data.local.dao

import androidx.room.*
import com.quietlogic.allisok.data.local.entity.CareLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CareLogDao {

    @Query("""
        SELECT * FROM care_logs 
        WHERE date >= :fromDate 
        ORDER BY date DESC, scheduledTime DESC
    """)
    fun getRecent(fromDate: String): Flow<List<CareLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: CareLogEntity)
}