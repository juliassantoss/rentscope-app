package com.example.rentscope.data.remote.dto.auth

import com.squareup.moshi.Json

data class TokenResponseDto(
    @Json(name = "access_token")
    val accessToken: String,

    @Json(name = "refresh_token")
    val refreshToken: String,

    @Json(name = "token_type")
    val tokenType: String
)