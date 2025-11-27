package com.example.myapplicationv10.model

import com.google.gson.annotations.SerializedName

/**
 * Piston - Modèle représentant un piston/vanne
 *
 * Note: Le champ `id` est optionnel car:
 * - GET /devices retourne les pistons sans ID
 * - POST /devices/{deviceId}/pistons/{pistonNumber} retourne le piston avec ID
 */
data class Piston(
    @SerializedName("id")
    val id: String? = null,

    @SerializedName("piston_number")
    val pistonNumber: Int,

    @SerializedName("state")
    val state: String, // "active" ou "inactive"

    @SerializedName("last_triggered")
    val lastTriggered: String? = null
)
