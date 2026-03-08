package com.quietlogic.allisok.data.repository

import android.content.Context
import com.quietlogic.allisok.alarm.engine.AlarmPlanner
import com.quietlogic.allisok.data.local.dao.CareItemDao
import com.quietlogic.allisok.data.local.dao.CareTimeDao
import com.quietlogic.allisok.data.local.entity.CareItemEntity
import com.quietlogic.allisok.data.local.entity.CareTimeEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class CareRepository(
    private val context: Context,
    private val careItemDao: CareItemDao,
    private val careTimeDao: CareTimeDao
) {

    fun getAllCareItems(): Flow<List<CareItemEntity>> = careItemDao.getAllActive()

    fun getAllArchivedCareItems(): Flow<List<CareItemEntity>> = careItemDao.getAllArchived()

    suspend fun insertCareItem(item: CareItemEntity): Long = careItemDao.insert(item)

    suspend fun updateCareItem(item: CareItemEntity) {
        careItemDao.update(item)
    }

    suspend fun deleteCareItem(item: CareItemEntity) {
        val times = careTimeDao.getTimesForItem(item.id).map { it.time }
        AlarmPlanner(context).cancelCareItemAlarms(item.id, times)
        careTimeDao.deleteByItemId(item.id)
        careItemDao.delete(item)
    }

    suspend fun archiveExpiredItems() {
        val expiredItems = careItemDao.getExpiredItems(LocalDate.now())
        val planner = AlarmPlanner(context)

        expiredItems.forEach { item ->
            val times = careTimeDao.getTimesForItem(item.id).map { it.time }
            planner.cancelCareItemAlarms(item.id, times)
            careItemDao.update(item.copy(isArchived = true))
        }
    }

    fun getTimesForItem(itemId: Long): Flow<List<CareTimeEntity>> = careTimeDao.getByItemId(itemId)

    suspend fun insertTime(time: CareTimeEntity) {
        careTimeDao.insert(time)
    }

    suspend fun deleteTimesForItem(itemId: Long) {
        careTimeDao.deleteByItemId(itemId)
    }
}