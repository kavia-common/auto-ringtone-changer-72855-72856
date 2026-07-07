package org.example.app.telephony

import android.content.Context
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.CallLog
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Registers a ContentObserver against CallLog.Calls to detect updates in call history.
 *
 * This is used as a practical cross-version "call ended" signal: when the call log
 * changes after OFFHOOK -> IDLE, we trigger ringtone selection.
 */
object CallLogObserverManager {
    private var observer: ContentObserver? = null
    private val running = AtomicBoolean(false)

    fun start(context: Context, settingsRepository: SettingsRepository) {
        if (running.getAndSet(true)) return

        val appContext = context.applicationContext
        val handler = Handler(Looper.getMainLooper())

        observer = object : ContentObserver(handler) {
            override fun onChange(selfChange: Boolean) {
                super.onChange(selfChange)

                // Trigger only if we recently observed a call end via phone state.
                if (PhoneStateTracker.consumePendingCallEnd()) {
                    maybeHandleCallEnd(appContext, settingsRepository)
                }
            }
        }

        appContext.contentResolver.registerContentObserver(
            CallLog.Calls.CONTENT_URI,
            true,
            observer!!
        )
    }

    fun stop(context: Context) {
        if (!running.getAndSet(false)) return
        val appContext = context.applicationContext
        observer?.let {
            try {
                appContext.contentResolver.unregisterContentObserver(it)
            } catch (_: Throwable) {
                // best-effort cleanup
            }
        }
        observer = null
    }

    private fun maybeHandleCallEnd(context: Context, settingsRepository: SettingsRepository) {
        when (settingsRepository.ringtoneMode()) {
            SettingsRepository.RingtoneMode.SYSTEM_DEFAULT_ONLY -> {
                // Explicitly do nothing.
            }
            SettingsRepository.RingtoneMode.RANDOM -> {
                RandomRingtoneChanger.trySetRandomRingtone(context)
            }
        }
    }
}
