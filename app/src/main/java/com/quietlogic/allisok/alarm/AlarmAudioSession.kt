package com.quietlogic.allisok.alarm

import android.os.SystemClock
import android.util.Log

/**
 * Process-local guard to ensure only one alarm ringtone session plays for a given
 * scheduled timestamp key.
 *
 * Strict rules:
 * - Ownership is deterministic and based solely on the provided [key].
 * - Same-time alarms must resolve to the same [key] so only one UI plays audio.
 */
object AlarmAudioSession {

    private val lock = Any()
    private var activeKey: String? = null
    private var acquiredAt: Long = 0L

    private const val STALE_TIMEOUT_MS = 2 * 60 * 1000L  // 2 minutes

    /**
     * Attempts to acquire audio ownership for [key].
     *
     * Returns true ONLY if no active session exists for that key (or any other key).
     * If already active, returns false immediately.
     */
    fun tryAcquire(key: String): Boolean {
        synchronized(lock) {
            val now = SystemClock.elapsedRealtime()
            if (activeKey != null && (now - acquiredAt) > STALE_TIMEOUT_MS) {
                Log.d("AllIsOK", "STALE SESSION CLEARED: $activeKey")
                activeKey = null
                acquiredAt = 0L
            }
            if (activeKey != null) {
                Log.d("AllIsOK", "AUDIO SKIPPED: $key")
                return false
            }
            activeKey = key
            acquiredAt = now
            Log.d("AllIsOK", "AUDIO ACQUIRED: $key")
            return true
        }
    }

    /**
     * Releases the session ONLY if [key] currently owns it.
     * Safe against double-release and mismatched callers.
     */
    fun release(key: String) {
        if (key.isBlank()) return
        synchronized(lock) {
            if (activeKey == key) {
                activeKey = null
                acquiredAt = 0L
            }
        }
    }
}

