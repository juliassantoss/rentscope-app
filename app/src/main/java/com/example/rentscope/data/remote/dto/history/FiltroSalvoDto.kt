package com.example.rentscope.data.remote.dto.history

data class FiltroSalvoDto(
    val id: String,
    val usuario_id: String,
    val country_code: String,
    val country_name: String,
    val renda_min: Float?,
    val renda_max: Float?,
    val peso_renda: Float,
    val peso_escolas: Float,
    val peso_hospitais: Float,
    val peso_criminalidade: Float,
    val criado_em: String,
    val favorito: Boolean
)