package com.example.myapplicationv10.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * TokenManager - Gestion sécurisée des tokens JWT
 *
 * Utilise EncryptedSharedPreferences pour stocker les tokens de manière sécurisée
 * dans le Android KeyStore
 */
class TokenManager(context: Context) {

    private val sharedPreferences: SharedPreferences

    init {
        // Créer ou récupérer la MasterKey pour le chiffrement
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        // Créer les SharedPreferences chiffrées
        sharedPreferences = try {
            EncryptedSharedPreferences.create(
                context,
                Constants.PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            // Fallback vers SharedPreferences standard en cas d'erreur
            // (ne devrait pas arriver sur les appareils modernes)
            e.printStackTrace()
            context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
        }
    }

    /**
     * Sauvegarder le token JWT
     */
    fun saveToken(token: String) {
        sharedPreferences.edit()
            .putString(Constants.PREFS_AUTH_TOKEN, token)
            .apply()
    }

    /**
     * Récupérer le token JWT
     */
    fun getToken(): String? {
        return sharedPreferences.getString(Constants.PREFS_AUTH_TOKEN, null)
    }

    /**
     * Sauvegarder les informations utilisateur
     */
    fun saveUserInfo(userId: String, email: String) {
        sharedPreferences.edit()
            .putString(Constants.PREFS_USER_ID, userId)
            .putString(Constants.PREFS_USER_EMAIL, email)
            .apply()
    }

    /**
     * Récupérer l'ID utilisateur
     */
    fun getUserId(): String? {
        return sharedPreferences.getString(Constants.PREFS_USER_ID, null)
    }

    /**
     * Récupérer l'email utilisateur
     */
    fun getUserEmail(): String? {
        return sharedPreferences.getString(Constants.PREFS_USER_EMAIL, null)
    }

    /**
     * Vérifier si l'utilisateur est connecté (a un token)
     */
    fun isLoggedIn(): Boolean {
        return getToken() != null
    }

    /**
     * Supprimer toutes les données d'authentification (logout)
     */
    fun clearAuth() {
        sharedPreferences.edit()
            .remove(Constants.PREFS_AUTH_TOKEN)
            .remove(Constants.PREFS_USER_ID)
            .remove(Constants.PREFS_USER_EMAIL)
            .apply()
    }

    /**
     * Déconnecter l'utilisateur
     * Alias pour clearAuth() pour plus de clarté
     */
    fun logout() {
        clearAuth()
    }

    /**
     * Obtenir le header Authorization complet avec le Bearer prefix
     */
    fun getAuthorizationHeader(): String? {
        val token = getToken()
        return if (token != null) {
            "${Constants.BEARER_PREFIX}$token"
        } else {
            null
        }
    }

    companion object {
        @Volatile
        private var instance: TokenManager? = null

        /**
         * Obtenir l'instance singleton de TokenManager
         */
        fun getInstance(context: Context): TokenManager {
            return instance ?: synchronized(this) {
                instance ?: TokenManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
