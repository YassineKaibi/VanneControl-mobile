package com.example.myapplicationv10.repository

import android.content.Context
import com.example.myapplicationv10.model.DeviceStatsResponse
import com.example.myapplicationv10.model.TelemetryEvent
import com.example.myapplicationv10.network.ApiClient
import com.example.myapplicationv10.network.NetworkResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * TelemetryRepository - Repository pour gérer les statistiques et la télémétrie
 *
 * Gère les opérations liées aux statistiques des appareils et à l'historique des événements
 */
class TelemetryRepository(private val context: Context) {

    private val apiService = ApiClient.getApiService(context)

    /**
     * Récupérer les statistiques d'un appareil
     *
     * @param deviceId L'ID de l'appareil
     * @return NetworkResult avec les statistiques
     */
    suspend fun getDeviceStats(deviceId: String): NetworkResult<DeviceStatsResponse> {
        return withContext(Dispatchers.IO) {
            ApiClient.safeApiCall {
                apiService.getDeviceStats(deviceId)
            }
        }
    }

    /**
     * Récupérer l'historique des événements (télémétrie)
     *
     * @param deviceId Filtrer par appareil (optionnel)
     * @param pistonNumber Filtrer par numéro de piston (optionnel)
     * @param action Filtrer par action: "activated" ou "deactivated" (optionnel)
     * @param startDate Date de début au format ISO (optionnel)
     * @param endDate Date de fin au format ISO (optionnel)
     * @param limit Nombre maximum de résultats
     * @return NetworkResult avec la liste des événements
     */
    suspend fun getTelemetry(
        deviceId: String? = null,
        pistonNumber: Int? = null,
        action: String? = null,
        startDate: String? = null,
        endDate: String? = null,
        limit: Int? = null
    ): NetworkResult<List<TelemetryEvent>> {
        return withContext(Dispatchers.IO) {
            val result = ApiClient.safeApiCall {
                apiService.getTelemetry(deviceId, pistonNumber, action, startDate, endDate, limit)
            }

            // Transformer TelemetryListResponse en List<TelemetryEvent>
            when (result) {
                is NetworkResult.Success -> NetworkResult.Success(result.data.telemetry)
                is NetworkResult.Error -> NetworkResult.Error(result.message, result.code)
                is NetworkResult.Loading -> NetworkResult.Loading
                is NetworkResult.Idle -> NetworkResult.Idle
            }
        }
    }
}
