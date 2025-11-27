package com.example.myapplicationv10

import kotlinx.coroutines.*

/**
 * ThreadManager - Gestion centralisée des coroutines pour VanneControl
 *
 * Gère tous les appels asynchrones pour :
 * - Requêtes API/Backend
 * - Communication MQTT
 * - Opérations base de données
 * - Traitement de données lourdes
 *
 * Utilise Kotlin Coroutines pour une meilleure performance et gestion de la concurrence
 */
object ThreadManager {

    // Custom dispatchers pour différents types d'opérations
    private val networkDispatcher = Dispatchers.IO.limitedParallelism(3)
    private val databaseDispatcher = Dispatchers.IO.limitedParallelism(1)
    private val backgroundDispatcher = Dispatchers.Default.limitedParallelism(2)

    // Scope global pour les opérations qui doivent survivre au cycle de vie des composants
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    /**
     * Exécute une opération réseau (API REST, MQTT) en arrière-plan
     *
     * @param task La tâche à exécuter
     * @return Job permettant d'annuler la tâche si nécessaire
     */
    fun executeNetworkTask(task: suspend () -> Unit): Job {
        return applicationScope.launch(networkDispatcher) {
            try {
                task()
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    // Gérer l'erreur sur le thread principal si nécessaire
                }
            }
        }
    }

    /**
     * Exécute une opération base de données en arrière-plan
     *
     * @param task La tâche à exécuter
     * @return Job permettant d'annuler la tâche si nécessaire
     */
    fun executeDatabaseTask(task: suspend () -> Unit): Job {
        return applicationScope.launch(databaseDispatcher) {
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
     * @return Job permettant d'annuler la tâche si nécessaire
     */
    fun executeBackgroundTask(task: suspend () -> Unit): Job {
        return applicationScope.launch(backgroundDispatcher) {
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
    fun runOnMainThread(task: suspend () -> Unit) {
        applicationScope.launch(Dispatchers.Main) {
            task()
        }
    }

    /**
     * Exécute du code sur le thread principal après un délai
     *
     * @param delayMillis Délai en millisecondes
     * @param task La tâche à exécuter
     */
    fun runOnMainThreadDelayed(delayMillis: Long, task: suspend () -> Unit): Job {
        return applicationScope.launch(Dispatchers.Main) {
            delay(delayMillis)
            task()
        }
    }

    /**
     * Exécute une tâche en arrière-plan puis met à jour l'UI avec le résultat
     *
     * @param backgroundTask Tâche à exécuter en arrière-plan, retourne un résultat
     * @param onResult Callback appelé sur le thread principal avec le résultat
     */
    fun <T> executeWithResult(
        backgroundTask: suspend () -> T,
        onResult: suspend (T) -> Unit
    ): Job {
        return applicationScope.launch(backgroundDispatcher) {
            try {
                val result = backgroundTask()
                withContext(Dispatchers.Main) {
                    onResult(result)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    // Gérer l'erreur
                }
            }
        }
    }

    /**
     * Suspend function pour exécuter une tâche réseau
     * À utiliser dans un contexte coroutine existant
     */
    suspend fun <T> withNetworkContext(task: suspend () -> T): T {
        return withContext(networkDispatcher) {
            task()
        }
    }

    /**
     * Suspend function pour exécuter une tâche base de données
     * À utiliser dans un contexte coroutine existant
     */
    suspend fun <T> withDatabaseContext(task: suspend () -> T): T {
        return withContext(databaseDispatcher) {
            task()
        }
    }

    /**
     * Suspend function pour exécuter une tâche d'arrière-plan
     * À utiliser dans un contexte coroutine existant
     */
    suspend fun <T> withBackgroundContext(task: suspend () -> T): T {
        return withContext(backgroundDispatcher) {
            task()
        }
    }

    /**
     * Annule toutes les coroutines en cours
     * À appeler lors de la fermeture de l'application
     */
    fun shutdown() {
        applicationScope.cancel()
    }
}

/**
 * Extensions pratiques pour les activités et autres composants
 */
fun CoroutineScope.runOnBackground(task: suspend () -> Unit): Job {
    return launch(Dispatchers.Default) {
        task()
    }
}

fun CoroutineScope.runOnNetwork(task: suspend () -> Unit): Job {
    return launch(Dispatchers.IO) {
        task()
    }
}

fun CoroutineScope.runOnDatabase(task: suspend () -> Unit): Job {
    return launch(Dispatchers.IO) {
        task()
    }
}

fun CoroutineScope.runOnUiThread(task: suspend () -> Unit): Job {
    return launch(Dispatchers.Main) {
        task()
    }
}

fun <T> CoroutineScope.runWithResult(
    backgroundTask: suspend () -> T,
    onResult: suspend (T) -> Unit
): Job {
    return launch {
        try {
            val result = withContext(Dispatchers.Default) {
                backgroundTask()
            }
            withContext(Dispatchers.Main) {
                onResult(result)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
