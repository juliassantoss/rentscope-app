package com.example.rentscope.data.remote.dto.ai

data class AiQuestionRequest(
    val pais: String,
    val municipio: String? = null,
    val pergunta: String,
    val renda: Float? = null,
    val escolas: Float? = null,
    val hospitais: Float? = null,
    val criminalidade: Float? = null,
    val score: Float? = null
)