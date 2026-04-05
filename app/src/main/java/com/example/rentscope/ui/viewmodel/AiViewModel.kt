package com.example.rentscope.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rentscope.data.remote.dto.ai.AiQuestionRequest
import com.example.rentscope.data.repository.AiRepository
import kotlinx.coroutines.launch

class AiViewModel : ViewModel() {

    var resposta by mutableStateOf("")
        private set

    var loading by mutableStateOf(false)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    fun perguntar(request: AiQuestionRequest) {
        viewModelScope.launch {
            loading = true
            error = null
            resposta = ""

            val result = AiRepository.perguntar(request)

            loading = false

            result
                .onSuccess { resposta = it.resposta }
                .onFailure { error = it.message ?: "Erro ao consultar IA." }
        }
    }
}