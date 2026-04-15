package com.example.rentscope.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rentscope.data.local.LastSearchManager
import com.example.rentscope.data.remote.dto.MunicipioDto
import com.example.rentscope.data.remote.dto.ai.AiQuestionRequest
import com.example.rentscope.data.repository.AiRepository
import com.example.rentscope.data.repository.MunicipioRepository
import com.example.rentscope.ui.components.ChatMessage
import com.example.rentscope.ui.components.MascotState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AiViewModel : ViewModel() {

    val messages = mutableStateListOf<ChatMessage>()

    var loading by mutableStateOf(false)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    var mascotState by mutableStateOf(MascotState.IDLE)
        private set

    var municipios by mutableStateOf<List<MunicipioDto>>(emptyList())
        private set

    init {
        loadMunicipios()

        messages.add(
            ChatMessage(
                "Olá! Pergunta-me qualquer coisa sobre municípios, mapas, filtros, preços ou como usar o RentScope.",
                false
            )
        )
    }

    private fun loadMunicipios() {
        viewModelScope.launch {
            val result = MunicipioRepository.getMunicipios()
            result.onSuccess {
                municipios = it
            }
        }
    }

    fun perguntar(pergunta: String) {
        val texto = pergunta.trim()
        if (texto.isBlank()) return

        val lastSearch = LastSearchManager.get()

        val pais = lastSearch?.countryName?.takeIf { it.isNotBlank() } ?: "Portugal"

        val municipio = resolveMunicipio(texto)

        viewModelScope.launch {
            loading = true
            error = null
            mascotState = MascotState.THINKING

            messages.add(ChatMessage(texto, true))

            val request = AiQuestionRequest(
                pais = pais,
                municipio = municipio,
                pergunta = buildPrompt(texto, pais, municipio, lastSearch)
            )

            val result = AiRepository.perguntar(request)

            loading = false

            result.onSuccess {
                messages.add(ChatMessage(it.resposta, false))
                mascotState = MascotState.SPEAKING

                delay(1500)
                mascotState = MascotState.IDLE
            }

            result.onFailure {
                error = it.message
                messages.add(
                    ChatMessage("Erro ao obter resposta. Tenta novamente.", false)
                )
                mascotState = MascotState.ERROR

                delay(1500)
                mascotState = MascotState.IDLE
            }
        }
    }

    private fun resolveMunicipio(pergunta: String): String {
        val perguntaLower = pergunta.lowercase()

        val encontrado = municipios.firstOrNull {
            perguntaLower.contains(it.municipio_localidade.lowercase())
        }

        if (encontrado != null) return encontrado.municipio_localidade

        if (municipios.isNotEmpty()) {
            return municipios.first().municipio_localidade
        }

        return "Município não especificado"
    }

    private fun buildPrompt(
        pergunta: String,
        pais: String,
        municipio: String,
        lastSearch: com.example.rentscope.data.local.LastSearchData?
    ): String {
        return """
            Tu és o assistente do RentScope.

            O utilizador pode perguntar QUALQUER coisa sobre:
            - municípios
            - qualidade de vida
            - score
            - filtros
            - renda
            - escolas
            - hospitais
            - criminalidade
            - mapa coroplético
            - histórico de preços
            - funcionalidades do app

            Contexto atual:
            País: $pais
            Município: $municipio
            Renda mínima: ${lastSearch?.rendaMin ?: "não definida"}
            Renda máxima: ${lastSearch?.rendaMax ?: "não definida"}

            Regras:
            - Responde sempre de forma útil e direta
            - Não peças mais dados desnecessariamente
            - Se for pergunta sobre o app, explica o app
            - Se for sobre viver, analisa o local
            - Mantém respostas naturais

            Pergunta:
            $pergunta
        """.trimIndent()
    }

    fun clearConversation() {
        messages.clear()
        messages.add(
            ChatMessage(
                "Conversa reiniciada. Pergunta qualquer coisa sobre o RentScope.",
                false
            )
        )
    }
}
