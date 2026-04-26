package com.example.rentscope.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rentscope.data.repository.AuthRepository
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    var loginLoading by mutableStateOf(false)
        private set

    var loginError by mutableStateOf<String?>(null)
        private set

    var registerLoading by mutableStateOf(false)
        private set

    var registerError by mutableStateOf<String?>(null)
        private set

    var forgotPasswordLoading by mutableStateOf(false)
        private set

    var forgotPasswordError by mutableStateOf<String?>(null)
        private set

    var forgotPasswordSuccess by mutableStateOf<String?>(null)
        private set

    fun login(
        email: String,
        password: String,
        onSuccess: () -> Unit
    ) {
        loginError = null

        if (email.isBlank() || password.isBlank()) {
            loginError = "Preenche e-mail e senha."
            return
        }

        viewModelScope.launch {
            loginLoading = true

            val result = AuthRepository.login(email, password)

            loginLoading = false

            result
                .onSuccess {
                    onSuccess()
                }
                .onFailure {
                    loginError = it.message ?: "Erro ao iniciar sessão."
                }
        }
    }

    fun register(
        email: String,
        password: String,
        confirmPassword: String,
        onSuccess: () -> Unit
    ) {
        registerError = null

        if (email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
            registerError = "Preenche todos os campos."
            return
        }

        if (password != confirmPassword) {
            registerError = "As senhas não coincidem."
            return
        }

        if (password.length < 6) {
            registerError = "A senha deve ter pelo menos 6 caracteres."
            return
        }

        viewModelScope.launch {
            registerLoading = true

            val result = AuthRepository.register(
                email = email,
                password = password
            )

            registerLoading = false

            result
                .onSuccess {
                    onSuccess()
                }
                .onFailure {
                    registerError = it.message ?: "Erro ao criar conta."
                }
        }
    }

    fun forgotPassword(email: String) {
        forgotPasswordError = null
        forgotPasswordSuccess = null

        if (email.isBlank()) {
            forgotPasswordError = "Digite seu e-mail."
            return
        }

        viewModelScope.launch {
            forgotPasswordLoading = true

            val result = AuthRepository.forgotPassword(email.trim())

            forgotPasswordLoading = false

            result
                .onSuccess {
                    forgotPasswordSuccess = "Enviamos um e-mail de recuperação."
                }
                .onFailure {
                    forgotPasswordError = it.message ?: "Erro ao solicitar recuperação de senha."
                }
        }
    }
}
