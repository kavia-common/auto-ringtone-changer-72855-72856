package org.example.app.telephony

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != "android.intent.action.BOOT_COMPLETED") return
        val repo = SettingsRepository(context)
        CallEndCoordinator.ensureStartedOrStopped(context, repo)
    }
}
