package com.example.util

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.security.MessageDigest

class PasscodeManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("trading_passcode_prefs", Context.MODE_PRIVATE)

    private val _isLocked = MutableStateFlow(hasPasscode())
    val isLocked: StateFlow<Boolean> = _isLocked.asStateFlow()

    fun hasPasscode(): Boolean {
        return prefs.getString(KEY_PASSCODE_HASH, null) != null
    }

    fun verifyPasscode(input: String): Boolean {
        val storedHash = prefs.getString(KEY_PASSCODE_HASH, null) ?: return false
        val inputHash = hashPasscode(input)
        val matches = storedHash == inputHash
        if (matches) {
            _isLocked.value = false
        }
        return matches
    }

    fun setPasscode(passcode: String): Boolean {
        if (passcode.length != 4 || !passcode.all { it.isDigit() }) return false
        val hash = hashPasscode(passcode)
        prefs.edit().putString(KEY_PASSCODE_HASH, hash).apply()
        _isLocked.value = false
        return true
    }

    fun lockApp() {
        if (hasPasscode()) {
            _isLocked.value = true
        }
    }

    fun unlockWithoutCheckForFirstSetup() {
        _isLocked.value = false
    }

    private fun hashPasscode(pin: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(pin.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    companion object {
        private const val KEY_PASSCODE_HASH = "passcode_hash"

        @Volatile
        private var instance: PasscodeManager? = null

        fun getInstance(context: Context): PasscodeManager {
            return instance ?: synchronized(this) {
                instance ?: PasscodeManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
