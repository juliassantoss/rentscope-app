package com.example.rentscope.data.local

import android.content.Context
import android.content.SharedPreferences

data class RentPreferenceData(
    val rendaMin: Float,
    val rendaMax: Float
)

object RentPreferenceManager {

    private const val PREFS_NAME = "rentscope_rent_preferences"
    private const val KEY_RENDA_MIN = "renda_min"
    private const val KEY_RENDA_MAX = "renda_max"

    private var prefs: SharedPreferences? = null

    /**
     * Initializes the preferences storage used for default rent preferences.
     *
     * @param context Application context used to access shared preferences.
     */
    fun init(context: Context) {
        if (prefs == null) {
            prefs = context.applicationContext.getSharedPreferences(
                PREFS_NAME,
                Context.MODE_PRIVATE
            )
        }
    }

    /**
     * Persists the preferred normalized rent interval selected by the user.
     *
     * @param rendaMin Lowest preferred normalized rent value.
     * @param rendaMax Highest preferred normalized rent value.
     */
    fun save(rendaMin: Float, rendaMax: Float) {
        prefs?.edit()
            ?.putFloat(KEY_RENDA_MIN, rendaMin)
            ?.putFloat(KEY_RENDA_MAX, rendaMax)
            ?.apply()
    }

    /**
     * Returns the saved normalized rent interval when available.
     *
     * @return Stored rent preference range or `null` when no preference was saved.
     */
    fun get(): RentPreferenceData? {
        val preferences = prefs ?: return null

        if (!preferences.contains(KEY_RENDA_MIN) || !preferences.contains(KEY_RENDA_MAX)) {
            return null
        }

        return RentPreferenceData(
            rendaMin = preferences.getFloat(KEY_RENDA_MIN, 0f),
            rendaMax = preferences.getFloat(KEY_RENDA_MAX, 20f)
        )
    }
}
