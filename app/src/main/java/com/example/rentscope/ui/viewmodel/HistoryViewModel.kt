package com.example.rentscope.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rentscope.data.remote.dto.history.FiltroSalvoDto
import com.example.rentscope.data.repository.HistoryRepository
import kotlinx.coroutines.launch

class HistoryViewModel : ViewModel() {

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var historico by mutableStateOf<List<FiltroSalvoDto>>(emptyList())
        private set

    var favoritos by mutableStateOf<List<FiltroSalvoDto>>(emptyList())
        private set

    fun carregarHistorico() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            val result = HistoryRepository.listarHistorico()

            isLoading = false
            result
                .onSuccess { historico = it }
                .onFailure { errorMessage = it.message ?: "Erro ao carregar histórico." }
        }
    }

    fun carregarFavoritos() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            val result = HistoryRepository.listarFavoritos()

            isLoading = false
            result
                .onSuccess { favoritos = it }
                .onFailure { errorMessage = it.message ?: "Erro ao carregar favoritos." }
        }
    }

    fun adicionarFavorito(filtroId: String, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            HistoryRepository.adicionarFavorito(filtroId)
            carregarHistorico()
            carregarFavoritos()
            onDone()
        }
    }

    fun removerFavorito(filtroId: String, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            HistoryRepository.removerFavorito(filtroId)
            carregarHistorico()
            carregarFavoritos()
            onDone()
        }
    }

    fun removerFiltro(filtroId: String, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            HistoryRepository.removerFiltro(filtroId)
            carregarHistorico()
            carregarFavoritos()
            onDone()
        }
    }

    fun salvarBusca(
        countryCode: String,
        countryName: String,
        rendaMin: Float?,
        rendaMax: Float?,
        pesoRenda: Float,
        pesoEscolas: Float,
        pesoHospitais: Float,
        pesoCriminalidade: Float
    ) {
        viewModelScope.launch {
            HistoryRepository.salvarFiltro(
                countryCode = countryCode,
                countryName = countryName,
                rendaMin = rendaMin,
                rendaMax = rendaMax,
                pesoRenda = pesoRenda,
                pesoEscolas = pesoEscolas,
                pesoHospitais = pesoHospitais,
                pesoCriminalidade = pesoCriminalidade
            )
        }
    }
}