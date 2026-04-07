package com.example.rentscope.data.repository

import com.example.rentscope.data.remote.ApiClient
import com.example.rentscope.data.remote.RentScopeApi
import com.example.rentscope.data.remote.dto.score.ScoreFiltroRequestDto
import com.example.rentscope.data.remote.dto.score.ScoreMunicipioDto
import org.json.JSONObject

object ScoreRepository {

    private val api: RentScopeApi = ApiClient.retrofit.create(RentScopeApi::class.java)

    suspend fun aplicarFiltros(
        busca: String? = null,
        rendaMin: Float? = null,
        rendaMax: Float? = null,
        pesoRenda: Float = 1f,
        pesoEscolas: Float = 1f,
        pesoHospitais: Float = 1f,
        pesoCriminalidade: Float = 1f,
        limite: Int = 200
    ): Result<List<ScoreMunicipioDto>> {
        return try {
            val request = ScoreFiltroRequestDto(
                busca = busca ?: "",
                renda_min = rendaMin ?: 0f,
                renda_max = rendaMax ?: 20f,
                peso_renda = pesoRenda,
                peso_escolas = pesoEscolas,
                peso_hospitais = pesoHospitais,
                peso_criminalidade = pesoCriminalidade,
                limite = limite
            )

            val response = api.aplicarFiltros(request)

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body)
                } else {
                    Result.failure(Exception("Resposta vazia do servidor."))
                }
            } else {
                val rawError = response.errorBody()?.string()
                Result.failure(Exception(parseErrorMessage(rawError)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Erro ao aplicar filtros."))
        }
    }

    private fun parseErrorMessage(rawBody: String?): String {
        if (rawBody.isNullOrBlank()) return "Erro inesperado."

        return try {
            val json = JSONObject(rawBody)
            json.optString("detail").ifBlank {
                rawBody
            }
        } catch (_: Exception) {
            rawBody
        }
    }
}