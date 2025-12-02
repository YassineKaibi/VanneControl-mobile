package com.example.myapplicationv10.repository

import android.content.Context
import com.example.myapplicationv10.model.UpdatePreferencesRequest
import com.example.myapplicationv10.model.UpdateProfileRequest
import com.example.myapplicationv10.model.User
import com.example.myapplicationv10.network.ApiClient
import com.example.myapplicationv10.network.NetworkResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * UserRepository - Repository pour gérer le profil utilisateur
 *
 * Gère les opérations CRUD du profil utilisateur
 * Utilise les coroutines pour les opérations asynchrones
 */
class UserRepository(private val context: Context) {

    private val apiService = ApiClient.getApiService(context)

    /**
     * Récupérer le profil de l'utilisateur connecté
     *
     * @return NetworkResult avec User en cas de succès
     */
    suspend fun getUserProfile(): NetworkResult<User> {
        return withContext(Dispatchers.IO) {
            ApiClient.safeApiCall {
                apiService.getUserProfile()
            }
        }
    }

    /**
     * Mettre à jour le profil utilisateur
     *
     * @param request Les données à mettre à jour
     * @return NetworkResult avec User mis à jour en cas de succès
     */
    suspend fun updateUserProfile(request: UpdateProfileRequest): NetworkResult<User> {
        return withContext(Dispatchers.IO) {
            ApiClient.safeApiCall {
                apiService.updateUserProfile(request)
            }
        }
    }

    /**
     * Mettre à jour les préférences utilisateur
     *
     * @param preferences Les préférences au format JSON
     * @return NetworkResult avec User mis à jour en cas de succès
     */
    suspend fun updateUserPreferences(preferences: String): NetworkResult<User> {
        return withContext(Dispatchers.IO) {
            ApiClient.safeApiCall {
                apiService.updateUserPreferences(UpdatePreferencesRequest(preferences))
            }
        }
    }
}
