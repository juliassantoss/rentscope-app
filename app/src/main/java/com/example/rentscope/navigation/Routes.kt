package com.example.rentscope.navigation

import android.net.Uri

object Routes {
    const val HOME = "home"

    const val COUNTRIES_WITH_ARG = "countries/{continent}"
    fun countries(continent: String) = "countries/${Uri.encode(continent)}"

    const val MAP =
        "map/{countryCode}/{countryName}" +
                "?rendaMin={rendaMin}" +
                "&rendaMax={rendaMax}" +
                "&pesoRenda={pesoRenda}" +
                "&pesoEscolas={pesoEscolas}" +
                "&pesoHospitais={pesoHospitais}" +
                "&pesoCriminalidade={pesoCriminalidade}" +
                "&saveToHistory={saveToHistory}"

    fun map(
        countryCode: String,
        countryName: String,
        rendaMin: Float? = null,
        rendaMax: Float? = null,
        pesoRenda: Float = 1f,
        pesoEscolas: Float = 1f,
        pesoHospitais: Float = 1f,
        pesoCriminalidade: Float = 1f,
        saveToHistory: Boolean = true
    ): String {
        val encodedCountryCode = Uri.encode(countryCode)
        val encodedCountryName = Uri.encode(countryName)
        val rendaMinValue = rendaMin?.toString() ?: ""
        val rendaMaxValue = rendaMax?.toString() ?: ""

        return "map/$encodedCountryCode/$encodedCountryName" +
                "?rendaMin=$rendaMinValue" +
                "&rendaMax=$rendaMaxValue" +
                "&pesoRenda=$pesoRenda" +
                "&pesoEscolas=$pesoEscolas" +
                "&pesoHospitais=$pesoHospitais" +
                "&pesoCriminalidade=$pesoCriminalidade" +
                "&saveToHistory=$saveToHistory"
    }

    const val FILTERS = "filters/{countryCode}/{countryName}"
    fun filters(countryCode: String, countryName: String): String {
        return "filters/${Uri.encode(countryCode)}/${Uri.encode(countryName)}"
    }

    const val LOGIN = "login"
    const val NEW_ACCOUNT = "new_account"

    const val LANGUAGE = "language"
    const val RESULTS = "results"
    const val HISTORY = "history"
    const val FAVORITES = "favorites"
    const val PRICE_HISTORY = "price_history"

    const val DEBUG_PAISES = "debug_paises"
    const val AI_ASSISTANT = "ai_assistant"
}