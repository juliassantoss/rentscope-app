package com.example.rentscope.data.local

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import java.util.Locale

object LanguageManager {

    private const val PREFS_NAME = "rentscope_language_prefs"
    private const val KEY_LANGUAGE = "selected_language"

    private var prefs: SharedPreferences? = null

    fun init(context: Context) {
        if (prefs == null) {
            prefs = context.applicationContext.getSharedPreferences(
                PREFS_NAME,
                Context.MODE_PRIVATE
            )
        }
    }

    fun getSavedLanguage(context: Context): String {
        init(context)
        return prefs?.getString(KEY_LANGUAGE, "pt") ?: "pt"
    }

    fun setLanguage(activity: Activity, languageCode: String) {
        init(activity)

        prefs?.edit()
            ?.putString(KEY_LANGUAGE, languageCode)
            ?.apply()

        applyLanguage(activity, languageCode)
        activity.recreate()
    }

    fun applySavedLanguage(context: Context) {
        val languageCode = getSavedLanguage(context)
        applyLanguage(context, languageCode)
    }

    fun applyLanguage(context: Context, languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val resources = context.resources
        val config = Configuration(resources.configuration)
        config.setLocale(locale)

        resources.updateConfiguration(config, resources.displayMetrics)
    }
}