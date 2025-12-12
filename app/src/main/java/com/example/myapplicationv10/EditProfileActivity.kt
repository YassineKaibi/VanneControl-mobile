package com.example.myapplicationv10

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.myapplicationv10.databinding.ActivityEditProfileBinding
import com.example.myapplicationv10.network.NetworkResult
import com.example.myapplicationv10.viewmodel.ProfileViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class EditProfileActivity : AppCompatActivity() {

    // ViewModel
    private val viewModel: ProfileViewModel by viewModels()

    private lateinit var binding: ActivityEditProfileBinding
    private var selectedDate: Calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupBackButton()
        setupBackPressedHandler()
        setupSaveButtons()
        setupDatePicker()
        setupChangePhotoButton()
        observeViewModel()
        loadCurrentData()
    }

    private fun setupBackButton() {
        binding.backButton.setOnClickListener {
            showDiscardChangesDialog()
        }
    }

    private fun setupBackPressedHandler() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showDiscardChangesDialog()
            }
        })
    }

    private fun setupSaveButtons() {
        binding.saveButton.setOnClickListener { saveProfile() }
        binding.saveChangesButton.setOnClickListener { saveProfile() }
    }

    private fun setupDatePicker() {
        binding.dateOfBirthEdit.setOnClickListener { showDatePickerDialog() }
    }

    private fun setupChangePhotoButton() {
        binding.changePhotoButton.setOnClickListener { showPhotoOptionsDialog() }
    }

    private fun loadCurrentData() {
        binding.firstNameEdit.setText(intent.getStringExtra("firstName") ?: "")
        binding.lastNameEdit.setText(intent.getStringExtra("lastName") ?: "")

        // Handle date of birth - don't show "N/A"
        val dateOfBirth = intent.getStringExtra("dateOfBirth") ?: ""
        if (dateOfBirth.isNotEmpty() && dateOfBirth != "N/A") {
            binding.dateOfBirthEdit.setText(dateOfBirth)
            // Parse existing date to update selectedDate calendar
            try {
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val date = sdf.parse(dateOfBirth)
                if (date != null) {
                    selectedDate.time = date
                }
            } catch (e: Exception) {
                // If parsing fails, keep current date
            }
        }

        binding.emailEdit.setText(intent.getStringExtra("email") ?: "")
        binding.phoneEdit.setText(intent.getStringExtra("phoneNumber") ?: "")
        binding.locationEdit.setText(intent.getStringExtra("location") ?: "")

        // Load valve limit from SharedPreferences
        val valveLimitManager = ValveLimitManager.getInstance(this)
        var currentLimit = valveLimitManager.getValveLimit()

        // Migration: If it's default (0) and we have intent data, use that as migration
        if (currentLimit == 0 && intent.hasExtra("numberOfValves")) {
            currentLimit = intent.getIntExtra("numberOfValves", 0).coerceIn(0, 8)
            valveLimitManager.setValveLimit(currentLimit)
        }

        binding.numberOfValvesEdit.setText(currentLimit.toString())
    }

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

    private fun showPhotoOptionsDialog() {
        val options = arrayOf("Take Photo", "Choose from Gallery", "Remove Photo", "Cancel")
        AlertDialog.Builder(this)
            .setTitle("Change Profile Photo")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> takePhoto()
                    1 -> chooseFromGallery()
                    2 -> removePhoto()
                    3 -> dialog.dismiss()
                }
            }.show()
    }

    private fun takePhoto() {
        Snackbar.make(binding.root, "Camera feature - Coming soon", Snackbar.LENGTH_SHORT).show()
    }

    private fun chooseFromGallery() {
        Snackbar.make(binding.root, "Gallery feature - Coming soon", Snackbar.LENGTH_SHORT).show()
    }

    private fun removePhoto() {
        AlertDialog.Builder(this)
            .setTitle("Remove Photo")
            .setMessage("Are you sure you want to remove your profile photo?")
            .setPositiveButton("Remove") { _, _ ->
                binding.profilePicture.setImageResource(R.drawable.ic_launcher_foreground)
                Snackbar.make(binding.root, "Profile photo removed", Snackbar.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun observeViewModel() {
        // Observer l'état de mise à jour du profil
        lifecycleScope.launch {
            viewModel.updateProfileState.collect { result ->
                when (result) {
                    is NetworkResult.Idle -> {
                        // État initial - Ne rien faire
                    }

                    is NetworkResult.Loading -> {
                        showLoading()
                    }

                    is NetworkResult.Success -> {
                        hideLoading()
                        Snackbar.make(
                            binding.root,
                            "Profile updated successfully!",
                            Snackbar.LENGTH_LONG
                        ).show()

                        // Attendre un peu avant de fermer pour montrer le message
                        binding.root.postDelayed({
                            setResult(RESULT_OK)
                            finish()
                        }, 1500)
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

    private fun showLoading() {
        binding.saveButton.isEnabled = false
        binding.saveChangesButton.isEnabled = false
        binding.saveChangesButton.text = "Saving..."
    }

    private fun hideLoading() {
        binding.saveButton.isEnabled = true
        binding.saveChangesButton.isEnabled = true
        binding.saveChangesButton.text = "Save Changes"
    }

    private fun saveProfile() {
        if (validateInputs()) {
            // Save valve limit locally first
            val valveLimit = binding.numberOfValvesEdit.text.toString().trim().toInt()
            ValveLimitManager.getInstance(this).setValveLimit(valveLimit)

            // Récupérer les valeurs
            val firstName = binding.firstNameEdit.text.toString().trim()
            val lastName = binding.lastNameEdit.text.toString().trim()
            val dateOfBirth = binding.dateOfBirthEdit.text.toString().trim()
            val phone = binding.phoneEdit.text.toString().trim()
            val location = binding.locationEdit.text.toString().trim()

            // Convertir la date du format dd/MM/yyyy au format yyyy-MM-dd pour l'API
            // Si vide, envoyer null au lieu d'une chaîne vide
            val dateForApi = if (dateOfBirth.isNotEmpty()) {
                convertDateToApiFormat(dateOfBirth)
            } else {
                null
            }

            // Appeler le ViewModel pour mettre à jour le profil
            // Note: numberOfValves is NOT sent to backend (saved locally only)
            viewModel.updateUserProfile(
                firstName = firstName,
                lastName = lastName,
                phoneNumber = phone,
                dateOfBirth = dateForApi,
                location = location,
                avatarUrl = null  // TODO: Implement avatar upload
            )
        }
    }

    /**
     * Convertir la date du format dd/MM/yyyy au format yyyy-MM-dd
     */
    private fun convertDateToApiFormat(date: String): String {
        return try {
            val inputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val parsedDate = inputFormat.parse(date)
            outputFormat.format(parsedDate!!)
        } catch (e: Exception) {
            date  // Retourner la date originale en cas d'erreur
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
        // Date of birth is now optional - no validation needed
        val email = binding.emailEdit.text.toString().trim()
        if (email.isEmpty()) {
            binding.emailEdit.error = "Email is required"
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailEdit.error = "Invalid email format"
            isValid = false
        }
        val phone = binding.phoneEdit.text.toString().trim()
        if (phone.isEmpty()) {
            binding.phoneEdit.error = "Phone number is required"
            isValid = false
        } else if (phone.length < 8) {
            binding.phoneEdit.error = "Phone number is too short"
            isValid = false
        }
        if (binding.locationEdit.text.toString().trim().isEmpty()) {
            binding.locationEdit.error = "Location is required"
            isValid = false
        }
        val valvesText = binding.numberOfValvesEdit.text.toString().trim()
        if (valvesText.isEmpty()) {
            binding.numberOfValvesEdit.error = "Number of valves is required"
            isValid = false
        } else {
            val valves = valvesText.toIntOrNull()
            if (valves == null || valves < 0) {
                binding.numberOfValvesEdit.error = "Must be at least 0"
                isValid = false
            } else if (valves > 8) {
                binding.numberOfValvesEdit.error = "Maximum 8 valves"
                isValid = false
            }
        }

        if (!isValid) Snackbar.make(binding.root, "Please fix the errors before saving", Snackbar.LENGTH_LONG).show()

        return isValid
    }

    private fun showDiscardChangesDialog() {
        AlertDialog.Builder(this)
            .setTitle("Discard Changes?")
            .setMessage("You have unsaved changes. Are you sure you want to go back?")
            .setPositiveButton("Discard") { _, _ -> finish() }
            .setNegativeButton("Keep Editing", null)
            .show()
    }

}
