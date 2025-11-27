package com.example.myapplicationv10.model

import com.google.gson.annotations.SerializedName

/**
 * Device - Modèle représentant un appareil ESP32
 */
data class Device(
    @SerializedName("id")
    val id: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("device_id")
    val deviceId: String,

    @SerializedName("status")
    val status: String, // "online" ou "offline"

    @SerializedName("last_seen")
    val lastSeen: String? = null,

    @SerializedName("pistons")
    val pistons: List<Piston> = emptyList()
)
