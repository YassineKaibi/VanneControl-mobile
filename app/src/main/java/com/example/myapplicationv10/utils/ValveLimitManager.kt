package com.example.myapplicationv10.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Manages the valve limit setting using encrypted SharedPreferences.
 * This is a singleton that provides methods to get/set the maximum number of valves
 * a user can control (0-8 range).
 */
class ValveLimitManager private constructor(context: Context) {

    private val sharedPreferences: SharedPreferences

    init {
        val masterKey = MasterKey.Builder(context.applicationContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        sharedPreferences = EncryptedSharedPreferences.create(
            context.applicationContext,
            Constants.PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    companion object {
        private const val DEFAULT_VALVE_LIMIT = 0

        @Volatile
        private var instance: ValveLimitManager? = null

        fun getInstance(context: Context): ValveLimitManager {
            return instance ?: synchronized(this) {
                instance ?: ValveLimitManager(context).also { instance = it }
            }
        }
    }

    /**
     * Get the current valve limit setting.
     * @return The valve limit (0-8), default is 0
     */
    fun getValveLimit(): Int {
        return sharedPreferences.getInt(Constants.PREFS_VALVE_LIMIT, DEFAULT_VALVE_LIMIT)
    }

    /**
     * Set the valve limit.
     * @param limit The number of valves to enable (0-8)
     */
    fun setValveLimit(limit: Int) {
        require(limit in 0..Constants.PISTONS_PER_DEVICE) {
            "Valve limit must be between 0 and ${Constants.PISTONS_PER_DEVICE}"
        }
        sharedPreferences.edit().putInt(Constants.PREFS_VALVE_LIMIT, limit).apply()
    }

    /**
     * Check if a specific valve is enabled based on the current limit.
     * @param valveNumber The valve number to check (1-8)
     * @return true if the valve is within the enabled range, false otherwise
     */
    fun isValveEnabled(valveNumber: Int): Boolean {
        val limit = getValveLimit()
        return valveNumber in 1..limit
    }
}
