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

/**
 * ScheduleResponse - Réponse contenant un planning
 */
data class ScheduleResponse(
    @SerializedName("id")
    val id: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("deviceId")
    val deviceId: String,

    @SerializedName("pistonNumber")
    val pistonNumber: Int,

    @SerializedName("action")
    val action: String,

    @SerializedName("cronExpression")
    val cronExpression: String,

    @SerializedName("enabled")
    val enabled: Boolean,

    @SerializedName("userId")
    val userId: String,

    @SerializedName("createdAt")
    val createdAt: String,

    @SerializedName("updatedAt")
    val updatedAt: String
)

/**
 * SchedulesListResponse - Réponse contenant la liste des plannings
 */
data class SchedulesListResponse(
    @SerializedName("schedules")
    val schedules: List<ScheduleResponse>
)

/**
 * DeleteScheduleResponse - Réponse après suppression d'un planning
 */
data class DeleteScheduleResponse(
    @SerializedName("message")
    val message: String
)
