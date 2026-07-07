package org.example.app.telephony

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager

class PhoneStateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // TelephonyManager.EXTRA_STATE can be RINGING/OFFHOOK/IDLE
        val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE) ?: return
        when (state) {
            TelephonyManager.EXTRA_STATE_RINGING -> PhoneStateTracker.onRinging()
            TelephonyManager.EXTRA_STATE_OFFHOOK -> PhoneStateTracker.onOffhook()
            TelephonyManager.EXTRA_STATE_IDLE -> PhoneStateTracker.onIdle()
        }
    }
}
