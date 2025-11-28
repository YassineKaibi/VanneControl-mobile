package com.example.myapplicationv10.repository

import android.content.Context
import com.example.myapplicationv10.model.Device
import com.example.myapplicationv10.network.ApiClient
import com.example.myapplicationv10.network.NetworkResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * DeviceRepository - Repository pour gérer les appareils
 *
 * Gère les opérations CRUD sur les appareils (devices)
 */
class DeviceRepository(private val context: Context) {

    private val apiService = ApiClient.getApiService(context)

    /**
     * Récupérer tous les appareils de l'utilisateur
     *
     * @return NetworkResult avec la liste des appareils
     */
    suspend fun getDevices(): NetworkResult<List<Device>> {
        return withContext(Dispatchers.IO) {
            val result = ApiClient.safeApiCall {
                apiService.getDevices()
            }

            // Transformer DevicesResponse en List<Device>
            when (result) {
                is NetworkResult.Success -> NetworkResult.Success(result.data.devices)
                is NetworkResult.Error -> NetworkResult.Error(result.message, result.code)
                is NetworkResult.Loading -> NetworkResult.Loading
                is NetworkResult.Idle -> NetworkResult.Idle
            }
        }
    }

    /**
     * Récupérer un appareil spécifique par son ID
     *
     * @param deviceId L'ID de l'appareil
     * @return NetworkResult avec l'appareil
     */
    suspend fun getDevice(deviceId: String): NetworkResult<Device> {
        return withContext(Dispatchers.IO) {
            val result = ApiClient.safeApiCall {
                apiService.getDevice(deviceId)
            }

            // Transformer DeviceResponse en Device
            when (result) {
                is NetworkResult.Success -> NetworkResult.Success(result.data.device)
                is NetworkResult.Error -> NetworkResult.Error(result.message, result.code)
                is NetworkResult.Loading -> NetworkResult.Loading
                is NetworkResult.Idle -> NetworkResult.Idle
            }
        }
    }

    /**
     * Rafraîchir les données d'un appareil
     * Utile pour mettre à jour l'état des pistons
     */
    suspend fun refreshDevice(deviceId: String): NetworkResult<Device> {
        return getDevice(deviceId)
    }
}
