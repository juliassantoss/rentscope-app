package com.example.rentscope.data.repository

import android.util.Log
import com.example.rentscope.data.local.OfflineScoreCalculator
import com.example.rentscope.data.remote.ApiClient
import com.example.rentscope.data.remote.RentScopeApi
import com.example.rentscope.data.remote.dto.score.ScoreFiltroRequestDto
import com.example.rentscope.data.remote.dto.score.ScoreMunicipioDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

object ScoreRepository {

    private val api: RentScopeApi = ApiClient.retrofit.create(RentScopeApi::class.java)
    private const val TAG = "ScoreRepository"

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
                    Result.success(
                        body.ifEmpty {
                            buildOfflineFallbackSync(
                                busca = busca,
                                rendaMin = rendaMin,
                                rendaMax = rendaMax,
                                pesoRenda = pesoRenda,
                                pesoEscolas = pesoEscolas,
                                pesoHospitais = pesoHospitais,
                                pesoCriminalidade = pesoCriminalidade,
                                limite = limite,
                                reason = "empty_list"
                            )
                        }
                    )
                } else {
                    Result.success(
                        buildOfflineFallback(
                            busca = busca,
                            rendaMin = rendaMin,
                            rendaMax = rendaMax,
                            pesoRenda = pesoRenda,
                            pesoEscolas = pesoEscolas,
                            pesoHospitais = pesoHospitais,
                            pesoCriminalidade = pesoCriminalidade,
                            limite = limite,
                            reason = "empty_response"
                        )
                    )
                }
            } else {
                val rawError = response.errorBody()?.string()
                Result.success(
                    buildOfflineFallback(
                        busca = busca,
                        rendaMin = rendaMin,
                        rendaMax = rendaMax,
                        pesoRenda = pesoRenda,
                        pesoEscolas = pesoEscolas,
                        pesoHospitais = pesoHospitais,
                        pesoCriminalidade = pesoCriminalidade,
                        limite = limite,
                        reason = parseErrorMessage(rawError)
                    )
                )
            }
        } catch (e: Exception) {
            Result.success(
                buildOfflineFallback(
                    busca = busca,
                    rendaMin = rendaMin,
                    rendaMax = rendaMax,
                    pesoRenda = pesoRenda,
                    pesoEscolas = pesoEscolas,
                    pesoHospitais = pesoHospitais,
                    pesoCriminalidade = pesoCriminalidade,
                    limite = limite,
                    reason = e.message ?: "Erro ao aplicar filtros."
                )
            )
        }
    }

    private suspend fun buildOfflineFallback(
        busca: String?,
        rendaMin: Float?,
        rendaMax: Float?,
        pesoRenda: Float,
        pesoEscolas: Float,
        pesoHospitais: Float,
        pesoCriminalidade: Float,
        limite: Int,
        reason: String
    ): List<ScoreMunicipioDto> {
        return withContext(Dispatchers.Default) {
            buildOfflineFallbackSync(
                busca = busca,
                rendaMin = rendaMin,
                rendaMax = rendaMax,
                pesoRenda = pesoRenda,
                pesoEscolas = pesoEscolas,
                pesoHospitais = pesoHospitais,
                pesoCriminalidade = pesoCriminalidade,
                limite = limite,
                reason = reason
            )
        }
    }

    private fun buildOfflineFallbackSync(
        busca: String?,
        rendaMin: Float?,
        rendaMax: Float?,
        pesoRenda: Float,
        pesoEscolas: Float,
        pesoHospitais: Float,
        pesoCriminalidade: Float,
        limite: Int,
        reason: String
    ): List<ScoreMunicipioDto> {
        Log.w(TAG, "Using offline municipality score fallback: $reason")
        return OfflineScoreCalculator.buildScores(
            busca = busca,
            rendaMin = rendaMin,
            rendaMax = rendaMax,
            pesoRenda = pesoRenda,
            pesoEscolas = pesoEscolas,
            pesoHospitais = pesoHospitais,
            pesoCriminalidade = pesoCriminalidade,
            limite = limite
        )
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
