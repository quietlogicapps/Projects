package com.quietlogic.allisok.data.repository

import com.quietlogic.allisok.data.local.dao.AppSettingsDao
import com.quietlogic.allisok.data.local.entity.AppSettingsEntity
import kotlinx.coroutines.flow.Flow

class SettingsRepository(
    private val appSettingsDao: AppSettingsDao
) {

    fun getSettings(): Flow<AppSettingsEntity?> {
        return appSettingsDao.get()
    }

    suspend fun saveSettings(settings: AppSettingsEntity) {
        appSettingsDao.upsert(settings)
    }

    suspend fun clear() {
        appSettingsDao.clear()
    }
}