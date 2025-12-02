package com.example.myapplicationv10.network

import com.example.myapplicationv10.model.*
import retrofit2.Response
import retrofit2.http.*

/**
 * ApiService - Interface Retrofit définissant tous les endpoints de l'API backend
 *
 * Base URL: http://your-server.com:8080
 * Documentation: Voir context.md pour les détails complets
 */
interface ApiService {

    // =====================
    // AUTHENTICATION
    // =====================

    /**
     * POST /auth/register
     * Créer un nouveau compte utilisateur
     */
    @POST("auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<AuthResponse>

    /**
     * POST /auth/login
     * Se connecter et obtenir un JWT token
     */
    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<AuthResponse>

    // =====================
    // USER PROFILE
    // =====================

    /**
     * GET /user/profile
     * Récupérer le profil de l'utilisateur connecté
     * Requires: Authorization header avec JWT token
     */
    @GET("user/profile")
    suspend fun getUserProfile(): Response<User>

    /**
     * PUT /user/profile
     * Mettre à jour le profil de l'utilisateur connecté
     * Requires: Authorization header avec JWT token
     */
    @PUT("user/profile")
    suspend fun updateUserProfile(
        @Body request: UpdateProfileRequest
    ): Response<User>

    /**
     * PUT /user/preferences
     * Mettre à jour les préférences de l'utilisateur
     * Requires: Authorization header avec JWT token
     */
    @PUT("user/preferences")
    suspend fun updateUserPreferences(
        @Body request: UpdatePreferencesRequest
    ): Response<User>

    // =====================
    // DEVICES
    // =====================

    /**
     * GET /devices
     * Récupérer la liste de tous les appareils de l'utilisateur
     * Requires: Authorization header avec JWT token
     */
    @GET("devices")
    suspend fun getDevices(): Response<DevicesResponse>

    /**
     * GET /devices/{deviceId}
     * Récupérer les détails d'un appareil spécifique
     * Requires: Authorization header avec JWT token
     *
     * @param deviceId L'ID unique de l'appareil
     */
    @GET("devices/{deviceId}")
    suspend fun getDevice(
        @Path("deviceId") deviceId: String
    ): Response<DeviceResponse>

    // =====================
    // PISTON CONTROL
    // =====================

    /**
     * POST /devices/{deviceId}/pistons/{pistonNumber}
     * Contrôler un piston spécifique (activer/désactiver)
     * Requires: Authorization header avec JWT token
     *
     * @param deviceId L'ID unique de l'appareil
     * @param pistonNumber Le numéro du piston (1-8)
     * @param request L'action à effectuer (activate/deactivate)
     */
    @POST("devices/{deviceId}/pistons/{pistonNumber}")
    suspend fun controlPiston(
        @Path("deviceId") deviceId: String,
        @Path("pistonNumber") pistonNumber: Int,
        @Body request: PistonControlRequest
    ): Response<PistonControlResponse>

    // =====================
    // HEALTH CHECK
    // =====================

    /**
     * GET /health
     * Vérifier l'état de santé du backend
     * No authentication required
     */
    @GET("health")
    suspend fun healthCheck(): Response<HealthResponse>
}
