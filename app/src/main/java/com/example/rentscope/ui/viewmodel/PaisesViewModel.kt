package com.example.rentscope.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rentscope.data.remote.ApiClient
import com.example.rentscope.data.remote.RentScopeApi
import com.example.rentscope.data.remote.dto.PaisDto
import com.example.rentscope.data.repository.PaisesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import retrofit2.HttpException

data class PaisesUiState(
    val loading: Boolean = false,
    val paises: List<PaisDto> = emptyList(),
    val error: String? = null
)

class PaisesViewModel : ViewModel() {

    private val api = ApiClient.retrofit.create(RentScopeApi::class.java)
    private val repo = PaisesRepository(api)

    private val _state = MutableStateFlow(PaisesUiState())
    val state: StateFlow<PaisesUiState> = _state

    fun carregarPaises() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            try {
                val paises = repo.listarPaises()
                _state.value = PaisesUiState(loading = false, paises = paises)
            } catch (e: Exception) {
                _state.value = PaisesUiState(
                    loading = false,
                    paises = emptyList(),
                    error = mapCountriesErrorKey(e)
                )
            }
        }
    }

    private fun mapCountriesErrorKey(error: Throwable): String {
        return when (error) {
            is UnknownHostException -> "countries_error_network"
            is SocketTimeoutException -> "countries_error_timeout"
            is HttpException -> "countries_error_server"
            is IOException -> "countries_error_network"
            else -> "countries_error_generic"
        }
    }
}
