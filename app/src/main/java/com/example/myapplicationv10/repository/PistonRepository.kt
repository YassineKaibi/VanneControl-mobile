package com.example.myapplicationv10.repository

import android.content.Context
import com.example.myapplicationv10.model.Piston
import com.example.myapplicationv10.model.PistonControlRequest
import com.example.myapplicationv10.network.ApiClient
import com.example.myapplicationv10.network.NetworkResult
import com.example.myapplicationv10.utils.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * PistonRepository - Repository pour contrôler les pistons
 *
 * Gère les opérations de contrôle (activation/désactivation) des pistons
 */
class PistonRepository(private val context: Context) {

    private val apiService = ApiClient.getApiService(context)

    /**
     * Activer un piston
     *
     * @param deviceId L'ID de l'appareil
     * @param pistonNumber Le numéro du piston (1-8)
     * @return NetworkResult avec le piston mis à jour
     */
    suspend fun activatePiston(
        deviceId: String,
        pistonNumber: Int
    ): NetworkResult<Piston> {
        return controlPiston(deviceId, pistonNumber, Constants.ACTION_ACTIVATE)
    }

    /**
     * Désactiver un piston
     *
     * @param deviceId L'ID de l'appareil
     * @param pistonNumber Le numéro du piston (1-8)
     * @return NetworkResult avec le piston mis à jour
     */
    suspend fun deactivatePiston(
        deviceId: String,
        pistonNumber: Int
    ): NetworkResult<Piston> {
        return controlPiston(deviceId, pistonNumber, Constants.ACTION_DEACTIVATE)
    }

    /**
     * Basculer l'état d'un piston (toggle)
     *
     * @param deviceId L'ID de l'appareil
     * @param pistonNumber Le numéro du piston (1-8)
     * @param currentState L'état actuel du piston
     * @return NetworkResult avec le piston mis à jour
     */
    suspend fun togglePiston(
        deviceId: String,
        pistonNumber: Int,
        currentState: String
    ): NetworkResult<Piston> {
        val action = if (currentState == Constants.STATE_ACTIVE) {
            Constants.ACTION_DEACTIVATE
        } else {
            Constants.ACTION_ACTIVATE
        }
        return controlPiston(deviceId, pistonNumber, action)
    }

    /**
     * Contrôler un piston (fonction interne)
     *
     * @param deviceId L'ID de l'appareil
     * @param pistonNumber Le numéro du piston (1-8)
     * @param action L'action à effectuer (activate/deactivate)
     * @return NetworkResult avec le piston mis à jour
     */
    private suspend fun controlPiston(
        deviceId: String,
        pistonNumber: Int,
        action: String
    ): NetworkResult<Piston> {
        return withContext(Dispatchers.IO) {
            val result = ApiClient.safeApiCall {
                apiService.controlPiston(
                    deviceId = deviceId,
                    pistonNumber = pistonNumber,
                    request = PistonControlRequest(action)
                )
            }

            // Transformer PistonControlResponse en Piston
            when (result) {
                is NetworkResult.Success -> NetworkResult.Success(result.data.piston)
                is NetworkResult.Error -> NetworkResult.Error(result.message, result.code)
                is NetworkResult.Loading -> NetworkResult.Loading
                is NetworkResult.Idle -> NetworkResult.Idle
            }
        }
    }
}
