package com.example.rentscope.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rentscope.data.remote.dto.MunicipioDto
import com.example.rentscope.data.repository.MunicipioRepository
import kotlinx.coroutines.launch

class MunicipioViewModel : ViewModel() {

    var municipios by mutableStateOf<List<MunicipioDto>>(emptyList())
        private set

    var loading by mutableStateOf(false)
        private set

    fun load() {
        viewModelScope.launch {
            loading = true

            val result = MunicipioRepository.getMunicipios()

            loading = false

            result.onSuccess {
                municipios = it
            }
        }
    }
}