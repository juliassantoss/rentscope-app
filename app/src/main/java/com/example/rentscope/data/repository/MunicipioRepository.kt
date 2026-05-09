package com.example.rentscope.data.repository

import android.util.Log
import com.example.rentscope.data.local.OfflineScoreCalculator
import com.example.rentscope.data.remote.ApiClient
import com.example.rentscope.data.remote.RentScopeApi
import com.example.rentscope.data.remote.dto.MunicipioDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object MunicipioRepository {

    private val api = ApiClient.retrofit.create(RentScopeApi::class.java)
    private const val TAG = "MunicipioRepository"

    suspend fun getMunicipios(): Result<List<MunicipioDto>> {
        return try {
            val response = api.getMunicipios()

            if (response.isSuccessful) {
                Result.success(
                    response.body().orEmpty().ifEmpty {
                        buildOfflineFallback("empty_response")
                    }
                )
            } else {
                Result.success(buildOfflineFallback("http_${response.code()}"))
            }
        } catch (e: Exception) {
            Result.success(buildOfflineFallback(e.message ?: "exception"))
        }
    }

    private suspend fun buildOfflineFallback(reason: String): List<MunicipioDto> {
        Log.w(TAG, "Using offline municipality fallback: $reason")
        return withContext(Dispatchers.Default) {
            OfflineScoreCalculator.buildMunicipios()
        }
    }
}
