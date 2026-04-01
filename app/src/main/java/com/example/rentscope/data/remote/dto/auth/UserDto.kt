package com.example.rentscope.data.remote.dto.auth

import com.squareup.moshi.Json

data class UserDto(
    val id: Int,
    val email: String,

    @Json(name = "is_verified")
    val isVerified: Boolean
)