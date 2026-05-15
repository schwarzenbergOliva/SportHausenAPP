package com.example.webviewapp.ui.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.webviewapp.data.SessionManager
import com.example.webviewapp.network.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val loginSuccess: Boolean = false
) {
    val isFormValid: Boolean
        get() = email.contains("@") && password.length >= 6
}

class LoginViewModel(app: Application) : AndroidViewModel(app) {

    private val authRepository = AuthRepository()
    private val sessionManager = SessionManager(app)

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    fun onEmailChange(value: String) {
        _state.update { it.copy(email = value, errorMessage = null) }
    }

    fun onPasswordChange(value: String) {
        _state.update { it.copy(password = value, errorMessage = null) }
    }

    fun onLoginClicked() {
        val current = _state.value
        if (!current.isFormValid || current.isLoading) return

        _state.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            authRepository.login(current.email.trim(), current.password)
                .onSuccess { token ->
                    sessionManager.saveToken(token)
                    _state.update {
                        it.copy(isLoading = false, loginSuccess = true)
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Error de autenticación"
                        )
                    }
                }
        }
    }

    fun consumeNavigation() {
        _state.update { it.copy(loginSuccess = false) }
    }
}
