package com.example.myapplicationv10.model

import com.google.gson.annotations.SerializedName

/**
 * Piston - Modèle représentant un piston/vanne
 */
data class Piston(
    @SerializedName("id")
    val id: String,

    @SerializedName("piston_number")
    val pistonNumber: Int,

    @SerializedName("state")
    val state: String, // "active" ou "inactive"

    @SerializedName("last_triggered")
    val lastTriggered: String? = null
)
