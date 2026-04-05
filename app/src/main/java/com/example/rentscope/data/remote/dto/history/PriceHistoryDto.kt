package com.example.rentscope.data.remote.dto.pricehistory

data class PriceHistoryDto(
    val codigo_municipio: Int,
    val municipio_localidade: String,
    val trimestre: String,
    val valor_medio_m2: Float
)