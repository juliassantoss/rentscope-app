package com.example.rentscope.data.repository

import com.example.rentscope.data.remote.ApiClient
import com.example.rentscope.data.remote.RentScopeApi
import com.example.rentscope.data.remote.dto.pricehistory.PriceHistoryDto

object PriceHistoryRepository {

    private val api = ApiClient.retrofit.create(RentScopeApi::class.java)

    suspend fun getHistorico(codigo: Int): Result<List<PriceHistoryDto>> {
        return try {
            val response = api.getHistoricoRenda(codigo)

            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Erro ao carregar histórico de preços"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}