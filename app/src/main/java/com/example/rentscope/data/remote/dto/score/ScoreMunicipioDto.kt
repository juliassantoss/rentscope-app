package com.example.rentscope.data.remote.dto.score

import com.squareup.moshi.Json

data class ScoreMunicipioDto(
    @Json(name = "codigo_municipio")
    val codigoMunicipio: Int,

    @Json(name = "municipio_localidade")
    val municipioLocalidade: String,

    val regiao: String?,

    @Json(name = "grande_regiao")
    val grandeRegiao: String?,

    @Json(name = "renda_trimestre")
    val rendaTrimestre: String?,

    @Json(name = "valor_medio_m2")
    val valorMedioM2: Float?,

    @Json(name = "total_escolas")
    val totalEscolas: Int,

    @Json(name = "total_hospitais")
    val totalHospitais: Int,

    @Json(name = "total_crimes")
    val totalCrimes: Int,

    @Json(name = "score_escolas")
    val scoreEscolas: Float,

    @Json(name = "score_hospitais")
    val scoreHospitais: Float,

    @Json(name = "score_criminalidade")
    val scoreCriminalidade: Float,

    val score: Float
)