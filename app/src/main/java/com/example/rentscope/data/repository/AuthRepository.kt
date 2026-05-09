package com.example.rentscope.data.repository

import com.example.rentscope.data.local.TokenManager
import com.example.rentscope.data.remote.ApiClient
import com.example.rentscope.data.remote.RentScopeApi
import com.example.rentscope.data.remote.dto.auth.ForgotPasswordRequestDto
import com.example.rentscope.data.remote.dto.auth.LoginRequestDto
import com.example.rentscope.data.remote.dto.auth.RegisterRequestDto
import com.example.rentscope.data.remote.dto.auth.UserDto
import org.json.JSONObject

object AuthRepository {

    private val api: RentScopeApi = ApiClient.retrofit.create(RentScopeApi::class.java)

    suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            val response = api.login(
                LoginRequestDto(
                    email = email,
                    password = password
                )
            )

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    TokenManager.saveTokens(
                        accessToken = body.accessToken,
                        refreshToken = body.refreshToken
                    )
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Resposta vazia do servidor."))
                }
            } else {
                Result.failure(Exception(parseErrorMessage(response.errorBody()?.string())))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Erro ao iniciar sessão."))
        }
    }

    suspend fun register(email: String, password: String): Result<UserDto> {
        return try {
            val response = api.register(
                RegisterRequestDto(
                    email = email,
                    password = password
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
            Result.failure(Exception(e.message ?: "Erro ao criar conta."))
        }
    }

    suspend fun forgotPassword(email: String): Result<String> {
        return try {
            val response = api.forgotPassword(
                ForgotPasswordRequestDto(email = email)
            )

            if (response.isSuccessful) {
                Result.success(response.body()?.message ?: "Enviamos um e-mail de recuperação.")
            } else {
                Result.failure(Exception(parseErrorMessage(response.errorBody()?.string())))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Erro ao solicitar recuperação de senha."))
        }
    }

    suspend fun me(): Result<UserDto> {
        return try {
            val response = api.me()

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
            Result.failure(Exception(e.message ?: "Erro ao carregar utilizador."))
        }
    }

    fun logout() {
        TokenManager.clearTokens()
    }

    fun isLoggedIn(): Boolean {
        return TokenManager.isLoggedIn()
    }

    private fun parseErrorMessage(rawBody: String?): String {
        if (rawBody.isNullOrBlank()) return "Erro inesperado."

        return try {
            val json = JSONObject(rawBody)
            normalizeAuthMessage(
                rawMessage = json.optString("detail").ifBlank {
                    json.optString("message")
                },
                fallback = "Erro inesperado."
            )
        } catch (_: Exception) {
            normalizeAuthMessage(
                rawMessage = rawBody,
                fallback = "Erro inesperado."
            )
        }
    }

    private fun normalizeAuthMessage(rawMessage: String?, fallback: String): String {
        val message = rawMessage?.trim().orEmpty()

        if (message.isBlank()) {
            return fallback
        }

        if (message.startsWith("{") || message.startsWith("[")) {
            return fallback
        }

        return when {
            message.contains("not verified", ignoreCase = true) ||
                message.contains("verify your email", ignoreCase = true) ||
                message.contains("verification", ignoreCase = true) -> {
                "Verifica o teu e-mail para concluir a validacao."
            }

            message.contains("invalid credentials", ignoreCase = true) ||
                message.contains("incorrect", ignoreCase = true) -> {
                "E-mail ou senha incorretos."
            }

            message.contains("already", ignoreCase = true) &&
                message.contains("email", ignoreCase = true) -> {
                "Este e-mail ja esta registado."
            }

            message.contains("verified", ignoreCase = true) &&
                message.contains("email", ignoreCase = true) -> {
                "E-mail verificado com sucesso."
            }

            else -> message
        }
    }
}
