package com.example.rentscope.data.remote.dto.score

data class ScoreFiltroRequestDto(
    val busca: String? = null,
    val renda_min: Float? = null,
    val renda_max: Float? = null,
    val peso_renda: Float = 1f,
    val peso_escolas: Float = 1f,
    val peso_hospitais: Float = 1f,
    val peso_criminalidade: Float = 1f,
    val limite: Int = 200
)