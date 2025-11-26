package com.example.myapplicationv10

import android.content.Context
import android.util.Log
import com.hivemq.client.mqtt.MqttClient
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient
import com.hivemq.client.mqtt.mqtt3.message.connect.connack.Mqtt3ConnAck
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3Publish
import com.hivemq.client.mqtt.datatypes.MqttQos
import java.nio.charset.StandardCharsets
import java.util.concurrent.ConcurrentHashMap

/**
 * MqttManager - Gestion de la communication MQTT avec multithreading
 *
 * Gère la connexion MQTT pour contrôler les vannes via Raspberry Pi
 * Tous les appels sont thread-safe et asynchrones
 *
 * Utilise HiveMQ MQTT Client (moderne et maintenu)
 */
class MqttManager private constructor(private val context: Context) {

    companion object {
        private const val TAG = "MqttManager"
        private const val BROKER_HOST = "localhost" // À configurer avec votre broker
        private const val BROKER_PORT = 1883
        private const val CLIENT_ID_PREFIX = "VanneControlApp"

        @Volatile
        private var instance: MqttManager? = null

        fun getInstance(context: Context): MqttManager {
            return instance ?: synchronized(this) {
                instance ?: MqttManager(context.applicationContext).also { instance = it }
            }
        }
    }

    private var mqttClient: Mqtt3AsyncClient? = null
    private val messageCallbacks = ConcurrentHashMap<String, (String) -> Unit>()
    private var isConnected = false

    // Callback pour les événements de connexion
    interface ConnectionCallback {
        fun onConnectionSuccess()
        fun onConnectionFailure(error: String)
        fun onConnectionLost()
    }

    private var connectionCallback: ConnectionCallback? = null

    /**
     * Se connecter au broker MQTT
     */
    fun connect(
        deviceId: String,
        username: String? = null,
        password: String? = null,
        callback: ConnectionCallback
    ) {
        this.connectionCallback = callback

        // Connexion sur un thread réseau
        ThreadManager.executeNetworkTask {
            try {
                // Créer le client MQTT HiveMQ
                val clientBuilder = MqttClient.builder()
                    .useMqttVersion3()
                    .identifier("$CLIENT_ID_PREFIX-$deviceId")
                    .serverHost(BROKER_HOST)
                    .serverPort(BROKER_PORT)
                    .automaticReconnect()
                    .initialDelay(1, java.util.concurrent.TimeUnit.SECONDS)
                    .maxDelay(30, java.util.concurrent.TimeUnit.SECONDS)
                    .applyAutomaticReconnect()

                mqttClient = clientBuilder.buildAsync()

                // Configurer la connexion
                val connectBuilder = mqttClient!!.connectWith()
                    .cleanSession(true)
                    .keepAlive(60)

                // Ajouter l'authentification si fournie
                if (username != null && password != null) {
                    connectBuilder.simpleAuth()
                        .username(username)
                        .password(password.toByteArray(StandardCharsets.UTF_8))
                        .applySimpleAuth()
                }

                // Se connecter de manière asynchrone
                connectBuilder.send()
                    .whenComplete { connAck, throwable ->
                        if (throwable != null) {
                            Log.e(TAG, "❌ Erreur de connexion MQTT", throwable)
                            isConnected = false
                            ThreadManager.runOnMainThread {
                                callback.onConnectionFailure("Erreur MQTT: ${throwable.message}")
                            }
                        } else {
                            Log.i(TAG, "✅ Connecté au broker MQTT")
                            isConnected = true

                            ThreadManager.runOnMainThread {
                                callback.onConnectionSuccess()
                            }

                            // S'abonner aux topics
                            subscribeToTopics(deviceId)
                        }
                    }

            } catch (e: Exception) {
                Log.e(TAG, "❌ Erreur lors de la création du client MQTT", e)
                isConnected = false

                ThreadManager.runOnMainThread {
                    callback.onConnectionFailure("Erreur: ${e.message}")
                }
            }
        }
    }

