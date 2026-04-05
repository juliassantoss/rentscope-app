package com.example.rentscope.data.repository

import com.example.rentscope.data.remote.ApiClient
import com.example.rentscope.data.remote.RentScopeApi
import com.example.rentscope.data.remote.dto.MunicipioDto

object MunicipioRepository {

    private val api = ApiClient.retrofit.create(RentScopeApi::class.java)

    suspend fun getMunicipios(): Result<List<MunicipioDto>> {
        return try {
            val response = api.getMunicipios()

            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Erro ao carregar municípios"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}