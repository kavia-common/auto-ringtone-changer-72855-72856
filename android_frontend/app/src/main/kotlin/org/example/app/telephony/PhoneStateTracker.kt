package org.example.app.telephony

import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

/**
 * Keeps a minimal in-memory model of call state to help infer a "call ended" event.
 *
 * This deliberately avoids holding long-lived resources; receivers update this
 * state and the CallLog observer consumes it.
 */
object PhoneStateTracker {

    // 0 = unknown/idle, 1 = ringing, 2 = offhook
    private val lastState = AtomicInteger(0)
    private val pendingCallEnd = AtomicBoolean(false)

    fun onRinging() {
        lastState.set(1)
    }

    fun onOffhook() {
        lastState.set(2)
    }

    fun onIdle() {
        // If we were previously in an active call state, mark a pending end.
        val prev = lastState.getAndSet(0)
        if (prev == 2) {
            pendingCallEnd.set(true)
        }
    }

    fun consumePendingCallEnd(): Boolean {
        return pendingCallEnd.getAndSet(false)
    }
}
