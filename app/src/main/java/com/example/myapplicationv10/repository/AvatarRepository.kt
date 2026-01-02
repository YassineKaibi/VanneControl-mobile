package com.example.myapplicationv10.repository

import android.content.Context
import android.net.Uri
import com.example.myapplicationv10.model.AvatarResponse
import com.example.myapplicationv10.network.ApiClient
import com.example.myapplicationv10.network.NetworkResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

/**
 * AvatarRepository - Repository pour gérer l'upload et la suppression d'avatars
 *
 * Gère:
 * - Upload d'image depuis Uri (gallery/camera)
 * - Suppression d'avatar
 * - Conversion Uri → File pour multipart upload
 */
class AvatarRepository(private val context: Context) {

    private val apiService = ApiClient.getApiService(context)

    /**
     * Upload un avatar depuis un Uri (gallery ou camera)
     *
     * @param imageUri Uri de l'image sélectionnée
     * @return NetworkResult avec AvatarResponse contenant l'URL
     */
    suspend fun uploadAvatar(imageUri: Uri): NetworkResult<AvatarResponse> {
        return withContext(Dispatchers.IO) {
            try {
                // Convertir Uri en File temporaire
                val file = uriToFile(imageUri)
                    ?: return@withContext NetworkResult.Error("Failed to read image file")

                // Déterminer le type MIME
                val mimeType = context.contentResolver.getType(imageUri) ?: "image/jpeg"

                // Créer le RequestBody et MultipartBody.Part
                val requestBody = file.asRequestBody(mimeType.toMediaTypeOrNull())
                val multipartBody = MultipartBody.Part.createFormData(
                    "avatar",
                    file.name,
                    requestBody
                )

                // Appeler l'API
                val result = ApiClient.safeApiCall {
                    apiService.uploadAvatar(multipartBody)
                }

                // Nettoyer le fichier temporaire
                file.delete()

                result

            } catch (e: Exception) {
                e.printStackTrace()
                NetworkResult.Error("Upload failed: ${e.message}")
            }
        }
    }

    /**
     * Supprimer l'avatar actuel
     *
     * @return NetworkResult avec message de confirmation
     */
    suspend fun deleteAvatar(): NetworkResult<AvatarResponse> {
        return withContext(Dispatchers.IO) {
            ApiClient.safeApiCall {
                apiService.deleteAvatar()
            }
        }
    }

    /**
     * Convertir un Uri en fichier temporaire
     *
     * @param uri Uri de l'image
     * @return File temporaire ou null si échec
     */
    private fun uriToFile(uri: Uri): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return null

            // Déterminer l'extension depuis le type MIME
            val mimeType = context.contentResolver.getType(uri)
            val extension = when (mimeType) {
                "image/png" -> "png"
                "image/webp" -> "webp"
                else -> "jpg"
            }

            // Créer un fichier temporaire
            val tempFile = File.createTempFile(
                "avatar_upload_",
                ".$extension",
                context.cacheDir
            )

            // Copier le contenu
            FileOutputStream(tempFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
            inputStream.close()

            tempFile

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}