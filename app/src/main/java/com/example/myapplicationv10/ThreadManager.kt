package com.example.myapplicationv10

import android.os.Handler
import android.os.Looper
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future

/**
 * ThreadManager - Gestion centralisée des threads pour VanneControl
 *
 * Gère tous les appels asynchrones pour :
 * - Requêtes API/Backend
 * - Communication MQTT
 * - Opérations base de données
 * - Traitement de données lourdes
 */
object ThreadManager {

    // Main Thread Handler pour les mises à jour UI
    private val mainHandler = Handler(Looper.getMainLooper())

    // Thread pool pour les opérations réseau (API/MQTT)
    private val networkExecutor: ExecutorService = Executors.newFixedThreadPool(3)

    // Thread pool pour les opérations base de données
    private val databaseExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    // Thread pool pour les opérations générales (calculs, parsing, etc.)
    private val backgroundExecutor: ExecutorService = Executors.newFixedThreadPool(2)

    /**
     * Exécute une opération réseau (API REST, MQTT) en arrière-plan
     *
     * @param task La tâche à exécuter
     * @return Future permettant d'annuler la tâche si nécessaire
     */
    fun executeNetworkTask(task: () -> Unit): Future<*> {
        return networkExecutor.submit {
            try {
                task()
            } catch (e: Exception) {
                e.printStackTrace()
                runOnMainThread {
                    // Gérer l'erreur sur le thread principal si nécessaire
                }
            }
        }
    }

    /**
     * Exécute une opération base de données en arrière-plan
     *
     * @param task La tâche à exécuter
     * @return Future permettant d'annuler la tâche si nécessaire
     */
    fun executeDatabaseTask(task: () -> Unit): Future<*> {
        return databaseExecutor.submit {
            try {
                task()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Exécute une opération générale en arrière-plan
     *
     * @param task La tâche à exécuter
     * @return Future permettant d'annuler la tâche si nécessaire
     */
    fun executeBackgroundTask(task: () -> Unit): Future<*> {
        return backgroundExecutor.submit {
            try {
                task()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Exécute du code sur le thread principal (pour mettre à jour l'UI)
     *
     * @param task La tâche à exécuter sur le thread principal
     */
    fun runOnMainThread(task: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            // Déjà sur le thread principal
            task()
        } else {
            // Post vers le thread principal
            mainHandler.post { task() }
        }
    }

    /**
     * Exécute du code sur le thread principal après un délai
     *
     * @param delayMillis Délai en millisecondes
     * @param task La tâche à exécuter
     */
    fun runOnMainThreadDelayed(delayMillis: Long, task: () -> Unit) {
        mainHandler.postDelayed({ task() }, delayMillis)
    }

    /**
     * Exécute une tâche en arrière-plan puis met à jour l'UI avec le résultat
     *
     * @param backgroundTask Tâche à exécuter en arrière-plan, retourne un résultat
     * @param onResult Callback appelé sur le thread principal avec le résultat
     */
    fun <T> executeWithResult(
        backgroundTask: () -> T,
        onResult: (T) -> Unit
    ): Future<*> {
        return backgroundExecutor.submit {
            try {
                val result = backgroundTask()
                runOnMainThread {
                    onResult(result)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnMainThread {
                    // Gérer l'erreur
                }
            }
        }
    }

    /**
     * Arrête tous les threads proprement
     * À appeler lors de la fermeture de l'application
     */
    fun shutdown() {
        networkExecutor.shutdown()
        databaseExecutor.shutdown()
        backgroundExecutor.shutdown()
    }
}

/**
 * Extensions pratiques pour les activités
 */
fun Any.runOnBackground(task: () -> Unit) {
    ThreadManager.executeBackgroundTask(task)
}

fun Any.runOnNetwork(task: () -> Unit) {
    ThreadManager.executeNetworkTask(task)
}

fun Any.runOnDatabase(task: () -> Unit) {
    ThreadManager.executeDatabaseTask(task)
}

fun Any.runOnUiThread(task: () -> Unit) {
    ThreadManager.runOnMainThread(task)
}

fun <T> Any.runWithResult(
    backgroundTask: () -> T,
    onResult: (T) -> Unit
) {
    ThreadManager.executeWithResult(backgroundTask, onResult)
}