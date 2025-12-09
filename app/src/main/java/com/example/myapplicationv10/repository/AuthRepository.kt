package com.example.myapplicationv10.repository

import android.content.Context
import com.example.myapplicationv10.model.AuthResponse
import com.example.myapplicationv10.model.LoginRequest
import com.example.myapplicationv10.model.RegisterRequest
import com.example.myapplicationv10.network.ApiClient
import com.example.myapplicationv10.network.NetworkResult
import com.example.myapplicationv10.utils.TokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * AuthRepository - Repository pour gérer l'authentification
 *
 * Gère les opérations de login, register, et logout
 * Utilise les coroutines pour les opérations asynchrones
 */
class AuthRepository(private val context: Context) {

    private val apiService = ApiClient.getApiService(context)
    private val tokenManager = TokenManager.getInstance(context)

    /**
     * Connexion de l'utilisateur
     *
     * @param email Email de l'utilisateur
     * @param password Mot de passe
     * @return NetworkResult avec AuthResponse en cas de succès
     */
    suspend fun login(email: String, password: String): NetworkResult<AuthResponse> {
        return withContext(Dispatchers.IO) {
            val result = ApiClient.safeApiCall {
                apiService.login(LoginRequest(email, password))
            }

            // Si succès, sauvegarder le token et les infos utilisateur
            if (result is NetworkResult.Success) {
                tokenManager.saveToken(result.data.token)
                tokenManager.saveUserInfo(result.data.userId, email)
            }

            result
        }
    }

    /**
     * Inscription d'un nouvel utilisateur
     *
     * @param firstName Prénom de l'utilisateur
     * @param lastName Nom de famille de l'utilisateur
     * @param email Email de l'utilisateur
     * @param phone Numéro de téléphone
     * @param password Mot de passe
     * @return NetworkResult avec AuthResponse en cas de succès
     */
    suspend fun register(
        firstName: String,
        lastName: String,
        email: String,
        phone: String,
        password: String
    ): NetworkResult<AuthResponse> {
        return withContext(Dispatchers.IO) {
            val result = ApiClient.safeApiCall {
                apiService.register(
                    RegisterRequest(
                        firstName = firstName,
                        lastName = lastName,
                        email = email,
                        phoneNumber = phone,
                        password = password
                    )
                )
            }

            // Si succès, sauvegarder le token et les infos utilisateur
            if (result is NetworkResult.Success) {
                tokenManager.saveToken(result.data.token)
                tokenManager.saveUserInfo(result.data.userId, email)
            }

            result
        }
    }

    /**
     * Déconnexion de l'utilisateur
     * Supprime le token et les données utilisateur
     */
    fun logout() {
        tokenManager.clearAuth()
        ApiClient.reset()
    }

    /**
     * Vérifier si l'utilisateur est connecté
     */
    fun isLoggedIn(): Boolean {
        return tokenManager.isLoggedIn()
    }

    /**
     * Récupérer l'email de l'utilisateur connecté
     */
    fun getUserEmail(): String? {
        return tokenManager.getUserEmail()
    }

    /**
     * Récupérer l'ID de l'utilisateur connecté
     */
    fun getUserId(): String? {
        return tokenManager.getUserId()
    }
}
