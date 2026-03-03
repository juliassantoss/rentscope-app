package com.example.rentscope.navigation

object Routes {
    const val HOME = "home"
    const val COUNTRIES_WITH_ARG = "countries/{continent}"
    fun countries(continent: String) = "countries/$continent"

    const val MAP = "map/{countryCode}/{countryName}"

    fun map(countryCode: String, countryName: String): String {
        return "map/$countryCode/$countryName"
    }

    const val FILTERS = "filters/{countryCode}/{countryName}"
    fun filters(countryCode: String, countryName: String) = "filters/$countryCode/$countryName"

    const val LOGIN = "login"

    const val NEW_ACCOUNT = "new_account"
    // Footer
    const val LANGUAGE = "language"
    const val RESULTS = "results"

    const val DEBUG_PAISES = "debug_paises"
}
