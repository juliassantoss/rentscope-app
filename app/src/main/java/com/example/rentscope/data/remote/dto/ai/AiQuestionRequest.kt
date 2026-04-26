package com.example.rentscope.data.remote.dto.ai

data class AiQuestionRequest(
    val pais: String,
    val municipio: String? = null,
    val pergunta: String,
    val idioma_app: String,
    val renda_min: Float? = null,
    val renda_max: Float? = null,
    val peso_renda: Float? = null,
    val peso_escolas: Float? = null,
    val peso_hospitais: Float? = null,
    val peso_criminalidade: Float? = null,
    val renda: Float? = null,
    val escolas: Float? = null,
    val hospitais: Float? = null,
    val criminalidade: Float? = null,
    val score: Float? = null
)
