package com.example.rentscope.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rentscope.data.remote.dto.score.ScoreMunicipioDto
import com.example.rentscope.data.repository.ScoreRepository
import kotlinx.coroutines.launch

class ScoreViewModel : ViewModel() {

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var municipios by mutableStateOf<List<ScoreMunicipioDto>>(emptyList())
        private set

    fun carregarScores(
        busca: String? = null,
        rendaMin: Float? = null,
        rendaMax: Float? = null,
        pesoRenda: Float = 1f,
        pesoEscolas: Float = 1f,
        pesoHospitais: Float = 1f,
        pesoCriminalidade: Float = 1f,
        limite: Int = 200
    ) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            val result = ScoreRepository.aplicarFiltros(
                busca = busca,
                rendaMin = rendaMin,
                rendaMax = rendaMax,
                pesoRenda = pesoRenda,
                pesoEscolas = pesoEscolas,
                pesoHospitais = pesoHospitais,
                pesoCriminalidade = pesoCriminalidade,
                limite = limite
            )

            isLoading = false

            result
                .onSuccess { lista ->
                    municipios = lista
                }
                .onFailure {
                    errorMessage = it.message ?: "Erro ao carregar scores."
                }
        }
    }
}