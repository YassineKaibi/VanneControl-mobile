package com.example.myapplicationv10.utils

/**
 * Constants - Configuration globale de l'application
 */
object Constants {

    // =====================
    // API CONFIGURATION
    // =====================

    /**
     * Base URL de l'API backend
     *
     * IMPORTANT:
     * - Pour développement local: "http://10.0.2.2:8080/api/" (émulateur Android)
     * - Pour appareil physique: "http://YOUR_COMPUTER_IP:8080/api/"
     * - Pour production: MUST use HTTPS: "https://your-domain.com/api/"
     *
     * NOTE: android:usesCleartextTraffic has been removed from AndroidManifest.xml
     * For development, you'll need to either:
     * 1. Use HTTPS with a self-signed certificate, OR
     * 2. Add network_security_config.xml to allow cleartext for development only
     */
    const val BASE_URL = "https://vannecontrol.swedencentral.cloudapp.azure.com/api/"

    /**
     * Timeout pour les connexions réseau (en secondes)
     */
    const val CONNECT_TIMEOUT = 30L
    const val READ_TIMEOUT = 30L
    const val WRITE_TIMEOUT = 30L

    // =====================
    // AUTHENTICATION
    // =====================

    /**
     * Préfixes pour les headers HTTP
     */
    const val AUTHORIZATION_HEADER = "Authorization"
    const val BEARER_PREFIX = "Bearer "

    /**
     * SharedPreferences keys
     */
    const val PREFS_NAME = "piston_control_prefs"
    const val PREFS_AUTH_TOKEN = "auth_token"
    const val PREFS_USER_ID = "user_id"
    const val PREFS_USER_EMAIL = "user_email"
    const val PREFS_VALVE_LIMIT = "valve_limit"

    // =====================
    // DEVICE & PISTON
    // =====================

    /**
     * Nombre de pistons par appareil
     */
    const val PISTONS_PER_DEVICE = 8

    /**
     * Actions disponibles pour les pistons
     */
    const val ACTION_ACTIVATE = "activate"
    const val ACTION_DEACTIVATE = "deactivate"

    /**
     * États des pistons
     */
    const val STATE_ACTIVE = "active"
    const val STATE_INACTIVE = "inactive"

    /**
     * États des appareils
     */
    const val STATUS_ONLINE = "online"
    const val STATUS_OFFLINE = "offline"

    // =====================
    // WEBSOCKET
    // =====================

    /**
     * URL du WebSocket pour les mises à jour en temps réel
     *
     * IMPORTANT:
     * - Pour développement local: "ws://10.0.2.2:8080/ws"
     * - Pour production: Remplacer par URL WSS réelle
     *
     * NOTE: WebSocket endpoint may or may not need /api prefix - check backend routing
     */
    const val WEBSOCKET_URL = "wss://vannecontrol.swedencentral.cloudapp.azure.com/ws"

    /**
     * Types de messages WebSocket
     */
    const val WS_TYPE_PISTON_UPDATE = "piston_update"
    const val WS_TYPE_DEVICE_STATUS = "device_status"

    // =====================
    // ERROR MESSAGES
    // =====================

    const val ERROR_NO_INTERNET = "Pas de connexion Internet"
    const val ERROR_SERVER_UNREACHABLE = "Impossible de contacter le serveur"
    const val ERROR_UNAUTHORIZED = "Session expirée, veuillez vous reconnecter"
    const val ERROR_UNKNOWN = "Une erreur inconnue s'est produite"

    // =====================
    // LOGGING
    // =====================

    /**
     * Activer les logs réseau (désactiver en production)
     */
    const val ENABLE_NETWORK_LOGS = true

    // =====================
    // AVATAR CONFIGURATION
    // =====================

    /**
     * Base domain for the backend (without /api suffix)
     */
    private const val BASE_DOMAIN = "https://vannecontrol.swedencentral.cloudapp.azure.com"

    /**
     * Fix avatar URLs returned by backend
     *
     * Handles multiple cases:
     * - localhost URLs → production domain
     * - HTTP URLs → HTTPS (for security)
     * - Malformed URLs → null
     *
     * @param url Avatar URL from backend (may be null, contain localhost, or use HTTP)
     * @return Fixed HTTPS URL accessible from mobile devices, or null
     */
    fun fixAvatarUrl(url: String?): String? {
        if (url.isNullOrEmpty()) return null

        var fixedUrl = url

        // Replace localhost URLs with production domain
        fixedUrl = fixedUrl
            .replace("http://localhost:8080", BASE_DOMAIN)
            .replace("https://localhost:8080", BASE_DOMAIN)
            .replace("http://127.0.0.1:8080", BASE_DOMAIN)
            .replace("https://127.0.0.1:8080", BASE_DOMAIN)

        // Ensure HTTPS for production domain (security requirement)
        if (fixedUrl.startsWith("http://vannecontrol.swedencentral.cloudapp.azure.com")) {
            fixedUrl = fixedUrl.replace(
                "http://vannecontrol.swedencentral.cloudapp.azure.com",
                "https://vannecontrol.swedencentral.cloudapp.azure.com"
            )
        }

        // Validate URL format
        return if (fixedUrl.startsWith("http://") || fixedUrl.startsWith("https://")) {
            fixedUrl
        } else {
            null
        }
    }
}
