package com.quietlogic.allisok.data.repository

import com.quietlogic.allisok.data.local.dao.CareLogDao
import com.quietlogic.allisok.data.local.entity.CareLogEntity
import kotlinx.coroutines.flow.Flow

class CareLogRepository(
    private val careLogDao: CareLogDao
) {

    fun getRecent(fromDate: String): Flow<List<CareLogEntity>> {
        return careLogDao.getRecent(fromDate)
    }

    suspend fun insert(log: CareLogEntity) {
        careLogDao.insert(log)
    }
}