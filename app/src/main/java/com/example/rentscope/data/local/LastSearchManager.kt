package com.example.rentscope.data.local

import android.content.Context
import android.content.SharedPreferences

data class LastSearchData(
    val countryCode: String,
    val countryName: String,
    val rendaMin: Float?,
    val rendaMax: Float?,
    val pesoRenda: Float,
    val pesoEscolas: Float,
    val pesoHospitais: Float,
    val pesoCriminalidade: Float
)

object LastSearchManager {

    private const val PREFS_NAME = "rentscope_last_search_prefs"

    private const val KEY_COUNTRY_CODE = "country_code"
    private const val KEY_COUNTRY_NAME = "country_name"
    private const val KEY_HAS_RENDA_MIN = "has_renda_min"
    private const val KEY_RENDA_MIN = "renda_min"
    private const val KEY_HAS_RENDA_MAX = "has_renda_max"
    private const val KEY_RENDA_MAX = "renda_max"
    private const val KEY_PESO_RENDA = "peso_renda"
    private const val KEY_PESO_ESCOLAS = "peso_escolas"
    private const val KEY_PESO_HOSPITAIS = "peso_hospitais"
    private const val KEY_PESO_CRIMINALIDADE = "peso_criminalidade"

    private var prefs: SharedPreferences? = null

    fun init(context: Context) {
        if (prefs == null) {
            prefs = context.applicationContext.getSharedPreferences(
                PREFS_NAME,
                Context.MODE_PRIVATE
            )
        }
    }

    fun save(
        countryCode: String,
        countryName: String,
        rendaMin: Float?,
        rendaMax: Float?,
        pesoRenda: Float,
        pesoEscolas: Float,
        pesoHospitais: Float,
        pesoCriminalidade: Float
    ) {
        prefs?.edit()
            ?.putString(KEY_COUNTRY_CODE, countryCode)
            ?.putString(KEY_COUNTRY_NAME, countryName)
            ?.putBoolean(KEY_HAS_RENDA_MIN, rendaMin != null)
            ?.putFloat(KEY_RENDA_MIN, rendaMin ?: 0f)
            ?.putBoolean(KEY_HAS_RENDA_MAX, rendaMax != null)
            ?.putFloat(KEY_RENDA_MAX, rendaMax ?: 0f)
            ?.putFloat(KEY_PESO_RENDA, pesoRenda)
            ?.putFloat(KEY_PESO_ESCOLAS, pesoEscolas)
            ?.putFloat(KEY_PESO_HOSPITAIS, pesoHospitais)
            ?.putFloat(KEY_PESO_CRIMINALIDADE, pesoCriminalidade)
            ?.apply()
    }

    fun get(): LastSearchData? {
        val countryCode = prefs?.getString(KEY_COUNTRY_CODE, null) ?: return null
        val countryName = prefs?.getString(KEY_COUNTRY_NAME, null) ?: return null

        val hasRendaMin = prefs?.getBoolean(KEY_HAS_RENDA_MIN, false) ?: false
        val hasRendaMax = prefs?.getBoolean(KEY_HAS_RENDA_MAX, false) ?: false

        val rendaMin = if (hasRendaMin) prefs?.getFloat(KEY_RENDA_MIN, 0f) else null
        val rendaMax = if (hasRendaMax) prefs?.getFloat(KEY_RENDA_MAX, 0f) else null

        return LastSearchData(
            countryCode = countryCode,
            countryName = countryName,
            rendaMin = rendaMin,
            rendaMax = rendaMax,
            pesoRenda = prefs?.getFloat(KEY_PESO_RENDA, 1f) ?: 1f,
            pesoEscolas = prefs?.getFloat(KEY_PESO_ESCOLAS, 1f) ?: 1f,
            pesoHospitais = prefs?.getFloat(KEY_PESO_HOSPITAIS, 1f) ?: 1f,
            pesoCriminalidade = prefs?.getFloat(KEY_PESO_CRIMINALIDADE, 1f) ?: 1f
        )
    }

    fun clear() {
        prefs?.edit()?.clear()?.apply()
    }
}