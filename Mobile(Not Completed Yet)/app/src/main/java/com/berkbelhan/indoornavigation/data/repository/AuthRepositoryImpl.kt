package com.berkbelhan.indoornavigation.data.repository

import com.berkbelhan.indoornavigation.core.common.AppError
import com.berkbelhan.indoornavigation.core.common.Result
import com.berkbelhan.indoornavigation.core.security.SecureTokenStore
import com.berkbelhan.indoornavigation.data.remote.api.IndoorNavApi
import com.berkbelhan.indoornavigation.data.remote.dto.LoginRequestDto
import com.berkbelhan.indoornavigation.domain.model.UserSession
import com.berkbelhan.indoornavigation.domain.repository.AuthRepository
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val api: IndoorNavApi,
    private val tokenStore: SecureTokenStore
) : AuthRepository {

    private var session: UserSession? = null

    override suspend fun login(email: String, password: String): Result<UserSession> {
        return try {
            val response = api.login(LoginRequestDto(email, password))
            if (response.isSuccessful) {
                val dto = response.body()!!
                tokenStore.saveAccessToken(dto.accessToken)
                dto.refreshToken?.let { tokenStore.saveRefreshToken(it) }
                tokenStore.saveUserId(dto.userId)
                val user = UserSession(
                    userId = dto.userId,
                    accessToken = dto.accessToken,
                    email = dto.email,
                    displayName = dto.displayName,
                    licenseExpiry = dto.licenseExpiry
                )
                session = user
                Result.Success(user)
            } else {
                Result.Failure(AppError.Auth("Login failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Login error")
            Result.Failure(AppError.Network(e.message))
        }
    }

    override suspend fun logout(): Result<Unit> {
        session = null
        tokenStore.clearAll()
        return Result.Success(Unit)
    }

    override suspend fun refreshSession(): Result<UserSession> {
        val refreshToken = tokenStore.getRefreshToken()
            ?: return Result.Failure(AppError.Auth("No refresh token"))
        return try {
            val response = api.refreshToken("Bearer $refreshToken")
            if (response.isSuccessful) {
                val dto = response.body()!!
                tokenStore.saveAccessToken(dto.accessToken)
                dto.refreshToken?.let { tokenStore.saveRefreshToken(it) }
                val user = UserSession(
                    userId = dto.userId,
                    accessToken = dto.accessToken,
                    email = dto.email,
                    displayName = dto.displayName,
                    licenseExpiry = dto.licenseExpiry
                )
                session = user
                Result.Success(user)
            } else {
                Result.Failure(AppError.Auth("Refresh failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Session refresh error")
            Result.Failure(AppError.Network(e.message))
        }
    }

    override fun isLoggedIn(): Boolean = tokenStore.hasValidToken()

    override fun getCurrentSession(): UserSession? = session
}
