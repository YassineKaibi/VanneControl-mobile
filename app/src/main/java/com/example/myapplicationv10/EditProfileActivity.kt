package com.example.myapplicationv10

import android.Manifest
import android.app.DatePickerDialog
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import coil.load
import coil.transform.CircleCropTransformation
import com.example.myapplicationv10.databinding.ActivityEditProfileBinding
import com.example.myapplicationv10.network.NetworkResult
import com.example.myapplicationv10.repository.AvatarRepository
import com.example.myapplicationv10.utils.Constants
import com.example.myapplicationv10.utils.ValveLimitManager
import com.example.myapplicationv10.viewmodel.ProfileViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * EditProfileActivity - Écran d'édition du profil avec upload d'avatar
 *
 * Fonctionnalités:
 * - Modification des informations personnelles
 * - Upload d'avatar depuis la galerie ou la caméra
 * - Suppression d'avatar
 * - Affichage d'avatar avec Coil
 */
class EditProfileActivity : BaseActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var viewModel: ProfileViewModel
    private lateinit var avatarRepository: AvatarRepository

    private val selectedDate = Calendar.getInstance()

    // Uri pour la photo prise par la caméra
    private var cameraImageUri: Uri? = null

    // Uri de l'avatar sélectionné (avant upload)
    private var selectedAvatarUri: Uri? = null

    // URL de l'avatar actuel (après upload)
    private var currentAvatarUrl: String? = null

    // ═══════════════════════════════════════════════════════════════
    // Activity Result Launchers
    // ═══════════════════════════════════════════════════════════════

    /**
     * Launcher pour sélectionner une image depuis la galerie
     */
    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { handleImageSelected(it) }
    }

    /**
     * Launcher pour prendre une photo avec la caméra
     */
    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success) {
            cameraImageUri?.let { handleImageSelected(it) }
        }
    }

    /**
     * Launcher pour demander la permission caméra
     */
    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            launchCamera()
        } else {
            Snackbar.make(
                binding.root,
                "Camera permission required to take photos",
                Snackbar.LENGTH_LONG
            ).show()
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // Lifecycle
    // ═══════════════════════════════════════════════════════════════

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[ProfileViewModel::class.java]
        avatarRepository = AvatarRepository(this)

        setupBackButton()
        setupDatePicker()
        setupPhotoButton()
        setupSaveButton()
        populateFieldsFromIntent()
        observeViewModel()
    }

    // ═══════════════════════════════════════════════════════════════
    // Setup Methods
    // ═══════════════════════════════════════════════════════════════

    private fun setupBackButton() {
        binding.backButton.setOnClickListener { finish() }
    }

    private fun setupDatePicker() {
        binding.dateOfBirthEdit.setOnClickListener { showDatePickerDialog() }
        binding.dateOfBirthEdit.isFocusable = false
    }

    private fun setupPhotoButton() {
        binding.changePhotoButton.setOnClickListener { showPhotoOptionsDialog() }
    }

    private fun setupSaveButton() {
        binding.saveButton.setOnClickListener { saveProfile() }
    }

    private fun populateFieldsFromIntent() {
        binding.firstNameEdit.setText(intent.getStringExtra("firstName") ?: "")
        binding.lastNameEdit.setText(intent.getStringExtra("lastName") ?: "")

        val dateOfBirth = intent.getStringExtra("dateOfBirth") ?: ""
        if (dateOfBirth.isNotEmpty() && dateOfBirth != "N/A") {
            binding.dateOfBirthEdit.setText(dateOfBirth)
            try {
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val date = sdf.parse(dateOfBirth)
                if (date != null) {
                    selectedDate.time = date
                }
            } catch (e: Exception) {
                // Keep current date
            }
        }

        binding.emailEdit.setText(intent.getStringExtra("email") ?: "")
        binding.phoneEdit.setText(intent.getStringExtra("phoneNumber") ?: "")
        binding.locationEdit.setText(intent.getStringExtra("location") ?: "")

        // Valve limit
        val valveLimitManager = ValveLimitManager.getInstance(this)
        var currentLimit = valveLimitManager.getValveLimit()

        if (currentLimit == 0 && intent.hasExtra("numberOfValves")) {
            currentLimit = intent.getIntExtra("numberOfValves", 0).coerceIn(0, 8)
            valveLimitManager.setValveLimit(currentLimit)
        }
        binding.numberOfValvesEdit.setText(currentLimit.toString())

        // Load current avatar URL (fix localhost URLs)
        currentAvatarUrl = Constants.fixAvatarUrl(intent.getStringExtra("avatarUrl"))
        loadAvatar(currentAvatarUrl)
    }

    // ═══════════════════════════════════════════════════════════════
    // Avatar Loading with Coil
    // ═══════════════════════════════════════════════════════════════

    /**
     * Charger et afficher l'avatar avec Coil (avec cache et logging)
     */
    private fun loadAvatar(url: String?) {
        binding.profilePicture.load(url) {
            crossfade(true)
            placeholder(R.drawable.ic_avatar_placeholder)
            error(R.drawable.ic_avatar_placeholder)
            transformations(CircleCropTransformation())
            // Enable caching
            memoryCacheKey(url)
            diskCacheKey(url)
            // Logging for debugging
            listener(
                onError = { _, result ->
                    android.util.Log.e("EditProfileActivity", "Avatar load failed: ${result.throwable.message}")
                },
                onSuccess = { _, _ ->
                    android.util.Log.d("EditProfileActivity", "Avatar loaded from: $url")
                }
            )
        }
    }

    /**
     * Afficher l'image sélectionnée (avant upload)
     */
    private fun loadAvatarFromUri(uri: Uri) {
        binding.profilePicture.load(uri) {
            crossfade(true)
            placeholder(R.drawable.ic_avatar_placeholder)
            error(R.drawable.ic_avatar_placeholder)
            transformations(CircleCropTransformation())
            // Don't cache local Uris (they're temporary)
            listener(
                onSuccess = { _, _ ->
                    android.util.Log.d("EditProfileActivity", "Preview loaded from Uri")
                }
            )
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // Photo Selection Dialog
    // ═══════════════════════════════════════════════════════════════

    private fun showPhotoOptionsDialog() {
        val options = mutableListOf("Choose from Gallery", "Take Photo")

        // Only show "Remove Photo" if there's an existing avatar
        if (currentAvatarUrl != null || selectedAvatarUri != null) {
            options.add("Remove Photo")
        }
        options.add("Cancel")

        AlertDialog.Builder(this)
            .setTitle("Change Profile Photo")
            .setItems(options.toTypedArray()) { dialog, which ->
                when (options[which]) {
                    "Choose from Gallery" -> openGallery()
                    "Take Photo" -> checkCameraPermissionAndLaunch()
                    "Remove Photo" -> confirmRemovePhoto()
                    "Cancel" -> dialog.dismiss()
                }
            }
            .show()
    }

    // ═══════════════════════════════════════════════════════════════
    // Gallery Selection
    // ═══════════════════════════════════════════════════════════════

    private fun openGallery() {
        galleryLauncher.launch("image/*")
    }

    // ═══════════════════════════════════════════════════════════════
    // Camera Capture
    // ═══════════════════════════════════════════════════════════════

    private fun checkCameraPermissionAndLaunch() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                launchCamera()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                Snackbar.make(
                    binding.root,
                    "Camera permission is needed to take photos",
                    Snackbar.LENGTH_LONG
                ).setAction("Grant") {
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                }.show()
            }
            else -> {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun launchCamera() {
        val photoFile = createImageFile()
        photoFile?.let { file ->
            cameraImageUri = FileProvider.getUriForFile(
                this,
                "${packageName}.fileprovider",
                file
            )
            cameraImageUri?.let { cameraLauncher.launch(it) }
        }
    }

    private fun createImageFile(): File? {
        return try {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            File.createTempFile("AVATAR_${timestamp}_", ".jpg", storageDir)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // Image Selection Handler
    // ═══════════════════════════════════════════════════════════════

    private fun handleImageSelected(uri: Uri) {
        selectedAvatarUri = uri
        loadAvatarFromUri(uri)

        // Upload immediately
        uploadAvatar(uri)
    }

    // ═══════════════════════════════════════════════════════════════
    // Avatar Upload
    // ═══════════════════════════════════════════════════════════════

    private fun uploadAvatar(uri: Uri) {
        showLoading("Uploading avatar...")
        android.util.Log.d("EditProfileActivity", "Starting avatar upload from Uri: $uri")

        lifecycleScope.launch {
            when (val result = avatarRepository.uploadAvatar(uri)) {
                is NetworkResult.Success -> {
                    hideLoading()
                    // Fix localhost URLs from backend
                    val rawUrl = result.data.avatarUrl
                    currentAvatarUrl = Constants.fixAvatarUrl(rawUrl)

                    android.util.Log.d("EditProfileActivity", "Upload success! Raw URL: $rawUrl, Fixed URL: $currentAvatarUrl")

                    Snackbar.make(
                        binding.root,
                        "Avatar uploaded successfully!",
                        Snackbar.LENGTH_SHORT
                    ).show()

                    // Reload avatar from server URL (already fixed above)
                    loadAvatar(currentAvatarUrl)
                }
                is NetworkResult.Error -> {
                    hideLoading()
                    selectedAvatarUri = null

                    android.util.Log.e("EditProfileActivity", "Upload failed: ${result.message}")

                    Snackbar.make(
                        binding.root,
                        "Upload failed: ${result.message}",
                        Snackbar.LENGTH_LONG
                    ).show()

                    // Revert to previous avatar
                    loadAvatar(currentAvatarUrl)
                }
                else -> {
                    android.util.Log.d("EditProfileActivity", "Upload result: $result")
                }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // Avatar Removal
    // ═══════════════════════════════════════════════════════════════

    private fun confirmRemovePhoto() {
        AlertDialog.Builder(this)
            .setTitle("Remove Photo")
            .setMessage("Are you sure you want to remove your profile photo?")
            .setPositiveButton("Remove") { _, _ -> removePhoto() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun removePhoto() {
        showLoading("Removing avatar...")

        lifecycleScope.launch {
            when (val result = avatarRepository.deleteAvatar()) {
                is NetworkResult.Success -> {
                    hideLoading()
                    currentAvatarUrl = null
                    selectedAvatarUri = null

                    // Reset to placeholder
                    binding.profilePicture.load(R.drawable.ic_avatar_placeholder) {
                        transformations(CircleCropTransformation())
                    }

                    Snackbar.make(
                        binding.root,
                        "Avatar removed successfully",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
                is NetworkResult.Error -> {
                    hideLoading()
                    Snackbar.make(
                        binding.root,
                        "Failed to remove avatar: ${result.message}",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
                else -> {}
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // Date Picker
    // ═══════════════════════════════════════════════════════════════

    private fun showDatePickerDialog() {
        val year = selectedDate.get(Calendar.YEAR)
        val month = selectedDate.get(Calendar.MONTH)
        val day = selectedDate.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                selectedDate.set(selectedYear, selectedMonth, selectedDay)
                updateDateField()
            },
            year,
            month,
            day
        )

        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
        datePickerDialog.show()
    }

    private fun updateDateField() {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        binding.dateOfBirthEdit.setText(sdf.format(selectedDate.time))
    }

    // ═══════════════════════════════════════════════════════════════
    // Profile Save
    // ═══════════════════════════════════════════════════════════════

    private fun saveProfile() {
        if (validateInputs()) {
            val valveLimit = binding.numberOfValvesEdit.text.toString().trim().toInt()
            ValveLimitManager.getInstance(this).setValveLimit(valveLimit)

            val firstName = binding.firstNameEdit.text.toString().trim()
            val lastName = binding.lastNameEdit.text.toString().trim()
            val dateOfBirth = binding.dateOfBirthEdit.text.toString().trim()
            val phone = binding.phoneEdit.text.toString().trim()
            val location = binding.locationEdit.text.toString().trim()

            val dateForApi = if (dateOfBirth.isNotEmpty()) {
                convertDateToApiFormat(dateOfBirth)
            } else {
                null
            }

            viewModel.updateUserProfile(
                firstName = firstName,
                lastName = lastName,
                phoneNumber = phone,
                dateOfBirth = dateForApi,
                location = location,
                avatarUrl = currentAvatarUrl  // Use the uploaded avatar URL
            )
        }
    }

    private fun convertDateToApiFormat(date: String): String {
        return try {
            val inputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val parsedDate = inputFormat.parse(date)
            outputFormat.format(parsedDate!!)
        } catch (e: Exception) {
            date
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true

        if (binding.firstNameEdit.text.toString().trim().isEmpty()) {
            binding.firstNameEdit.error = "First name is required"
            isValid = false
        }

        if (binding.lastNameEdit.text.toString().trim().isEmpty()) {
            binding.lastNameEdit.error = "Last name is required"
            isValid = false
        }

        val valveLimit = binding.numberOfValvesEdit.text.toString().trim()
        if (valveLimit.isEmpty()) {
            binding.numberOfValvesEdit.error = "Number of valves is required"
            isValid = false
        } else {
            val limit = valveLimit.toIntOrNull()
            if (limit == null || limit < 0 || limit > 8) {
                binding.numberOfValvesEdit.error = "Must be between 0 and 8"
                isValid = false
            }
        }

        return isValid
    }

    // ═══════════════════════════════════════════════════════════════
    // Loading State
    // ═══════════════════════════════════════════════════════════════

    private fun showLoading(message: String = "Saving...") {
        binding.saveButton.isEnabled = false
        binding.saveButton.text = message
    }

    private fun hideLoading() {
        binding.saveButton.isEnabled = true
        binding.saveButton.text = getString(R.string.save)
    }

    // ═══════════════════════════════════════════════════════════════
    // ViewModel Observer
    // ═══════════════════════════════════════════════════════════════

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.updateProfileState.collect { result ->
                when (result) {
                    is NetworkResult.Idle -> { /* Initial state */ }
                    is NetworkResult.Loading -> showLoading("Saving...")
                    is NetworkResult.Success -> {
                        hideLoading()
                        Snackbar.make(
                            binding.root,
                            "Profile updated successfully!",
                            Snackbar.LENGTH_SHORT
                        ).show()

                        binding.root.postDelayed({
                            setResult(RESULT_OK)
                            finish()
                        }, 1000)
                    }
                    is NetworkResult.Error -> {
                        hideLoading()
                        Snackbar.make(
                            binding.root,
                            "Failed to update profile: ${result.message}",
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }
}