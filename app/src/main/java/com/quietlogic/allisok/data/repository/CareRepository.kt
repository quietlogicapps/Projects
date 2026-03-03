package com.quietlogic.allisok.data.repository

import com.quietlogic.allisok.data.local.dao.CareItemDao
import com.quietlogic.allisok.data.local.dao.CareTimeDao
import com.quietlogic.allisok.data.local.entity.CareItemEntity
import com.quietlogic.allisok.data.local.entity.CareTimeEntity
import kotlinx.coroutines.flow.Flow

class CareRepository(
    private val careItemDao: CareItemDao,
    private val careTimeDao: CareTimeDao
) {

    fun getAllCareItems(): Flow<List<CareItemEntity>> {
        return careItemDao.getAllActive()
    }

    suspend fun insertCareItem(item: CareItemEntity): Long {
        return careItemDao.insert(item)
    }

    suspend fun updateCareItem(item: CareItemEntity) {
        careItemDao.update(item)
    }

    suspend fun deleteCareItem(item: CareItemEntity) {
        careItemDao.delete(item)
    }

    fun getTimesForItem(itemId: Long): Flow<List<CareTimeEntity>> {
        return careTimeDao.getByItemId(itemId)
    }

    suspend fun insertTime(time: CareTimeEntity) {
        careTimeDao.insert(time)
    }

    suspend fun deleteTimesForItem(itemId: Long) {
        careTimeDao.deleteByItemId(itemId)
    }
}