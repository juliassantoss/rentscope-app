package com.example.rentscope.data.remote.dto.history

/**
 * Body do endpoint `POST /historico/favoritos`.
 *
 * O conceito de favorito mudou de "guardar uma busca" para "guardar uma cidade",
 * por isso aqui passamos o código do município (não um filtro).
 */
data class FavoritoCreateDto(
    val codigo_municipio: Int
)
