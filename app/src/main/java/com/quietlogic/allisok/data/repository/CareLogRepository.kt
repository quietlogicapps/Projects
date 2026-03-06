package com.quietlogic.allisok.data.repository

import com.quietlogic.allisok.data.local.dao.CareLogDao
import com.quietlogic.allisok.data.local.entity.CareLogEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class CareLogRepository(
    private val careLogDao: CareLogDao
) {

    fun getRecent(fromDate: String): Flow<List<CareLogEntity>> {
        return careLogDao.getRecent(fromDate)
    }

    fun getRecentLast72Hours(): Flow<List<CareLogEntity>> {
        val fromDate = LocalDate.now().minusDays(3).toString()
        return careLogDao.getRecent(fromDate)
    }

    suspend fun insert(log: CareLogEntity) {
        careLogDao.insert(log)
    }
}