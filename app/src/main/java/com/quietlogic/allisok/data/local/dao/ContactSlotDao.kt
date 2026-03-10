package com.quietlogic.allisok.data.local.dao

import androidx.room.*
import com.quietlogic.allisok.data.local.entity.ContactSlotEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactSlotDao {

    @Query("SELECT * FROM contact_slots ORDER BY slotId ASC")
    fun getAll(): Flow<List<ContactSlotEntity>>

    @Query("SELECT * FROM contact_slots ORDER BY slotId ASC")
    suspend fun getAllDirect(): List<ContactSlotEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(contact: ContactSlotEntity)

    @Update
    suspend fun update(contact: ContactSlotEntity)

    @Query("DELETE FROM contact_slots")
    suspend fun clearAll()
}