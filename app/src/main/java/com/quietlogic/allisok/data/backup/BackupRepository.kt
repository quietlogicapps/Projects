package com.quietlogic.allisok.data.backup

import com.quietlogic.allisok.data.local.db.AppDatabase
import org.json.JSONArray
import org.json.JSONObject

class BackupRepository(private val db: AppDatabase) {

    suspend fun buildExportJson(): String {

        val root = JSONObject()

        val contacts = db.contactSlotDao().getAllDirect()
        val contactsArray = JSONArray()
        contacts.forEach {
            val obj = JSONObject()
            obj.put("slotId", it.slotId)
            obj.put("label", it.label)
            obj.put("phoneNumber", it.phoneNumber)
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
            obj.put("date", it.date)
            obj.put("scheduledTime", it.scheduledTime)
            logsArray.put(obj)
        }
        root.put("careLogs", logsArray)

        val emergency = db.emergencyInfoDao().getDirect()
        if (emergency != null) {
            val obj = JSONObject()
            obj.put("data", emergency.toString())
            root.put("emergencyInfo", obj)
        }

        val settings = db.appSettingsDao().getDirect()
        if (settings != null) {
            val obj = JSONObject()
            obj.put("data", settings.toString())
            root.put("settings", obj)
        }

        return root.toString()
    }
}