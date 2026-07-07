package org.example.app.telephony

import android.content.Context
import android.content.SharedPreferences

class SettingsRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    enum class RingtoneMode {
        RANDOM,
        SYSTEM_DEFAULT_ONLY
    }

    fun isEnabled(): Boolean = prefs.getBoolean(KEY_ENABLED, false)

    fun setEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_ENABLED, enabled).apply()
    }

    fun ringtoneMode(): RingtoneMode {
        val raw = prefs.getString(KEY_RINGTONE_MODE, RingtoneMode.RANDOM.name) ?: RingtoneMode.RANDOM.name
        return try {
            RingtoneMode.valueOf(raw)
        } catch (_: IllegalArgumentException) {
            RingtoneMode.RANDOM
        }
    }

    fun setRingtoneMode(mode: RingtoneMode) {
        prefs.edit().putString(KEY_RINGTONE_MODE, mode.name).apply()
    }

    companion object {
        private const val PREFS_NAME = "auto_ringtone_changer_settings"
        private const val KEY_ENABLED = "enabled"
        private const val KEY_RINGTONE_MODE = "ringtone_mode"
    }
}
