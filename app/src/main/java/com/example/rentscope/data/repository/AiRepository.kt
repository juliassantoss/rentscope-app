package com.example.rentscope.data.repository

import com.example.rentscope.data.remote.ApiClient
import com.example.rentscope.data.remote.RentScopeApi
import com.example.rentscope.data.remote.dto.ai.AiQuestionRequest
import com.example.rentscope.data.remote.dto.ai.AiQuestionResponse

object AiRepository {

    private val api = ApiClient.retrofit.create(RentScopeApi::class.java)

    suspend fun perguntar(request: AiQuestionRequest): Result<AiQuestionResponse> {
        return try {
            val response = api.perguntarIa(request)

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body)
                } else {
                    Result.failure(Exception("Resposta vazia da IA"))
                }
            } else {
                Result.failure(Exception("Erro ao consultar IA"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}