package com.example.myapplicationv10.network.interceptors

import com.example.myapplicationv10.utils.Constants
import com.example.myapplicationv10.utils.TokenManager
import okhttp3.Interceptor
import okhttp3.Response

/**
 * AuthInterceptor - Intercepteur pour ajouter automatiquement le token JWT
 * à toutes les requêtes authentifiées
 *
 * Ajoute le header: Authorization: Bearer {token}
 */
class AuthInterceptor(private val tokenManager: TokenManager) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Récupérer le token
        val token = tokenManager.getToken()

        // Si pas de token, continuer avec la requête originale
        if (token == null) {
            return chain.proceed(originalRequest)
        }

        // Ajouter le header Authorization avec le Bearer token
        val authenticatedRequest = originalRequest.newBuilder()
            .header(Constants.AUTHORIZATION_HEADER, "${Constants.BEARER_PREFIX}$token")
            .build()

        return chain.proceed(authenticatedRequest)
    }
}