    /**
     * S'abonner aux topics nécessaires
     */
    private fun subscribeToTopics(deviceId: String) {
        try {
            // S'abonner au status des valves
            mqttClient?.subscribeWith()
                ?.topicFilter("devices/$deviceId/status")
                ?.qos(MqttQos.AT_LEAST_ONCE)
                ?.callback { publish ->
                    handleIncomingMessage("devices/$deviceId/status", publish)
                }
                ?.send()
                ?.whenComplete { _, throwable ->
                    if (throwable != null) {
                        Log.e(TAG, "❌ Erreur d'abonnement status", throwable)
                    } else {
                        Log.i(TAG, "✅ Abonné au topic status")
                    }
                }

            // S'abonner à la télémétrie
            mqttClient?.subscribeWith()
                ?.topicFilter("devices/$deviceId/telemetry")
                ?.qos(MqttQos.AT_LEAST_ONCE)
                ?.callback { publish ->
                    handleIncomingMessage("devices/$deviceId/telemetry", publish)
                }
                ?.send()
                ?.whenComplete { _, throwable ->
                    if (throwable != null) {
                        Log.e(TAG, "❌ Erreur d'abonnement telemetry", throwable)
                    } else {
                        Log.i(TAG, "✅ Abonné au topic telemetry")
                    }
                }

        } catch (e: Exception) {
            Log.e(TAG, "❌ Erreur lors de l'abonnement", e)
        }
    }

    /**
     * Publier une commande pour contrôler une valve
     */
    fun publishValveCommand(
        deviceId: String,
        valveId: Int,
        action: String, // "open" ou "close"
        onSuccess: (() -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) {
        if (!isConnected) {
            onError?.invoke("Non connecté au broker MQTT")
            return
        }

        ThreadManager.executeNetworkTask {
            try {
                val topic = "devices/$deviceId/commands"
                val payload = """
                    {
                        "valve": $valveId,
                        "action": "$action",
                        "timestamp": ${System.currentTimeMillis()}
                    }
                """.trimIndent()

                mqttClient?.publishWith()
                    ?.topic(topic)
                    ?.payload(payload.toByteArray(StandardCharsets.UTF_8))
                    ?.qos(MqttQos.AT_LEAST_ONCE)
                    ?.retain(false)
                    ?.send()
                    ?.whenComplete { _, throwable ->
                        if (throwable != null) {
                            Log.e(TAG, "❌ Erreur d'envoi de commande", throwable)
                            ThreadManager.runOnMainThread {
                                onError?.invoke("Erreur MQTT: ${throwable.message}")
                            }
                        } else {
                            Log.i(TAG, "✅ Commande envoyée: Valve $valveId -> $action")
                            ThreadManager.runOnMainThread {
                                onSuccess?.invoke()
                            }
                        }
                    }

            } catch (e: Exception) {
                Log.e(TAG, "❌ Erreur lors de la publication", e)
                ThreadManager.runOnMainThread {
                    onError?.invoke("Erreur: ${e.message}")
                }
            }
        }
    }

    /**
     * Gérer les messages entrants
     */
    private fun handleIncomingMessage(topic: String, publish: Mqtt3Publish) {
        val payload = String(
            publish.payloadAsBytes,
            StandardCharsets.UTF_8
        )

        Log.d(TAG, "📥 Message reçu: $topic -> $payload")

        // Traiter sur un thread d'arrière-plan
        ThreadManager.executeBackgroundTask {
            try {
                // TODO: Parser le JSON avec Gson
                // val data = gson.fromJson(payload, ValveStatus::class.java)

                // Notifier les listeners enregistrés
                messageCallbacks[topic]?.let { callback ->
                    ThreadManager.runOnMainThread {
                        callback(payload)
                    }
                }

            } catch (e: Exception) {
                Log.e(TAG, "❌ Erreur de traitement du message", e)
            }
        }
    }

    /**
     * Enregistrer un callback pour un topic spécifique
     */
    fun registerMessageCallback(topic: String, callback: (String) -> Unit) {
        messageCallbacks[topic] = callback
    }

    /**
     * Désinscrire un callback
     */
    fun unregisterMessageCallback(topic: String) {
        messageCallbacks.remove(topic)
    }

    /**
     * Se déconnecter du broker
     */
    fun disconnect() {
        ThreadManager.executeNetworkTask {
            try {
                if (isConnected) {
                    mqttClient?.disconnect()
                        ?.whenComplete { _, throwable ->
                            if (throwable != null) {
                                Log.e(TAG, "❌ Erreur de déconnexion", throwable)
                            } else {
                                Log.i(TAG, "✅ Déconnecté du broker MQTT")
                            }
                            isConnected = false
                        }
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Erreur lors de la déconnexion", e)
            }
        }
    }

    /**
     * Vérifier si connecté
     */
    fun isConnected(): Boolean = isConnected

    // Data classes pour le parsing JSON (à utiliser avec Gson)
    data class ValveStatus(
        val valveId: Int,
        val state: String,
        val timestamp: Long
    )

    data class Telemetry(
        val valveId: Int,
        val event: String,
        val data: Map<String, Any>,
        val timestamp: Long
    )
}

/**
 * Extensions pour faciliter l'utilisation dans les activités
 */
fun Context.getMqttManager(): MqttManager {
    return MqttManager.getInstance(this)
}