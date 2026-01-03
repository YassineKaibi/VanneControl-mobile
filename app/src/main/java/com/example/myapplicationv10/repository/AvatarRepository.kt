package com.example.myapplicationv10.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
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
import kotlin.math.min

/**
 * AvatarRepository - Repository pour gérer l'upload et la suppression d'avatars
 *
 * Gère:
 * - Upload d'image depuis Uri (gallery/camera)
 * - Compression intelligente des images (réduit la taille sans perte de qualité visible)
 * - Correction de l'orientation EXIF
 * - Suppression d'avatar
 * - Conversion Uri → File pour multipart upload
 */
class AvatarRepository(private val context: Context) {

    private val apiService = ApiClient.getApiService(context)

    companion object {
        // Configuration de compression
        private const val MAX_IMAGE_DIMENSION = 1024  // Max width/height in pixels
        private const val JPEG_QUALITY = 85  // 0-100, 85 is good balance between quality and size
        private const val MAX_FILE_SIZE_MB = 5
    }

    /**
     * Upload un avatar depuis un Uri (gallery ou camera)
     *
     * Process:
     * 1. Decode image from Uri
     * 2. Fix orientation based on EXIF data
     * 3. Resize to max dimensions (maintains aspect ratio)
     * 4. Compress to JPEG with quality setting
     * 5. Upload to server
     *
     * @param imageUri Uri de l'image sélectionnée
     * @return NetworkResult avec AvatarResponse contenant l'URL
     */
    suspend fun uploadAvatar(imageUri: Uri): NetworkResult<AvatarResponse> {
        return withContext(Dispatchers.IO) {
            var tempFile: File? = null
            try {
                // Compress and prepare image
                tempFile = compressAndPrepareImage(imageUri)
                    ?: return@withContext NetworkResult.Error("Failed to process image")

                // Check file size after compression
                val fileSizeMB = tempFile.length() / (1024.0 * 1024.0)
                if (fileSizeMB > MAX_FILE_SIZE_MB) {
                    return@withContext NetworkResult.Error("Image too large after compression (${String.format("%.1f", fileSizeMB)}MB). Max: ${MAX_FILE_SIZE_MB}MB")
                }

                // Créer le RequestBody et MultipartBody.Part
                val requestBody = tempFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
                val multipartBody = MultipartBody.Part.createFormData(
                    "avatar",
                    "avatar.jpg",
                    requestBody
                )

                // Appeler l'API
                val result = ApiClient.safeApiCall {
                    apiService.uploadAvatar(multipartBody)
                }

                result

            } catch (e: Exception) {
                e.printStackTrace()
                NetworkResult.Error("Upload failed: ${e.message}")
            } finally {
                // Nettoyer le fichier temporaire
                tempFile?.delete()
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
     * Compress and prepare image for upload
     *
     * Steps:
     * 1. Decode bitmap from Uri
     * 2. Get EXIF orientation and fix rotation
     * 3. Resize to max dimensions
     * 4. Compress to JPEG
     *
     * @param uri Uri de l'image source
     * @return Compressed JPEG file ready for upload, or null on failure
     */
    private fun compressAndPrepareImage(uri: Uri): File? {
        return try {
            // Open input stream to read image
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return null

            // Decode bitmap
            var bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()

            if (bitmap == null) return null

            // Fix orientation based on EXIF data
            bitmap = fixImageOrientation(uri, bitmap)

            // Resize if needed (maintains aspect ratio)
            bitmap = resizeBitmap(bitmap, MAX_IMAGE_DIMENSION)

            // Create temp file for compressed image
            val tempFile = File.createTempFile(
                "avatar_compressed_",
                ".jpg",
                context.cacheDir
            )

            // Compress and save as JPEG
            FileOutputStream(tempFile).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, outputStream)
            }

            // Clean up bitmap
            bitmap.recycle()

            tempFile

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Fix image orientation based on EXIF data
     *
     * Photos from camera often have rotation in EXIF metadata.
     * This applies that rotation to the bitmap.
     */
    private fun fixImageOrientation(uri: Uri, bitmap: Bitmap): Bitmap {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return bitmap

            val exif = ExifInterface(inputStream)
            inputStream.close()

            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )

            val matrix = Matrix()
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
                ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
                else -> return bitmap
            }

            val rotatedBitmap = Bitmap.createBitmap(
                bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
            )

            if (rotatedBitmap != bitmap) {
                bitmap.recycle()
            }

            rotatedBitmap

        } catch (e: Exception) {
            e.printStackTrace()
            bitmap
        }
    }

    /**
     * Resize bitmap to fit within max dimensions while maintaining aspect ratio
     *
     * @param bitmap Original bitmap
     * @param maxDimension Maximum width or height
     * @return Resized bitmap (or original if already small enough)
     */
    private fun resizeBitmap(bitmap: Bitmap, maxDimension: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        // Check if resize needed
        if (width <= maxDimension && height <= maxDimension) {
            return bitmap
        }

        // Calculate scale factor
        val scale = min(
            maxDimension.toFloat() / width,
            maxDimension.toFloat() / height
        )

        val newWidth = (width * scale).toInt()
        val newHeight = (height * scale).toInt()

        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)

        if (resizedBitmap != bitmap) {
            bitmap.recycle()
        }

        return resizedBitmap
    }
}