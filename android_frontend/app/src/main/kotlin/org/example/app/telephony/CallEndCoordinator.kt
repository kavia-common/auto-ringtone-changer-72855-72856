package org.example.app.telephony

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.provider.Settings

object CallEndCoordinator {

    // PUBLIC_INTERFACE
    fun ensureStartedOrStopped(context: Context, settingsRepository: SettingsRepository) {
        /** Starts or stops call-end detection based on the user's settings and current permissions. */
        if (!settingsRepository.isEnabled()) {
            CallLogObserverManager.stop(context)
            return
        }

        val phoneGranted = hasPermission(context, Manifest.permission.READ_PHONE_STATE)
        val callLogGranted = hasPermission(context, Manifest.permission.READ_CALL_LOG)
        val writeSettingsGranted = Settings.System.canWrite(context)

        if (phoneGranted && callLogGranted && writeSettingsGranted) {
            CallLogObserverManager.start(context, settingsRepository)
        } else {
            // If missing permissions, we do not run observers (prevents crashes and wasted wakeups).
            CallLogObserverManager.stop(context)
        }
    }

    private fun hasPermission(context: Context, permission: String): Boolean {
        return context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
    }
}
