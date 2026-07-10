package com.quietlogic.allisok.ui.backup

import com.quietlogic.allisok.data.backup.BackupConstants
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.LocalTime

object BackupJsonValidator {

    fun validate(json: String): String? {
        return try {
            val root = JSONObject(json.trim())

            if (!root.has("backupVersion") || root.isNull("backupVersion")) {
                throw ValidationException("Missing backupVersion")
            }

            val backupVersion = root.getInt("backupVersion")
            if (backupVersion != BackupConstants.BACKUP_VERSION) {
                throw ValidationException("Unsupported backupVersion: $backupVersion")
            }

            rejectLegacyToStringFormat(root)

            requireArray(root, "contacts")
            requireArray(root, "careItems")
            requireArray(root, "careTimes")
            requireArray(root, "careLogs")
            requireObject(root, "sharedPreferences")

            validateContacts(root.getJSONArray("contacts"))
            validateCareItems(root.getJSONArray("careItems"))
            validateCareTimes(root.getJSONArray("careTimes"))
            validateCareLogs(root.getJSONArray("careLogs"))

            if (root.has("emergencyInfo") && !root.isNull("emergencyInfo")) {
                validateEmergencyInfo(root.getJSONObject("emergencyInfo"))
            }

            if (root.has("settings") && !root.isNull("settings")) {
                validateSettings(root.getJSONObject("settings"))
            }

            validateSharedPreferences(root.getJSONObject("sharedPreferences"))

            null
        } catch (error: ValidationException) {
            error.message
        } catch (_: Exception) {
            "Invalid backup file"
        }
    }

    private fun rejectLegacyToStringFormat(root: JSONObject) {
        if (root.has("emergencyInfo") && !root.isNull("emergencyInfo")) {
            val section = root.getJSONObject("emergencyInfo")
            if (section.has("data")) {
                throw ValidationException("Unsupported legacy emergencyInfo format")
            }
        }

        if (root.has("settings") && !root.isNull("settings")) {
            val section = root.getJSONObject("settings")
            if (section.has("data")) {
                throw ValidationException("Unsupported legacy settings format")
            }
        }
    }

    private fun requireArray(root: JSONObject, key: String): JSONArray {
        if (!root.has(key) || root.isNull(key)) {
            throw ValidationException("Missing required section: $key")
        }
        return root.getJSONArray(key)
    }

    private fun requireObject(root: JSONObject, key: String): JSONObject {
        if (!root.has(key) || root.isNull(key)) {
            throw ValidationException("Missing required section: $key")
        }
        return root.getJSONObject(key)
    }

    private fun validateContacts(array: JSONArray) {
        for (index in 0 until array.length()) {
            val item = array.getJSONObject(index)
            item.getInt("slotId")
            item.getString("label")
            if (!item.isNull("phoneNumber")) {
                item.getString("phoneNumber")
            }
            item.getString("iconType")
        }
    }

    private fun validateCareItems(array: JSONArray) {
        for (index in 0 until array.length()) {
            val item = array.getJSONObject(index)
            item.getLong("id")
            item.getString("name")
            item.getString("instruction")
            LocalDate.parse(item.getString("startDate"))
            LocalDate.parse(item.getString("endDate"))
            item.getString("repeatType")
            item.getBoolean("isArchived")
        }
    }

    private fun validateCareTimes(array: JSONArray) {
        for (index in 0 until array.length()) {
            val item = array.getJSONObject(index)
            item.getLong("careItemId")
            LocalTime.parse(item.getString("time"))
        }
    }

    private fun validateCareLogs(array: JSONArray) {
        for (index in 0 until array.length()) {
            val item = array.getJSONObject(index)
            item.getLong("careItemId")
            LocalDate.parse(item.getString("date"))
            LocalTime.parse(item.getString("scheduledTime"))
        }
    }

    private fun validateEmergencyInfo(section: JSONObject) {
        requireNullableString(section, "bloodType")
        requireNullableString(section, "allergies")
        requireNullableString(section, "conditions")
        requireNullableString(section, "notes")
    }

    private fun validateSettings(section: JSONObject) {
        section.getInt("id")
        requireNullableString(section, "appPinHash")
        requireNullableString(section, "adminPinHash")
        if (!section.isNull("trialStartTimestamp")) {
            section.getLong("trialStartTimestamp")
        }
        section.getBoolean("isTrialUsed")
        section.getString("languageCode")
        section.getString("dateFormat")
    }

    private fun validateSharedPreferences(section: JSONObject) {
        val pinPrefs = section.getJSONObject(BackupConstants.PREFS_PIN)
        pinPrefs.getBoolean(BackupConstants.KEY_USER_PIN_ENABLED)
        requireNullableString(pinPrefs, BackupConstants.KEY_USER_PIN_HASH)
        pinPrefs.getBoolean(BackupConstants.KEY_ADMIN_PIN_ENABLED)
        requireNullableString(pinPrefs, BackupConstants.KEY_ADMIN_PIN_HASH)

        val appPrefs = section.getJSONObject(BackupConstants.PREFS_APP_SETTINGS)
        val language = appPrefs.getString(BackupConstants.KEY_APP_LANGUAGE)
        if (language.isNullOrBlank()) {
            throw ValidationException("Missing app_language")
        }
    }

    private fun requireNullableString(section: JSONObject, key: String) {
        if (!section.has(key)) {
            throw ValidationException("Missing field: $key")
        }
        if (!section.isNull(key)) {
            section.getString(key)
        }
    }

    private class ValidationException(message: String) : Exception(message)
}
