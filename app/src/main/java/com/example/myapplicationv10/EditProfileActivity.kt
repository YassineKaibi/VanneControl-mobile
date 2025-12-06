package com.example.myapplicationv10

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplicationv10.databinding.ActivityEditProfileBinding
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.util.*

class EditProfileActivity : AppCompatActivity() {


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
        binding.firstNameEdit.setText(intent.getStringExtra("firstName") ?: "Yassine")
        binding.lastNameEdit.setText(intent.getStringExtra("lastName") ?: "Channa")
        binding.dateOfBirthEdit.setText(intent.getStringExtra("dateOfBirth") ?: "15/03/1995")
        binding.emailEdit.setText(intent.getStringExtra("email") ?: "admin@vannecontrol.com")
        binding.phoneEdit.setText(intent.getStringExtra("phoneNumber") ?: "+216 XX XXX XXX")
        binding.locationEdit.setText(intent.getStringExtra("location") ?: "Houmt Souk, Medenine, Tunisia")
        binding.numberOfValvesEdit.setText(intent.getIntExtra("numberOfValves", 8).toString())
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

    private fun saveProfile() {
        if (validateInputs()) {
            val firstName = binding.firstNameEdit.text.toString().trim()
            val lastName = binding.lastNameEdit.text.toString().trim()
            val dateOfBirth = binding.dateOfBirthEdit.text.toString().trim()
            val email = binding.emailEdit.text.toString().trim()
            val phone = binding.phoneEdit.text.toString().trim()
            val location = binding.locationEdit.text.toString().trim()
            val numberOfValves = binding.numberOfValvesEdit.text.toString().trim().toIntOrNull() ?: 8

            Snackbar.make(binding.root, "Profile updated successfully!", Snackbar.LENGTH_LONG).show()

            val resultIntent = Intent()
            resultIntent.putExtra("firstName", firstName)
            resultIntent.putExtra("lastName", lastName)
            resultIntent.putExtra("dateOfBirth", dateOfBirth)
            resultIntent.putExtra("email", email)
            resultIntent.putExtra("phone", phone)
            resultIntent.putExtra("location", location)
            resultIntent.putExtra("numberOfValves", numberOfValves)
            setResult(RESULT_OK, resultIntent)

            binding.root.postDelayed({ finish() }, 1500)
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
        if (binding.dateOfBirthEdit.text.toString().trim().isEmpty()) {
            binding.dateOfBirthEdit.error = "Date of birth is required"
            isValid = false
        }
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
            if (valves == null || valves < 1) {
                binding.numberOfValvesEdit.error = "Must be at least 1"
                isValid = false
            } else if (valves > 50) {
                binding.numberOfValvesEdit.error = "Maximum 50 valves"
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
