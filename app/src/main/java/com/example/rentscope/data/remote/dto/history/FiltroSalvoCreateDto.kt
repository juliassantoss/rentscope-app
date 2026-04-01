package com.example.rentscope.data.remote.dto.history

data class FiltroSalvoCreateDto(
    val country_code: String,
    val country_name: String,
    val renda_min: Float? = null,
    val renda_max: Float? = null,
    val peso_renda: Float = 1f,
    val peso_escolas: Float = 1f,
    val peso_hospitais: Float = 1f,
    val peso_criminalidade: Float = 1f
)