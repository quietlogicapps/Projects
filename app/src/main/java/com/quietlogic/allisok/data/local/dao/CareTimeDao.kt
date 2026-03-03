package com.quietlogic.allisok.data.local.dao

import androidx.room.*
import com.quietlogic.allisok.data.local.entity.CareTimeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CareTimeDao {

    @Query("SELECT * FROM care_times WHERE careItemId = :itemId")
    fun getByItemId(itemId: Long): Flow<List<CareTimeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(time: CareTimeEntity)

    @Query("DELETE FROM care_times WHERE careItemId = :itemId")
    suspend fun deleteByItemId(itemId: Long)
}