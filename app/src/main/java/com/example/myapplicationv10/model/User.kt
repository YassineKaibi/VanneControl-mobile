package com.example.myapplicationv10.model

import com.google.gson.annotations.SerializedName

/**
 * User - Modèle représentant un utilisateur
 */
data class User(
    @SerializedName("id")
    val id: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("role")
    val role: String? = "user"
)
