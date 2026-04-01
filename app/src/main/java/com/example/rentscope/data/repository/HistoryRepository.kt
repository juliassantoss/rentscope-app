package com.example.rentscope.data.repository

import com.example.rentscope.data.remote.ApiClient
import com.example.rentscope.data.remote.RentScopeApi
import com.example.rentscope.data.remote.dto.history.FavoritoCreateDto
import com.example.rentscope.data.remote.dto.history.FiltroSalvoCreateDto
import com.example.rentscope.data.remote.dto.history.FiltroSalvoDto
import org.json.JSONObject

object HistoryRepository {

    private val api: RentScopeApi = ApiClient.retrofit.create(RentScopeApi::class.java)

    suspend fun salvarFiltro(
        countryCode: String,
        countryName: String,
        rendaMin: Float?,
        rendaMax: Float?,
        pesoRenda: Float,
        pesoEscolas: Float,
        pesoHospitais: Float,
        pesoCriminalidade: Float
    ): Result<FiltroSalvoDto> {
        return try {
            val response = api.salvarFiltro(
                FiltroSalvoCreateDto(
                    country_code = countryCode,
                    country_name = countryName,
                    renda_min = rendaMin,
                    renda_max = rendaMax,
                    peso_renda = pesoRenda,
                    peso_escolas = pesoEscolas,
                    peso_hospitais = pesoHospitais,
                    peso_criminalidade = pesoCriminalidade
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
            Result.failure(Exception(e.message ?: "Erro ao salvar filtro."))
        }
    }

    suspend fun listarHistorico(): Result<List<FiltroSalvoDto>> {
        return try {
            val response = api.listarFiltrosSalvos()
            if (response.isSuccessful) {
                Result.success(response.body().orEmpty())
            } else {
                Result.failure(Exception(parseErrorMessage(response.errorBody()?.string())))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Erro ao carregar histórico."))
        }
    }

    suspend fun removerFiltro(filtroId: String): Result<Unit> {
        return try {
            val response = api.removerFiltroSalvo(filtroId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(parseErrorMessage(response.errorBody()?.string())))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Erro ao remover filtro."))
        }
    }

    suspend fun adicionarFavorito(filtroId: String): Result<Unit> {
        return try {
            val response = api.adicionarFavorito(FavoritoCreateDto(filtro_id = filtroId))
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(parseErrorMessage(response.errorBody()?.string())))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Erro ao adicionar favorito."))
        }
    }

    suspend fun listarFavoritos(): Result<List<FiltroSalvoDto>> {
        return try {
            val response = api.listarFavoritos()
            if (response.isSuccessful) {
                Result.success(response.body().orEmpty())
            } else {
                Result.failure(Exception(parseErrorMessage(response.errorBody()?.string())))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Erro ao carregar favoritos."))
        }
    }

    suspend fun removerFavorito(filtroId: String): Result<Unit> {
        return try {
            val response = api.removerFavorito(filtroId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(parseErrorMessage(response.errorBody()?.string())))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Erro ao remover favorito."))
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