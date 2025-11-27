package com.example.myapplicationv10.model

import com.google.gson.annotations.SerializedName

/**
 * API Request Models
 */

/**
 * LoginRequest - Corps de la requête pour l'authentification
 */
data class LoginRequest(
    @SerializedName("email")
    val email: String,

    @SerializedName("password")
    val password: String
)

/**
 * RegisterRequest - Corps de la requête pour l'inscription
 */
data class RegisterRequest(
    @SerializedName("email")
    val email: String,

    @SerializedName("password")
    val password: String
)

/**
 * PistonControlRequest - Corps de la requête pour contrôler un piston
 */
data class PistonControlRequest(
    @SerializedName("action")
    val action: String // "activate" ou "deactivate"
)
