package com.example.myapplicationv10.websocket

import android.content.Context
import android.util.Log
import com.example.myapplicationv10.utils.Constants
import com.example.myapplicationv10.utils.TokenManager
import com.google.gson.Gson
import kotlinx.coroutines.*
import okhttp3.*
import java.util.concurrent.TimeUnit

/**
 * WebSocketManager - Gestion de la connexion WebSocket pour les mises à jour en temps réel
 *
 * Gère la connexion WebSocket avec le backend pour recevoir:
 * - Les mises à jour d'état des pistons
 * - Les changements de statut des appareils
 *
 * Singleton avec reconnexion automatique utilisant Kotlin Coroutines
 */
class WebSocketManager private constructor(private val context: Context) {

    companion object {
        private const val TAG = "WebSocketManager"
        private const val RECONNECT_DELAY_MS = 5000L // 5 secondes

        @Volatile
        private var instance: WebSocketManager? = null

        fun getInstance(context: Context): WebSocketManager {
            return instance ?: synchronized(this) {
                instance ?: WebSocketManager(context.applicationContext).also { instance = it }
            }
        }
    }

    private val gson = Gson()
    private val tokenManager = TokenManager.getInstance(context)
    private var webSocket: WebSocket? = null
    private var isConnected = false
    private var shouldReconnect = true

    // CoroutineScope pour gérer les opérations asynchrones
    private val webSocketScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var reconnectJob: Job? = null

    // Listeners pour les différents types de messages
    private val pistonUpdateListeners = mutableListOf<(PistonUpdateMessage) -> Unit>()
    private val deviceStatusListeners = mutableListOf<(DeviceStatusMessage) -> Unit>()
    private val connectionListeners = mutableListOf<(Boolean) -> Unit>()

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.MILLISECONDS) // Pas de timeout pour WebSocket
        .build()

    /**
     * Se connecter au WebSocket
     */
    fun connect() {
        if (isConnected) {
            Log.d(TAG, "WebSocket déjà connecté")
            return
        }

        val token = tokenManager.getToken()
        if (token == null) {
            Log.w(TAG, "Impossible de se connecter au WebSocket: pas de token")
            return
        }

        shouldReconnect = true

        // Construire la requête WebSocket avec le token
        val request = Request.Builder()
            .url(Constants.WEBSOCKET_URL)
            .addHeader("Authorization", "${Constants.BEARER_PREFIX}$token")
            .build()

        // Créer le listener WebSocket
        val listener = object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.i(TAG, "✅ WebSocket connecté")
                isConnected = true
                notifyConnectionListeners(true)
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d(TAG, "📥 Message WebSocket reçu: $text")
                handleMessage(text)
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                Log.i(TAG, "WebSocket en cours de fermeture: $code - $reason")
                webSocket.close(1000, null)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.i(TAG, "❌ WebSocket fermé: $code - $reason")
                isConnected = false
                notifyConnectionListeners(false)

                // Tenter la reconnexion si nécessaire
                if (shouldReconnect) {
                    scheduleReconnect()
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e(TAG, "❌ Erreur WebSocket: ${t.message}", t)
                isConnected = false
                notifyConnectionListeners(false)

                // Tenter la reconnexion
                if (shouldReconnect) {
                    scheduleReconnect()
                }
            }
        }

        // Créer la connexion WebSocket
        webSocket = client.newWebSocket(request, listener)
    }

    /**
     * Déconnecter le WebSocket
     */
    fun disconnect() {
        shouldReconnect = false
        reconnectJob?.cancel()
        webSocket?.close(1000, "Déconnexion normale")
        webSocket = null
        isConnected = false
    }

    /**
     * Planifier une reconnexion avec coroutines
     */
    private fun scheduleReconnect() {
        Log.d(TAG, "Reconnexion programmée dans ${RECONNECT_DELAY_MS}ms")

        // Annuler toute reconnexion en cours
        reconnectJob?.cancel()

        // Programmer une nouvelle reconnexion
        reconnectJob = webSocketScope.launch(Dispatchers.Main) {
            delay(RECONNECT_DELAY_MS)
            if (shouldReconnect && !isConnected) {
                Log.d(TAG, "Tentative de reconnexion...")
                connect()
            }
        }
    }

    /**
     * Gérer les messages entrants
     */
    private fun handleMessage(text: String) {
        try {
            val message = gson.fromJson(text, WebSocketMessage::class.java)

            when (message.type) {
                Constants.WS_TYPE_PISTON_UPDATE -> {
                    val pistonUpdate = gson.fromJson(text, PistonUpdateMessage::class.java)
                    notifyPistonUpdateListeners(pistonUpdate)
                }

                Constants.WS_TYPE_DEVICE_STATUS -> {
                    val deviceStatus = gson.fromJson(text, DeviceStatusMessage::class.java)
                    notifyDeviceStatusListeners(deviceStatus)
                }

                else -> {
                    Log.w(TAG, "Type de message WebSocket inconnu: ${message.type}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors du parsing du message WebSocket", e)
        }
    }

    /**
     * Ajouter un listener pour les mises à jour de piston
     */
    fun addPistonUpdateListener(listener: (PistonUpdateMessage) -> Unit) {
        pistonUpdateListeners.add(listener)
    }

    /**
     * Retirer un listener pour les mises à jour de piston
     */
    fun removePistonUpdateListener(listener: (PistonUpdateMessage) -> Unit) {
        pistonUpdateListeners.remove(listener)
    }

    /**
     * Ajouter un listener pour les changements de statut d'appareil
     */
    fun addDeviceStatusListener(listener: (DeviceStatusMessage) -> Unit) {
        deviceStatusListeners.add(listener)
    }

    /**
     * Retirer un listener pour les changements de statut d'appareil
     */
    fun removeDeviceStatusListener(listener: (DeviceStatusMessage) -> Unit) {
        deviceStatusListeners.remove(listener)
    }

    /**
     * Ajouter un listener pour les changements de connexion
     */
    fun addConnectionListener(listener: (Boolean) -> Unit) {
        connectionListeners.add(listener)
    }

    /**
     * Retirer un listener pour les changements de connexion
     */
    fun removeConnectionListener(listener: (Boolean) -> Unit) {
        connectionListeners.remove(listener)
    }

    /**
     * Notifier les listeners de mise à jour de piston
     */
    private fun notifyPistonUpdateListeners(message: PistonUpdateMessage) {
        pistonUpdateListeners.forEach { it(message) }
    }

    /**
     * Notifier les listeners de changement de statut d'appareil
     */
    private fun notifyDeviceStatusListeners(message: DeviceStatusMessage) {
        deviceStatusListeners.forEach { it(message) }
    }

    /**
     * Notifier les listeners de changement de connexion
     */
    private fun notifyConnectionListeners(connected: Boolean) {
        connectionListeners.forEach { it(connected) }
    }

    /**
     * Vérifier si le WebSocket est connecté
     */
    fun isConnected(): Boolean = isConnected

    /**
     * Nettoyer les ressources et annuler toutes les coroutines
     */
    fun cleanup() {
        disconnect()
        webSocketScope.cancel()
    }
}
