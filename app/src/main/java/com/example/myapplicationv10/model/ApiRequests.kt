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

/**
 * UpdateProfileRequest - Corps de la requête pour mettre à jour le profil utilisateur
 */
data class UpdateProfileRequest(
    @SerializedName("firstName")
    val firstName: String? = null,

    @SerializedName("lastName")
    val lastName: String? = null,

    @SerializedName("phoneNumber")
    val phoneNumber: String? = null,

    @SerializedName("dateOfBirth")
    val dateOfBirth: String? = null,

    @SerializedName("location")
    val location: String? = null,

    @SerializedName("avatarUrl")
    val avatarUrl: String? = null
)

/**
 * UpdatePreferencesRequest - Corps de la requête pour mettre à jour les préférences
 */
data class UpdatePreferencesRequest(
    @SerializedName("preferences")
    val preferences: String
)
