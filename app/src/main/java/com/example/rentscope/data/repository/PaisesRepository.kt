package com.example.rentscope.data.repository

import com.example.rentscope.data.remote.RentScopeApi
import com.example.rentscope.data.remote.dto.PaisDto

class PaisesRepository(
    private val api: RentScopeApi
) {
    suspend fun listarPaises(): List<PaisDto> = api.listarPaises()
}