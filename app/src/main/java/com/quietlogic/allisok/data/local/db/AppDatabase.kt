package com.quietlogic.allisok.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.quietlogic.allisok.data.local.dao.AppSettingsDao
import com.quietlogic.allisok.data.local.dao.CareItemDao
import com.quietlogic.allisok.data.local.dao.CareLogDao
import com.quietlogic.allisok.data.local.dao.CareTimeDao
import com.quietlogic.allisok.data.local.dao.ContactSlotDao
import com.quietlogic.allisok.data.local.dao.EmergencyInfoDao
import com.quietlogic.allisok.data.local.entity.AppSettingsEntity
import com.quietlogic.allisok.data.local.entity.CareItemEntity
import com.quietlogic.allisok.data.local.entity.CareLogEntity
import com.quietlogic.allisok.data.local.entity.CareTimeEntity
import com.quietlogic.allisok.data.local.entity.ContactSlotEntity
import com.quietlogic.allisok.data.local.entity.EmergencyInfoEntity

@Database(
    entities = [
        ContactSlotEntity::class,
        CareItemEntity::class,
        CareTimeEntity::class,
        CareLogEntity::class,
        EmergencyInfoEntity::class,
        AppSettingsEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun contactSlotDao(): ContactSlotDao
    abstract fun careItemDao(): CareItemDao
    abstract fun careTimeDao(): CareTimeDao
    abstract fun careLogDao(): CareLogDao
    abstract fun emergencyInfoDao(): EmergencyInfoDao
    abstract fun appSettingsDao(): AppSettingsDao
}