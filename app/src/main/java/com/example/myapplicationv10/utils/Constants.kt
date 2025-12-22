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
     * - Pour développement local: "http://10.0.2.2:8080/" (émulateur Android)
     * - Pour appareil physique: "http://YOUR_COMPUTER_IP:8080/"
     * - Pour production: MUST use HTTPS: "https://your-domain.com/"
     *
     * NOTE: android:usesCleartextTraffic has been removed from AndroidManifest.xml
     * For development, you'll need to either:
     * 1. Use HTTPS with a self-signed certificate, OR
     * 2. Add network_security_config.xml to allow cleartext for development only
     */
    const val BASE_URL = "http://4.165.39.94:8080/"  // TODO: Change to HTTPS for production!

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
     * - Pour production: Remplacer par votre URL WSS réelle
     */
    const val WEBSOCKET_URL = "ws://4.165.39.94:8080/ws"

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
}
