package com.berkbelhan.indoornavigation.domain.repository

import com.berkbelhan.indoornavigation.core.common.Result
import com.berkbelhan.indoornavigation.domain.model.UserSession

/** Domain contract for authentication operations. */
interface AuthRepository {
    suspend fun login(email: String, password: String): Result<UserSession>
    suspend fun logout(): Result<Unit>
    suspend fun refreshSession(): Result<UserSession>
    fun isLoggedIn(): Boolean
    fun getCurrentSession(): UserSession?
}
