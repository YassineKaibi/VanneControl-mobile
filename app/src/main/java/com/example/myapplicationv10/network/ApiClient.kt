package com.example.myapplicationv10.network

import android.content.Context
import com.example.myapplicationv10.model.ErrorResponse
import com.example.myapplicationv10.network.interceptors.AuthInterceptor
import com.example.myapplicationv10.utils.Constants
import com.example.myapplicationv10.utils.TokenManager
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * ApiClient - Configuration centralisée de Retrofit pour les appels API
 *
 * Singleton qui fournit une instance configurée de l'API service
 */
object ApiClient {

    @Volatile
    private var apiService: ApiService? = null

    @Volatile
    private var tokenManager: TokenManager? = null

    private val gson: Gson = GsonBuilder()
        .setLenient()
        .create()

    /**
     * Initialiser le client API avec le contexte
     * À appeler une fois au démarrage de l'application
     */
    fun initialize(context: Context) {
        if (tokenManager == null) {
            tokenManager = TokenManager.getInstance(context)
        }
    }

    /**
     * Obtenir l'instance du service API
     */
    fun getApiService(context: Context): ApiService {
        // Initialiser le TokenManager si nécessaire
        if (tokenManager == null) {
            initialize(context)
        }

        // Retourner l'instance existante ou en créer une nouvelle
        return apiService ?: synchronized(this) {
            apiService ?: buildApiService().also { apiService = it }
        }
    }

    /**
     * Construire l'instance Retrofit avec tous les intercepteurs
     */
    private fun buildApiService(): ApiService {
        val okHttpClient = buildOkHttpClient()

        val retrofit = Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        return retrofit.create(ApiService::class.java)
    }

    /**
     * Construire le client OkHttp avec les intercepteurs
     */
    private fun buildOkHttpClient(): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .connectTimeout(Constants.CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(Constants.READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(Constants.WRITE_TIMEOUT, TimeUnit.SECONDS)

        // Ajouter l'intercepteur d'authentification
        tokenManager?.let { tm ->
            builder.addInterceptor(AuthInterceptor(tm))
        }

        // Ajouter l'intercepteur de logging en mode debug
        if (Constants.ENABLE_NETWORK_LOGS) {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            builder.addInterceptor(loggingInterceptor)
        }

        return builder.build()
    }

    /**
     * Fonction utilitaire pour traiter les réponses API et les convertir en NetworkResult
     */
    suspend fun <T> safeApiCall(
        apiCall: suspend () -> Response<T>
    ): NetworkResult<T> {
        return try {
            val response = apiCall()

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    NetworkResult.Success(body)
                } else {
                    NetworkResult.Error("Réponse vide du serveur", response.code())
                }
            } else {
                // Essayer de parser le message d'erreur
                val errorMessage = try {
                    val errorBody = response.errorBody()?.string()
                    if (errorBody != null) {
                        val errorResponse = gson.fromJson(errorBody, ErrorResponse::class.java)
                        errorResponse.message ?: errorResponse.error ?: "Erreur inconnue"
                    } else {
                        "Erreur ${response.code()}"
                    }
                } catch (e: Exception) {
                    "Erreur ${response.code()}: ${response.message()}"
                }

                NetworkResult.Error(errorMessage, response.code())
            }
        } catch (e: Exception) {
            e.printStackTrace()

            // Gérer les différents types d'erreurs
            val errorMessage = when {
                e is java.net.UnknownHostException -> Constants.ERROR_SERVER_UNREACHABLE
                e is java.net.SocketTimeoutException -> "Délai d'attente dépassé"
                e is java.io.IOException -> Constants.ERROR_NO_INTERNET
                else -> "${Constants.ERROR_UNKNOWN}: ${e.localizedMessage}"
            }

            NetworkResult.Error(errorMessage)
        }
    }

    /**
     * Réinitialiser le client API (utile pour le logout)
     */
    fun reset() {
        apiService = null
    }
}
