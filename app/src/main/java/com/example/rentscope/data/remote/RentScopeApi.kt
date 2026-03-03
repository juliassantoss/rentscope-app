package com.example.rentscope.data.remote

import com.example.rentscope.data.remote.dto.PaisDto
import retrofit2.http.GET

interface RentScopeApi {
    @GET("paises")
    suspend fun listarPaises(): List<PaisDto>
}