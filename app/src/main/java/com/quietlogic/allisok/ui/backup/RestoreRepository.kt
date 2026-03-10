package com.quietlogic.allisok.data.backup

import com.quietlogic.allisok.data.local.db.AppDatabase
import com.quietlogic.allisok.data.local.entity.CareItemEntity
import com.quietlogic.allisok.data.local.entity.CareLogEntity
import com.quietlogic.allisok.data.local.entity.CareTimeEntity
import com.quietlogic.allisok.data.local.entity.ContactSlotEntity
import org.json.JSONObject
import java.time.LocalDate
import java.time.LocalTime

class RestoreRepository(private val db: AppDatabase) {

    suspend fun restoreFromJson(json: String) {

        val root = JSONObject(json)

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

        if (root.has("emergencyInfo")) {
            db.emergencyInfoDao().clear()
        }

        if (root.has("settings")) {
            db.appSettingsDao().clear()
        }
    }
}