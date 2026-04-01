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
            val response = api.aplicarFiltros(
                ScoreFiltroRequestDto(
                    busca = busca,
                    renda_min = rendaMin,
                    renda_max = rendaMax,
                    peso_renda = pesoRenda,
                    peso_escolas = pesoEscolas,
                    peso_hospitais = pesoHospitais,
                    peso_criminalidade = pesoCriminalidade,
                    limite = limite
                )
            )

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body)
                } else {
                    Result.failure(Exception("Resposta vazia do servidor."))
                }
            } else {
                Result.failure(Exception(parseErrorMessage(response.errorBody()?.string())))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Erro ao aplicar filtros."))
        }
    }

    private fun parseErrorMessage(rawBody: String?): String {
        if (rawBody.isNullOrBlank()) return "Erro inesperado."

        return try {
            val json = JSONObject(rawBody)
            json.optString("detail").ifBlank { "Erro inesperado." }
        } catch (_: Exception) {
            "Erro inesperado."
        }
    }
}