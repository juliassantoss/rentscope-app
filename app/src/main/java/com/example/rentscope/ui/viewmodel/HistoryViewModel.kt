package com.example.rentscope.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rentscope.data.remote.dto.history.FavoritoMunicipioDto
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

    /**
     * Lista de municípios favoritos do utilizador.
     *
     * Os favoritos passaram a ser por município (não por filtro), por isso
     * esta lista é de [FavoritoMunicipioDto] e a chave para verificar se um
     * município é favorito é o seu `codigo_municipio`.
     */
    var favoritos by mutableStateOf<List<FavoritoMunicipioDto>>(emptyList())
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

    fun adicionarFavorito(codigoMunicipio: Int, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            val result = HistoryRepository.adicionarFavorito(codigoMunicipio)
            result.onFailure {
                errorMessage = it.message ?: "Erro ao adicionar favorito."
            }
            carregarFavoritos()
            onDone()
        }
    }

    fun removerFavorito(codigoMunicipio: Int, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            val result = HistoryRepository.removerFavorito(codigoMunicipio)
            result.onFailure {
                errorMessage = it.message ?: "Erro ao remover favorito."
            }
            carregarFavoritos()
            onDone()
        }
    }

    fun removerFiltro(filtroId: String, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            HistoryRepository.removerFiltro(filtroId)
            carregarHistorico()
            onDone()
        }
    }

    /** Conveniência: indica se um município é favorito. */
    fun isMunicipioFavorito(codigoMunicipio: Int): Boolean {
        return favoritos.any { it.codigo_municipio == codigoMunicipio }
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
