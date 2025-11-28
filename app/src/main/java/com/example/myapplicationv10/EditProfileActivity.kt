package com.example.myapplicationv10

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.*

class EditProfileActivity : AppCompatActivity() {

    // Views
    private lateinit var profilePicture: ImageView
    private lateinit var changePhotoButton: CardView
    private lateinit var firstNameEdit: TextInputEditText
    private lateinit var lastNameEdit: TextInputEditText
    private lateinit var dateOfBirthEdit: TextInputEditText
    private lateinit var emailEdit: TextInputEditText
    private lateinit var phoneEdit: TextInputEditText
    private lateinit var locationEdit: TextInputEditText
    private lateinit var numberOfValvesEdit: TextInputEditText
    private lateinit var saveButton: TextView
    private lateinit var saveChangesButton: androidx.appcompat.widget.AppCompatButton
    private lateinit var backButton: ImageView

    // Date picker calendar
    private var selectedDate: Calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        initializeViews()
        setupBackButton()
        setupBackPressedHandler()
        setupSaveButtons()
        setupDatePicker()
        setupChangePhotoButton()
        loadCurrentData()
    }

    private fun initializeViews() {
        profilePicture = findViewById(R.id.profilePicture)
        changePhotoButton = findViewById(R.id.changePhotoButton)
        firstNameEdit = findViewById(R.id.firstNameEdit)
        lastNameEdit = findViewById(R.id.lastNameEdit)
        dateOfBirthEdit = findViewById(R.id.dateOfBirthEdit)
        emailEdit = findViewById(R.id.emailEdit)
        phoneEdit = findViewById(R.id.phoneEdit)
        locationEdit = findViewById(R.id.locationEdit)
        numberOfValvesEdit = findViewById(R.id.numberOfValvesEdit)
        saveButton = findViewById(R.id.saveButton)
        saveChangesButton = findViewById(R.id.saveChangesButton)
        backButton = findViewById(R.id.backButton)
    }

    private fun setupBackButton() {
        backButton.setOnClickListener {
            // Vérifier si des modifications ont été faites
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
        // Bouton Save en haut à droite
        saveButton.setOnClickListener {
            saveProfile()
        }

        // Bouton Save Changes en bas
        saveChangesButton.setOnClickListener {
            saveProfile()
        }
    }

    private fun setupDatePicker() {
        dateOfBirthEdit.setOnClickListener {
            showDatePickerDialog()
        }
    }

    private fun setupChangePhotoButton() {
        changePhotoButton.setOnClickListener {
            showPhotoOptionsDialog()
        }
    }

    private fun loadCurrentData() {
        // Récupérer les données depuis l'Intent
        firstNameEdit.setText(intent.getStringExtra("firstName") ?: "Yassine")
        lastNameEdit.setText(intent.getStringExtra("lastName") ?: "Channa")
        dateOfBirthEdit.setText(intent.getStringExtra("dateOfBirth") ?: "15/03/1995")
        emailEdit.setText(intent.getStringExtra("email") ?: "admin@vannecontrol.com")
        phoneEdit.setText(intent.getStringExtra("phoneNumber") ?: "+216 XX XXX XXX")
        locationEdit.setText(intent.getStringExtra("location") ?: "Houmt Souk, Medenine, Tunisia")
        numberOfValvesEdit.setText(intent.getIntExtra("numberOfValves", 8).toString())
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

        // Limiter la date à aujourd'hui (pas de dates futures)
        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()

        datePickerDialog.show()
    }

    private fun updateDateField() {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        dateOfBirthEdit.setText(sdf.format(selectedDate.time))
    }

    private fun showPhotoOptionsDialog() {
        val options = arrayOf(
            "Take Photo",
            "Choose from Gallery",
            "Remove Photo",
            "Cancel"
        )

        AlertDialog.Builder(this)
            .setTitle("Change Profile Photo")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> takePhoto()
                    1 -> chooseFromGallery()
                    2 -> removePhoto()
                    3 -> dialog.dismiss()
                }
            }
            .show()
    }

    private fun takePhoto() {
        // TODO: Implémenter la prise de photo avec la caméra
        Snackbar.make(
            findViewById(android.R.id.content),
            "Camera feature - Coming soon",
            Snackbar.LENGTH_SHORT
        ).show()
    }

    private fun chooseFromGallery() {
        // TODO: Implémenter la sélection depuis la galerie
        Snackbar.make(
            findViewById(android.R.id.content),
            "Gallery feature - Coming soon",
            Snackbar.LENGTH_SHORT
        ).show()
    }

    private fun removePhoto() {
        AlertDialog.Builder(this)
            .setTitle("Remove Photo")
            .setMessage("Are you sure you want to remove your profile photo?")
            .setPositiveButton("Remove") { _, _ ->
                // TODO: Supprimer la photo de profil
                profilePicture.setImageResource(R.drawable.ic_launcher_foreground)
                Snackbar.make(
                    findViewById(android.R.id.content),
                    "Profile photo removed",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun saveProfile() {
        if (validateInputs()) {
            // Récupérer les valeurs
            val firstName = firstNameEdit.text.toString().trim()
            val lastName = lastNameEdit.text.toString().trim()
            val dateOfBirth = dateOfBirthEdit.text.toString().trim()
            val email = emailEdit.text.toString().trim()
            val phone = phoneEdit.text.toString().trim()
            val location = locationEdit.text.toString().trim()
            val numberOfValves = numberOfValvesEdit.text.toString().trim().toIntOrNull() ?: 8

            // TODO: Sauvegarder dans la base de données
            // TODO: Envoyer au backend via API

            // Afficher un message de succès
            Snackbar.make(
                findViewById(android.R.id.content),
                "Profile updated successfully!",
                Snackbar.LENGTH_LONG
            ).show()

            // Retourner à ProfileActivity avec les nouvelles données
            val resultIntent = Intent()
            resultIntent.putExtra("firstName", firstName)
            resultIntent.putExtra("lastName", lastName)
            resultIntent.putExtra("dateOfBirth", dateOfBirth)
            resultIntent.putExtra("email", email)
            resultIntent.putExtra("phone", phone)
            resultIntent.putExtra("location", location)
            resultIntent.putExtra("numberOfValves", numberOfValves)
            setResult(RESULT_OK, resultIntent)

            // Attendre un peu avant de fermer pour montrer le message
            findViewById<android.view.View>(android.R.id.content).postDelayed({
                finish()
            }, 1500)
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true

        // Validation First Name
        if (firstNameEdit.text.toString().trim().isEmpty()) {
            firstNameEdit.error = "First name is required"
            isValid = false
        }

        // Validation Last Name
        if (lastNameEdit.text.toString().trim().isEmpty()) {
            lastNameEdit.error = "Last name is required"
            isValid = false
        }

        // Validation Date of Birth
        if (dateOfBirthEdit.text.toString().trim().isEmpty()) {
            dateOfBirthEdit.error = "Date of birth is required"
            isValid = false
        }

        // Validation Email
        val email = emailEdit.text.toString().trim()
        if (email.isEmpty()) {
            emailEdit.error = "Email is required"
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEdit.error = "Invalid email format"
            isValid = false
        }

        // Validation Phone
        val phone = phoneEdit.text.toString().trim()
        if (phone.isEmpty()) {
            phoneEdit.error = "Phone number is required"
            isValid = false
        } else if (phone.length < 8) {
            phoneEdit.error = "Phone number is too short"
            isValid = false
        }

        // Validation Location
        if (locationEdit.text.toString().trim().isEmpty()) {
            locationEdit.error = "Location is required"
            isValid = false
        }

        // Validation Number of Valves
        val valvesText = numberOfValvesEdit.text.toString().trim()
        if (valvesText.isEmpty()) {
            numberOfValvesEdit.error = "Number of valves is required"
            isValid = false
        } else {
            val valves = valvesText.toIntOrNull()
            if (valves == null || valves < 1) {
                numberOfValvesEdit.error = "Must be at least 1"
                isValid = false
            } else if (valves > 50) {
                numberOfValvesEdit.error = "Maximum 50 valves"
                isValid = false
            }
        }

        if (!isValid) {
            Snackbar.make(
                findViewById(android.R.id.content),
                "Please fix the errors before saving",
                Snackbar.LENGTH_LONG
            ).show()
        }

        return isValid
    }

    private fun showDiscardChangesDialog() {
        AlertDialog.Builder(this)
            .setTitle("Discard Changes?")
            .setMessage("You have unsaved changes. Are you sure you want to go back?")
            .setPositiveButton("Discard") { _, _ ->
                finish()
            }
            .setNegativeButton("Keep Editing", null)
            .show()
    }
}