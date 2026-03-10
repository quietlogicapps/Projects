package com.quietlogic.allisok.data.backup

import com.quietlogic.allisok.data.local.entity.AppSettingsEntity
import com.quietlogic.allisok.data.local.entity.CareItemEntity
import com.quietlogic.allisok.data.local.entity.CareLogEntity
import com.quietlogic.allisok.data.local.entity.CareTimeEntity
import com.quietlogic.allisok.data.local.entity.ContactSlotEntity
import com.quietlogic.allisok.data.local.entity.EmergencyInfoEntity

data class BackupData(

    val contacts: List<ContactSlotEntity>,

    val careItems: List<CareItemEntity>,

    val careTimes: List<CareTimeEntity>,

    val careLogs: List<CareLogEntity>,

    val emergencyInfo: EmergencyInfoEntity?,

    val settings: AppSettingsEntity?
)