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

    private var greetingMessage by mutableStateOf("")
    private var errorReplyMessage by mutableStateOf("")

    init {
        loadMunicipios()
    }

    private fun loadMunicipios() {
        viewModelScope.launch {
            val result = MunicipioRepository.getMunicipios()
            result.onSuccess {
                municipios = it
            }
        }
    }

    fun syncLocalizedTexts(
        greeting: String,
        errorReply: String
    ) {
        greetingMessage = greeting
        errorReplyMessage = errorReply

        when {
            messages.isEmpty() -> messages.add(ChatMessage(greeting, false))
            messages.size == 1 && !messages.first().isUser -> {
                messages[0] = ChatMessage(greeting, false)
            }
        }
    }

    fun perguntar(
        pergunta: String,
        languageCode: String
    ) {
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
                pergunta = texto,
                idioma_app = languageCode,
                renda_min = lastSearch?.rendaMin,
                renda_max = lastSearch?.rendaMax,
                peso_renda = lastSearch?.pesoRenda,
                peso_escolas = lastSearch?.pesoEscolas,
                peso_hospitais = lastSearch?.pesoHospitais,
                peso_criminalidade = lastSearch?.pesoCriminalidade
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
                    ChatMessage(
                        errorReplyMessage.ifBlank {
                            "Nao consegui responder agora. Tenta novamente daqui a pouco."
                        },
                        false
                    )
                )
                mascotState = MascotState.ERROR

                delay(1500)
                mascotState = MascotState.IDLE
            }
        }
    }

    private fun resolveMunicipio(pergunta: String): String? {
        val perguntaLower = pergunta.lowercase()

        val encontrado = municipios.firstOrNull {
            perguntaLower.contains(it.municipio_localidade.lowercase())
        }

        return encontrado?.municipio_localidade
    }

    fun clearConversation() {
        messages.clear()
        if (greetingMessage.isNotBlank()) {
            messages.add(ChatMessage(greetingMessage, false))
        }
    }
}
