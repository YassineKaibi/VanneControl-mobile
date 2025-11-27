package com.example.myapplicationv10.model

import com.google.gson.annotations.SerializedName

/**
 * API Response Models
 */

/**
 * AuthResponse - Réponse de l'authentification (login/register)
 * Matches backend's LoginResponse format
 */
data class AuthResponse(
    @SerializedName("token")
    val token: String,

    @SerializedName("userId")
    val userId: String
)

/**
 * DevicesResponse - Réponse contenant la liste des appareils
 */
data class DevicesResponse(
    @SerializedName("devices")
    val devices: List<Device>
)

/**
 * DeviceResponse - Réponse contenant un seul appareil
 */
data class DeviceResponse(
    @SerializedName("device")
    val device: Device
)

/**
 * PistonControlResponse - Réponse après contrôle d'un piston
 */
data class PistonControlResponse(
    @SerializedName("message")
    val message: String,

    @SerializedName("piston")
    val piston: Piston
)

/**
 * HealthResponse - Réponse du health check
 */
data class HealthResponse(
    @SerializedName("status")
    val status: String,

    @SerializedName("timestamp")
    val timestamp: Long
)

/**
 * ErrorResponse - Réponse d'erreur générique
 */
data class ErrorResponse(
    @SerializedName("error")
    val error: String? = null,

    @SerializedName("message")
    val message: String? = null
)
