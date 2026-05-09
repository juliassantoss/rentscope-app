package com.example.rentscope.data.repository

import com.example.rentscope.data.remote.RentScopeApi
import com.example.rentscope.data.remote.dto.PaisDto

class PaisesRepository(
    private val api: RentScopeApi
) {
    /**
     * Loads the available countries from the API and falls back to the currently
     * supported built-in list when the request is unavailable.
     *
     * @return Remote countries when available, otherwise the local fallback list.
     */
    suspend fun listarPaises(): List<PaisDto> {
        return try {
            val remoteCountries = api.listarPaises()
            if (remoteCountries.isNotEmpty()) {
                remoteCountries
            } else {
                fallbackCountries()
            }
        } catch (_: Exception) {
            fallbackCountries()
        }
    }

    private fun fallbackCountries(): List<PaisDto> {
        return listOf(
            PaisDto(
                codigo = "PT",
                nome = "Portugal"
            )
        )
    }
}
