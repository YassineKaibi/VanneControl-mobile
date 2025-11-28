package com.example.myapplicationv10.network

/**
 * NetworkResult - Sealed class pour gérer les résultats d'appels réseau
 *
 * Utilisé pour encapsuler les états de chargement, succès et erreur
 * des requêtes API de manière type-safe
 */
sealed class NetworkResult<out T> {
    /**
     * État initial/repos - Aucune opération en cours
     */
    object Idle : NetworkResult<Nothing>()

    /**
     * État de chargement - Requête en cours
     */
    object Loading : NetworkResult<Nothing>()

    /**
     * État de succès - Requête réussie avec des données
     * @param data Les données retournées par l'API
     */
    data class Success<T>(val data: T) : NetworkResult<T>()

    /**
     * État d'erreur - Requête échouée
     * @param message Message d'erreur à afficher
     * @param code Code d'erreur HTTP (optionnel)
     */
    data class Error(
        val message: String,
        val code: Int? = null
    ) : NetworkResult<Nothing>()
}

/**
 * Extension pour exécuter du code uniquement en cas de succès
 */
inline fun <T> NetworkResult<T>.onSuccess(action: (T) -> Unit): NetworkResult<T> {
    if (this is NetworkResult.Success) {
        action(data)
    }
    return this
}

/**
 * Extension pour exécuter du code uniquement en cas d'erreur
 */
inline fun <T> NetworkResult<T>.onError(action: (String, Int?) -> Unit): NetworkResult<T> {
    if (this is NetworkResult.Error) {
        action(message, code)
    }
    return this
}

/**
 * Extension pour exécuter du code uniquement en cas de chargement
 */
inline fun <T> NetworkResult<T>.onLoading(action: () -> Unit): NetworkResult<T> {
    if (this is NetworkResult.Loading) {
        action()
    }
    return this
}

/**
 * Extension pour exécuter du code uniquement en état initial/repos
 */
inline fun <T> NetworkResult<T>.onIdle(action: () -> Unit): NetworkResult<T> {
    if (this is NetworkResult.Idle) {
        action()
    }
    return this
}
