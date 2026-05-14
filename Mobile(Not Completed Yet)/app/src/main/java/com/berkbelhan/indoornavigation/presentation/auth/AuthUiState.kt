package com.berkbelhan.indoornavigation.presentation.auth

import com.berkbelhan.indoornavigation.domain.model.UserSession

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isAuthenticated: Boolean = false,
    val session: UserSession? = null
)
