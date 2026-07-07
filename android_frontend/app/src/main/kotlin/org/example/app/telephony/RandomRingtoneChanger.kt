package org.example.app.telephony

import android.content.Context
import android.media.RingtoneManager
import android.net.Uri
import android.provider.Settings
import kotlin.random.Random

object RandomRingtoneChanger {

    /**
     * Attempts to set a random ringtone.
     *
     * Requires the WRITE_SETTINGS special permission to be granted, otherwise this
     * method will return without changing anything.
     */
    fun trySetRandomRingtone(context: Context) {
        if (!Settings.System.canWrite(context)) return

        val manager = RingtoneManager(context)
        manager.setType(RingtoneManager.TYPE_RINGTONE)

        val cursor = manager.cursor ?: return
        cursor.use {
            val count = it.count
            if (count <= 0) return

            val randomPos = Random.nextInt(count)
            if (!it.moveToPosition(randomPos)) return

            val uri: Uri = manager.getRingtoneUri(randomPos) ?: return

            // Set as the system default ringtone
            RingtoneManager.setActualDefaultRingtoneUri(
                context,
                RingtoneManager.TYPE_RINGTONE,
                uri
            )
        }
    }
}
