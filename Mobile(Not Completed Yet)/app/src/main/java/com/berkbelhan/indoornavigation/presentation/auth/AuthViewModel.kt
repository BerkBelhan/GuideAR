package com.berkbelhan.indoornavigation.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.berkbelhan.indoornavigation.core.common.Result
import com.berkbelhan.indoornavigation.domain.repository.AuthRepository
import com.berkbelhan.indoornavigation.domain.usecase.LoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        // If user is already authenticated, mark as authenticated immediately.
        if (authRepository.isLoggedIn()) {
            _uiState.value = _uiState.value.copy(isAuthenticated = true)
        }
    }

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Email and password are required")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = loginUseCase(email, password)) {
                is Result.Success -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isAuthenticated = true,
                    session = result.value
                )
                is Result.Failure -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.error.toString()
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
