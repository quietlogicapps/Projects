package com.quietlogic.allisok.data.repository

import com.quietlogic.allisok.data.local.dao.EmergencyInfoDao
import com.quietlogic.allisok.data.local.entity.EmergencyInfoEntity
import kotlinx.coroutines.flow.Flow

class InfoRepository(
    private val emergencyInfoDao: EmergencyInfoDao
) {

    fun getInfo(): Flow<EmergencyInfoEntity?> {
        return emergencyInfoDao.get()
    }

    suspend fun saveInfo(info: EmergencyInfoEntity) {
        emergencyInfoDao.upsert(info)
    }

    suspend fun clear() {
        emergencyInfoDao.clear()
    }
}