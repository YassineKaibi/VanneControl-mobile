package com.example.myapplicationv10.websocket

import com.google.gson.annotations.SerializedName

/**
 * WebSocketMessage - Modèles pour les messages WebSocket
 */

/**
 * Message générique WebSocket
 */
data class WebSocketMessage(
    @SerializedName("type")
    val type: String,

    @SerializedName("device_id")
    val deviceId: String? = null,

    @SerializedName("piston_number")
    val pistonNumber: Int? = null,

    @SerializedName("state")
    val state: String? = null,

    @SerializedName("status")
    val status: String? = null,

    @SerializedName("timestamp")
    val timestamp: Long? = null
)

/**
 * Message de mise à jour de piston
 * Type: "piston_update"
 *
 * Note: Le serveur envoie timestamp en millisecondes (Long)
 */
data class PistonUpdateMessage(
    @SerializedName("device_id")
    val deviceId: String,

    @SerializedName("piston_number")
    val pistonNumber: Int,

    @SerializedName("state")
    val state: String, // "active" ou "inactive"

    @SerializedName("timestamp")
    val timestamp: Long
)

/**
 * Message de statut d'appareil
 * Type: "device_status"
 *
 * Note: Le serveur envoie timestamp en millisecondes (Long)
 */
data class DeviceStatusMessage(
    @SerializedName("device_id")
    val deviceId: String,

    @SerializedName("status")
    val status: String, // "online" ou "offline"

    @SerializedName("timestamp")
    val timestamp: Long
)
