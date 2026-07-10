package com.quietlogic.allisok.data.backup

import android.content.Context
import com.quietlogic.allisok.data.local.db.AppDatabase
import com.quietlogic.allisok.data.local.entity.AppSettingsEntity
import com.quietlogic.allisok.data.local.entity.EmergencyInfoEntity
import org.json.JSONArray
import org.json.JSONObject

class BackupRepository(
    private val context: Context,
    private val db: AppDatabase
) {

    suspend fun buildExportJson(): String {
        val root = JSONObject()
        root.put("backupVersion", BackupConstants.BACKUP_VERSION)

        val contacts = db.contactSlotDao().getAllDirect()
        val contactsArray = JSONArray()
        contacts.forEach {
            val obj = JSONObject()
            obj.put("slotId", it.slotId)
            obj.put("label", it.label)
            putNullableString(obj, "phoneNumber", it.phoneNumber)
            obj.put("iconType", it.iconType)
            contactsArray.put(obj)
        }
        root.put("contacts", contactsArray)

        val careItems = db.careItemDao().getAllDirect()
        val careItemsArray = JSONArray()
        careItems.forEach {
            val obj = JSONObject()
            obj.put("id", it.id)
            obj.put("name", it.name)
            obj.put("instruction", it.instruction)
            obj.put("startDate", it.startDate.toString())
            obj.put("endDate", it.endDate.toString())
            obj.put("repeatType", it.repeatType)
            obj.put("isArchived", it.isArchived)
            careItemsArray.put(obj)
        }
        root.put("careItems", careItemsArray)

        val careTimes = db.careTimeDao().getAllDirect()
        val timesArray = JSONArray()
        careTimes.forEach {
            val obj = JSONObject()
            obj.put("careItemId", it.careItemId)
            obj.put("time", it.time.toString())
            timesArray.put(obj)
        }
        root.put("careTimes", timesArray)

        val logs = db.careLogDao().getAllDirect()
        val logsArray = JSONArray()
        logs.forEach {
            val obj = JSONObject()
            obj.put("careItemId", it.careItemId)
            obj.put("date", it.date.toString())
            obj.put("scheduledTime", it.scheduledTime.toString())
            logsArray.put(obj)
        }
        root.put("careLogs", logsArray)

        val emergency = db.emergencyInfoDao().getDirect()
        if (emergency != null) {
            root.put("emergencyInfo", emergencyInfoToJson(emergency))
        }

        val settings = db.appSettingsDao().getDirect()
        if (settings != null) {
            root.put("settings", settingsToJson(settings))
        }

        root.put("sharedPreferences", BackupPreferencesSnapshot.exportToJson(context))

        return root.toString()
    }

    private fun emergencyInfoToJson(entity: EmergencyInfoEntity): JSONObject {
        val obj = JSONObject()
        putNullableString(obj, "bloodType", entity.bloodType)
        putNullableString(obj, "allergies", entity.allergies)
        putNullableString(obj, "conditions", entity.conditions)
        putNullableString(obj, "notes", entity.notes)
        return obj
    }

    private fun settingsToJson(entity: AppSettingsEntity): JSONObject {
        val obj = JSONObject()
        obj.put("id", entity.id)
        putNullableString(obj, "appPinHash", entity.appPinHash)
        putNullableString(obj, "adminPinHash", entity.adminPinHash)
        if (entity.trialStartTimestamp == null) {
            obj.put("trialStartTimestamp", JSONObject.NULL)
        } else {
            obj.put("trialStartTimestamp", entity.trialStartTimestamp)
        }
        obj.put("isTrialUsed", entity.isTrialUsed)
        obj.put("languageCode", entity.languageCode)
        obj.put("dateFormat", entity.dateFormat)
        return obj
    }

    private fun putNullableString(objectJson: JSONObject, key: String, value: String?) {
        if (value == null) {
            objectJson.put(key, JSONObject.NULL)
        } else {
            objectJson.put(key, value)
        }
    }
}
