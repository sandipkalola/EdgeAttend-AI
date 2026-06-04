package com.example.data.pref

import android.content.Context
import android.content.SharedPreferences
import java.util.UUID

class AppPreferences(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        "edge_attend_ai_preferences",
        Context.MODE_PRIVATE
    )

    companion object {
        private const val KEY_PIN = "secure_pin_hash"
        private const val KEY_DEVICE_ID = "offline_device_id"
        private const val KEY_LOCATION_SIMULATED = "gps_location_simulated"
        private const val KEY_LOCATION_NAME = "gps_location_name"
        private const val KEY_LIVENESS_BYPASS = "liveness_bypass_for_dev"
    }

    init {
        // Initialize default settings
        if (getSecurePin() == null) {
            setSecurePin("1234") // Standard default enterprise PIN
        }
        if (getDeviceId().isEmpty()) {
            val randomId = "EA-" + UUID.randomUUID().toString().uppercase().take(8)
            setDeviceId(randomId)
        }
        if (getSimulatedLocationCoords().isEmpty()) {
            setSimulatedLocation("28.6139, 77.2090", "Delhi Corporate Office (HQ)")
        }
    }

    fun getSecurePin(): String? {
        return prefs.getString(KEY_PIN, "1234")
    }

    fun setSecurePin(pin: String) {
        prefs.edit().putString(KEY_PIN, pin).apply()
    }

    fun getDeviceId(): String {
        return prefs.getString(KEY_DEVICE_ID, "") ?: "EA-OFFLINE-01"
    }

    fun setDeviceId(id: String) {
        prefs.edit().putString(KEY_DEVICE_ID, id).apply()
    }

    fun getSimulatedLocationCoords(): String {
        return prefs.getString(KEY_LOCATION_SIMULATED, "28.6139, 77.2090") ?: "28.6139, 77.2090"
    }

    fun getSimulatedLocationName(): String {
        return prefs.getString(KEY_LOCATION_NAME, "Delhi HQ Office") ?: "Delhi HQ Office"
    }

    fun setSimulatedLocation(coords: String, name: String) {
        prefs.edit()
            .putString(KEY_LOCATION_SIMULATED, coords)
            .putString(KEY_LOCATION_NAME, name)
            .apply()
    }

    fun isLivenessBypassEnabled(): Boolean {
        return prefs.getBoolean(KEY_LIVENESS_BYPASS, false)
    }

    fun setLivenessBypass(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_LIVENESS_BYPASS, enabled).apply()
    }
}
