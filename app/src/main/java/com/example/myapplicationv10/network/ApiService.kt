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
    // SCHEDULES
    // =====================

    /**
     * POST /schedules
     * Créer un nouveau planning automatisé
     * Requires: Authorization header avec JWT token
     */
    @POST("schedules")
    suspend fun createSchedule(
        @Body request: CreateScheduleRequest
    ): Response<ScheduleResponse>

    /**
     * GET /schedules
     * Récupérer tous les plannings de l'utilisateur
     * Requires: Authorization header avec JWT token
     */
    @GET("schedules")
    suspend fun getSchedules(): Response<SchedulesListResponse>

    /**
     * GET /schedules/{id}
     * Récupérer un planning spécifique par ID
     * Requires: Authorization header avec JWT token
     */
    @GET("schedules/{id}")
    suspend fun getSchedule(
        @Path("id") scheduleId: String
    ): Response<ScheduleResponse>

    /**
     * PUT /schedules/{id}
     * Mettre à jour un planning existant
     * Requires: Authorization header avec JWT token
     */
    @PUT("schedules/{id}")
    suspend fun updateSchedule(
        @Path("id") scheduleId: String,
        @Body request: UpdateScheduleRequest
    ): Response<ScheduleResponse>

    /**
     * DELETE /schedules/{id}
     * Supprimer un planning
     * Requires: Authorization header avec JWT token
     */
    @DELETE("schedules/{id}")
    suspend fun deleteSchedule(
        @Path("id") scheduleId: String
    ): Response<DeleteScheduleResponse>

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
