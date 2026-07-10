package com.quietlogic.allisok.data.backup

import android.content.Context
import com.quietlogic.allisok.security.AdminSession
import com.quietlogic.allisok.security.UserSession
import org.json.JSONObject

object BackupPreferencesSnapshot {

    fun exportToJson(context: Context): JSONObject {
        val root = JSONObject()

        val pinPrefs = context.getSharedPreferences(BackupConstants.PREFS_PIN, Context.MODE_PRIVATE)
        val pinObject = JSONObject()
        pinObject.put(BackupConstants.KEY_USER_PIN_ENABLED, pinPrefs.getBoolean(BackupConstants.KEY_USER_PIN_ENABLED, false))
        putNullableString(pinObject, BackupConstants.KEY_USER_PIN_HASH, pinPrefs.getString(BackupConstants.KEY_USER_PIN_HASH, null))
        pinObject.put(BackupConstants.KEY_ADMIN_PIN_ENABLED, pinPrefs.getBoolean(BackupConstants.KEY_ADMIN_PIN_ENABLED, false))
        putNullableString(pinObject, BackupConstants.KEY_ADMIN_PIN_HASH, pinPrefs.getString(BackupConstants.KEY_ADMIN_PIN_HASH, null))
        root.put(BackupConstants.PREFS_PIN, pinObject)

        val appPrefs = context.getSharedPreferences(BackupConstants.PREFS_APP_SETTINGS, Context.MODE_PRIVATE)
        val appSettingsObject = JSONObject()
        appSettingsObject.put(
            BackupConstants.KEY_APP_LANGUAGE,
            appPrefs.getString(BackupConstants.KEY_APP_LANGUAGE, "en") ?: "en"
        )
        root.put(BackupConstants.PREFS_APP_SETTINGS, appSettingsObject)

        return root
    }

    fun restore(context: Context, sharedPreferencesSection: JSONObject) {
        val pinSection = sharedPreferencesSection.getJSONObject(BackupConstants.PREFS_PIN)
        val pinPrefs = context.getSharedPreferences(BackupConstants.PREFS_PIN, Context.MODE_PRIVATE)
        val pinEditor = pinPrefs.edit()
        pinEditor.putBoolean(
            BackupConstants.KEY_USER_PIN_ENABLED,
            pinSection.getBoolean(BackupConstants.KEY_USER_PIN_ENABLED)
        )
        if (pinSection.isNull(BackupConstants.KEY_USER_PIN_HASH)) {
            pinEditor.remove(BackupConstants.KEY_USER_PIN_HASH)
        } else {
            pinEditor.putString(BackupConstants.KEY_USER_PIN_HASH, pinSection.getString(BackupConstants.KEY_USER_PIN_HASH))
        }
        pinEditor.putBoolean(
            BackupConstants.KEY_ADMIN_PIN_ENABLED,
            pinSection.getBoolean(BackupConstants.KEY_ADMIN_PIN_ENABLED)
        )
        if (pinSection.isNull(BackupConstants.KEY_ADMIN_PIN_HASH)) {
            pinEditor.remove(BackupConstants.KEY_ADMIN_PIN_HASH)
        } else {
            pinEditor.putString(BackupConstants.KEY_ADMIN_PIN_HASH, pinSection.getString(BackupConstants.KEY_ADMIN_PIN_HASH))
        }
        pinEditor.commit()

        val appSection = sharedPreferencesSection.getJSONObject(BackupConstants.PREFS_APP_SETTINGS)
        val appPrefs = context.getSharedPreferences(BackupConstants.PREFS_APP_SETTINGS, Context.MODE_PRIVATE)
        appPrefs.edit()
            .putString(BackupConstants.KEY_APP_LANGUAGE, appSection.getString(BackupConstants.KEY_APP_LANGUAGE))
            .commit()

        context.getSharedPreferences(BackupConstants.PREFS_USER_SESSION, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(BackupConstants.KEY_SESSION_ACTIVE, false)
            .apply()

        UserSession.stop(context)
        AdminSession.stop()
    }

    private fun putNullableString(objectJson: JSONObject, key: String, value: String?) {
        if (value == null) {
            objectJson.put(key, JSONObject.NULL)
        } else {
            objectJson.put(key, value)
        }
    }
}
