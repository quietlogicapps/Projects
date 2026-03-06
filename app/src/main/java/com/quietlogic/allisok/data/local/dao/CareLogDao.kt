package com.quietlogic.allisok.data.local.dao

import androidx.room.*
import com.quietlogic.allisok.data.local.entity.CareLogEntity
import com.quietlogic.allisok.data.local.entity.RecentTakenItem
import kotlinx.coroutines.flow.Flow

@Dao
interface CareLogDao {

    @Query("""
        SELECT * FROM care_logs
        WHERE date >= :fromDate
        ORDER BY date DESC, scheduledTime DESC
    """)
    fun getRecent(fromDate: String): Flow<List<CareLogEntity>>

    @Query("""
        SELECT
            care_logs.date AS date,
            care_logs.scheduledTime AS scheduledTime,
            care_items.name AS careItemName
        FROM care_logs
        INNER JOIN care_items
            ON care_logs.careItemId = care_items.id
        WHERE care_logs.date >= :fromDate
        ORDER BY care_logs.date DESC, care_logs.scheduledTime DESC
    """)
    fun getRecentTakenItems(fromDate: String): Flow<List<RecentTakenItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: CareLogEntity)
}