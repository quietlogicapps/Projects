package com.quietlogic.allisok.data.backup

import android.content.Context
import androidx.room.withTransaction
import com.quietlogic.allisok.data.local.db.AppDatabase
import com.quietlogic.allisok.data.local.entity.AppSettingsEntity
import com.quietlogic.allisok.data.local.entity.CareItemEntity
import com.quietlogic.allisok.data.local.entity.CareLogEntity
import com.quietlogic.allisok.data.local.entity.CareTimeEntity
import com.quietlogic.allisok.data.local.entity.ContactSlotEntity
import com.quietlogic.allisok.data.local.entity.EmergencyInfoEntity
import org.json.JSONObject
import java.time.LocalDate
import java.time.LocalTime

class RestoreRepository(
    private val context: Context,
    private val db: AppDatabase
) {

    suspend fun restoreFromJson(json: String) {
        val root = JSONObject(json)
        val sharedPreferencesSection = root.getJSONObject("sharedPreferences")

        db.withTransaction {
            val contactsArray = root.getJSONArray("contacts")
            val careItemsArray = root.getJSONArray("careItems")
            val careTimesArray = root.getJSONArray("careTimes")
            val careLogsArray = root.getJSONArray("careLogs")

            db.contactSlotDao().clearAll()

            for (i in 0 until contactsArray.length()) {
                val obj = contactsArray.getJSONObject(i)

                val contact = ContactSlotEntity(
                    slotId = obj.getInt("slotId"),
                    label = obj.getString("label"),
                    phoneNumber = if (obj.isNull("phoneNumber")) null else obj.getString("phoneNumber"),
                    iconType = obj.getString("iconType")
                )

                db.contactSlotDao().insert(contact)
            }

            val existingItems = db.careItemDao().getAllDirect()

            for (item in existingItems) {
                db.careTimeDao().deleteByItemId(item.id)
                db.careItemDao().delete(item)
            }

            for (i in 0 until careItemsArray.length()) {
                val obj = careItemsArray.getJSONObject(i)

                val item = CareItemEntity(
                    id = obj.getLong("id"),
                    name = obj.getString("name"),
                    instruction = obj.getString("instruction"),
                    startDate = LocalDate.parse(obj.getString("startDate")),
                    endDate = LocalDate.parse(obj.getString("endDate")),
                    repeatType = obj.getString("repeatType"),
                    isArchived = obj.getBoolean("isArchived")
                )

                db.careItemDao().insert(item)
            }

            for (i in 0 until careTimesArray.length()) {
                val obj = careTimesArray.getJSONObject(i)

                val time = CareTimeEntity(
                    careItemId = obj.getLong("careItemId"),
                    time = LocalTime.parse(obj.getString("time"))
                )

                db.careTimeDao().insert(time)
            }

            for (i in 0 until careLogsArray.length()) {
                val obj = careLogsArray.getJSONObject(i)

                val log = CareLogEntity(
                    careItemId = obj.getLong("careItemId"),
                    date = LocalDate.parse(obj.getString("date")),
                    scheduledTime = LocalTime.parse(obj.getString("scheduledTime"))
                )

                db.careLogDao().insert(log)
            }

            if (root.has("emergencyInfo") && !root.isNull("emergencyInfo")) {
                db.emergencyInfoDao().clear()
                db.emergencyInfoDao().upsert(parseEmergencyInfo(root.getJSONObject("emergencyInfo")))
            }

            if (root.has("settings") && !root.isNull("settings")) {
                db.appSettingsDao().clear()
                db.appSettingsDao().upsert(parseSettings(root.getJSONObject("settings")))
            }
        }

        BackupPreferencesSnapshot.restore(context, sharedPreferencesSection)
    }

    private fun parseEmergencyInfo(section: JSONObject): EmergencyInfoEntity {
        return EmergencyInfoEntity(
            id = 1,
            bloodType = readNullableString(section, "bloodType"),
            allergies = readNullableString(section, "allergies"),
            conditions = readNullableString(section, "conditions"),
            notes = readNullableString(section, "notes")
        )
    }

    private fun parseSettings(section: JSONObject): AppSettingsEntity {
        return AppSettingsEntity(
            id = section.getInt("id"),
            appPinHash = readNullableString(section, "appPinHash"),
            adminPinHash = readNullableString(section, "adminPinHash"),
            trialStartTimestamp = if (section.isNull("trialStartTimestamp")) {
                null
            } else {
                section.getLong("trialStartTimestamp")
            },
            isTrialUsed = section.getBoolean("isTrialUsed"),
            languageCode = section.getString("languageCode"),
            dateFormat = section.getString("dateFormat")
        )
    }

    private fun readNullableString(section: JSONObject, key: String): String? {
        return if (section.isNull(key)) null else section.getString(key)
    }
}
