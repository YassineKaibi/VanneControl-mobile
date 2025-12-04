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
    val role: String? = "user",

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
    val avatarUrl: String? = null,

    @SerializedName("preferences")
    val preferences: String? = "{}"
)
