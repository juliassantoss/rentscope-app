package com.example.rentscope.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rentscope.data.remote.dto.pricehistory.PriceHistoryDto
import com.example.rentscope.data.repository.PriceHistoryRepository
import kotlinx.coroutines.launch

class PriceHistoryViewModel : ViewModel() {

    var data by mutableStateOf<List<PriceHistoryDto>>(emptyList())
        private set

    var loading by mutableStateOf(false)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    fun load(codigo: Int) {
        viewModelScope.launch {
            loading = true
            error = null
            data = emptyList()

            val result = PriceHistoryRepository.getHistorico(codigo)

            loading = false

            result
                .onSuccess {
                    data = it
                }
                .onFailure {
                    error = it.message ?: "Erro ao carregar histórico de preços."
                }
        }
    }
}