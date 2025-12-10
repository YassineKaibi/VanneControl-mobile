package com.example.myapplicationv10.repository

import android.content.Context
import com.example.myapplicationv10.model.*
import com.example.myapplicationv10.network.ApiClient
import com.example.myapplicationv10.network.NetworkResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * ScheduleRepository - Repository pour gérer les plannings automatisés
 *
 * Gère les opérations CRUD sur les plannings (schedules)
 */
class ScheduleRepository(private val context: Context) {

    private val apiService = ApiClient.getApiService(context)

    /**
     * Créer un nouveau planning
     *
     * @param request Les détails du planning à créer
     * @return NetworkResult avec le planning créé
     */
    suspend fun createSchedule(request: CreateScheduleRequest): NetworkResult<ScheduleResponse> {
        return withContext(Dispatchers.IO) {
            ApiClient.safeApiCall {
                apiService.createSchedule(request)
            }
        }
    }

    /**
     * Récupérer tous les plannings de l'utilisateur
     *
     * @return NetworkResult avec la liste des plannings
     */
    suspend fun getSchedules(): NetworkResult<List<ScheduleResponse>> {
        return withContext(Dispatchers.IO) {
            val result = ApiClient.safeApiCall {
                apiService.getSchedules()
            }

            // Transformer SchedulesListResponse en List<ScheduleResponse>
            when (result) {
                is NetworkResult.Success -> NetworkResult.Success(result.data.schedules)
                is NetworkResult.Error -> NetworkResult.Error(result.message, result.code)
                is NetworkResult.Loading -> NetworkResult.Loading
                is NetworkResult.Idle -> NetworkResult.Idle
            }
        }
    }

    /**
     * Récupérer un planning spécifique par son ID
     *
     * @param scheduleId L'ID du planning
     * @return NetworkResult avec le planning
     */
    suspend fun getSchedule(scheduleId: String): NetworkResult<ScheduleResponse> {
        return withContext(Dispatchers.IO) {
            ApiClient.safeApiCall {
                apiService.getSchedule(scheduleId)
            }
        }
    }

    /**
     * Mettre à jour un planning existant
     *
     * @param scheduleId L'ID du planning à mettre à jour
     * @param request Les nouvelles données du planning
     * @return NetworkResult avec le planning mis à jour
     */
    suspend fun updateSchedule(
        scheduleId: String,
        request: UpdateScheduleRequest
    ): NetworkResult<ScheduleResponse> {
        return withContext(Dispatchers.IO) {
            ApiClient.safeApiCall {
                apiService.updateSchedule(scheduleId, request)
            }
        }
    }

    /**
     * Supprimer un planning
     *
     * @param scheduleId L'ID du planning à supprimer
     * @return NetworkResult avec le message de confirmation
     */
    suspend fun deleteSchedule(scheduleId: String): NetworkResult<DeleteScheduleResponse> {
        return withContext(Dispatchers.IO) {
            ApiClient.safeApiCall {
                apiService.deleteSchedule(scheduleId)
            }
        }
    }

    /**
     * Activer ou désactiver un planning
     *
     * @param scheduleId L'ID du planning
     * @param enabled true pour activer, false pour désactiver
     * @return NetworkResult avec le planning mis à jour
     */
    suspend fun toggleSchedule(scheduleId: String, enabled: Boolean): NetworkResult<ScheduleResponse> {
        return updateSchedule(
            scheduleId,
            UpdateScheduleRequest(enabled = enabled)
        )
    }
}
