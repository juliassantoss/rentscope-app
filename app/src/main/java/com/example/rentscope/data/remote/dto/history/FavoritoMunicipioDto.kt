package com.example.rentscope.data.remote.dto.history

/**
 * Item devolvido pelo endpoint `GET /historico/favoritos`.
 *
 * Cada linha corresponde a um município que o utilizador marcou como favorito.
 */
data class FavoritoMunicipioDto(
    val favorito_id: String,
    val favoritado_em: String?,
    val codigo_municipio: Int,
    val municipio_localidade: String,
    val regiao: String?,
    val grande_regiao: String?
)
