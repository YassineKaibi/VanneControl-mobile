package com.example.myapplicationv10

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.myapplicationv10.databinding.ActivityProfileBinding
import com.example.myapplicationv10.model.User
import com.example.myapplicationv10.network.ApiClient
import com.example.myapplicationv10.network.NetworkResult
import com.example.myapplicationv10.utils.TokenManager
import com.example.myapplicationv10.viewmodel.ProfileViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private val viewModel: ProfileViewModel by viewModels()

    // Current user data from API
    private var currentUser: User? = null

    // Activity Result Launcher for edit profile
    private val editProfileLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                // Reload profile from API after edit
                viewModel.loadUserProfile()

                Snackbar.make(
                    binding.root,
                    "Profile updated successfully!",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }

    companion object {
        private const val EDIT_PROFILE_REQUEST = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialisation du ViewBinding
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configuration des événements
        setupTopBarEvents()
        setupPhotoChangeButton()
        setupTabs()
        setupLogoutButton()

        // Afficher l'onglet par défaut
        showPersonalInfo()

        // Observer les changements d'état du profil
        observeProfileState()

        // Charger les données du profil depuis l'API
        viewModel.loadUserProfile()
    }

    // -----------------------------------------------------
    //        OBSERVATION DES DONNÉES
    // -----------------------------------------------------

    private fun observeProfileState() {
        lifecycleScope.launch {
            viewModel.profileState.collect { result ->
                when (result) {
                    is NetworkResult.Loading -> {
                        showLoading(true)
                    }
                    is NetworkResult.Success -> {
                        showLoading(false)
                        currentUser = result.data
                        loadProfileData()
                    }
                    is NetworkResult.Error -> {
                        showLoading(false)
                        showError(result.message)
                    }
                    is NetworkResult.Idle -> {
                        showLoading(false)
                    }
                }
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        // TODO: Add a progress bar if needed
        binding.root.alpha = if (isLoading) 0.5f else 1.0f
    }

    private fun showError(message: String) {
        Snackbar.make(
            binding.root,
            "Error: $message",
            Snackbar.LENGTH_LONG
        ).show()
    }

    // -----------------------------------------------------
    //        CONFIGURATION DES ÉVÉNEMENTS
    // -----------------------------------------------------

    private fun setupTopBarEvents() {
        // Bouton retour
        binding.backButton.setOnClickListener {
            finish()
        }

        // Bouton Edit
        binding.editButton.setOnClickListener {
            openEditProfile()
        }

        // Bouton Close (optionnel)
        binding.closeButton.setOnClickListener {
            finish()
        }
    }

    private fun setupPhotoChangeButton() {
        binding.changePhotoButton.setOnClickListener {
            showPhotoOptionsDialog()
        }
    }

    private fun setupTabs() {
        binding.personalInfoTab.setOnClickListener {
            showPersonalInfo()
        }

        binding.teamsTab.setOnClickListener {
            showSystemInfo()
        }
    }

    private fun setupLogoutButton() {
        binding.logoutButton.setOnClickListener {
            showLogoutConfirmationDialog()
        }
    }

    // -----------------------------------------------------
    //        CHARGEMENT DES DONNÉES
    // -----------------------------------------------------

    private fun loadProfileData() {
        val user = currentUser ?: return

        // Mettre à jour l'en-tête
        val fullName = listOfNotNull(user.firstName, user.lastName)
            .joinToString(" ")
            .ifEmpty { "User" }
        binding.userFullName.text = fullName
        binding.userEmailHeader.text = user.email

        // Mettre à jour Personal Info avec "Not set" par défaut
        binding.firstNameValue.text = user.firstName ?: "Not set"
        binding.lastNameValue.text = user.lastName ?: "Not set"
        binding.dateOfBirthValue.text = user.dateOfBirth ?: "Not set"
        binding.emailValue.text = user.email
        binding.phoneNumberValue.text = user.phoneNumber ?: "Not set"
        binding.locationValue.text = user.location ?: "Not set"

        // Mettre à jour System Info
        // Note: numberOfValves is not in the User model - this should come from device count
        binding.numberOfValvesValue.text = "Managed through devices"
    }

    // -----------------------------------------------------
    //        NAVIGATION VERS EDIT PROFILE
    // -----------------------------------------------------

    private fun openEditProfile() {
        val user = currentUser ?: return

        val intent = Intent(this, EditProfileActivity::class.java)

        // Passer les données actuelles de l'utilisateur
        intent.putExtra("firstName", user.firstName)
        intent.putExtra("lastName", user.lastName)
        intent.putExtra("dateOfBirth", user.dateOfBirth)
        intent.putExtra("email", user.email)
        intent.putExtra("phoneNumber", user.phoneNumber)
        intent.putExtra("location", user.location)

        editProfileLauncher.launch(intent)
    }

    // -----------------------------------------------------
    //        DIALOG POUR CHANGER LA PHOTO
    // -----------------------------------------------------

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
        Toast.makeText(this, "Camera feature - Coming soon", Toast.LENGTH_SHORT).show()
    }

    private fun chooseFromGallery() {
        // TODO: Implémenter la sélection depuis la galerie
        Toast.makeText(this, "Gallery feature - Coming soon", Toast.LENGTH_SHORT).show()
    }

    private fun removePhoto() {
        AlertDialog.Builder(this)
            .setTitle("Remove Photo")
            .setMessage("Are you sure you want to remove your profile photo?")
            .setPositiveButton("Remove") { _, _ ->
                // TODO: Supprimer la photo de profil
                binding.profilePicture.setImageResource(R.drawable.ic_launcher_foreground)
                Toast.makeText(this, "Profile photo removed", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // -----------------------------------------------------
    //        LOGOUT CONFIRMATION DIALOG
    // -----------------------------------------------------

    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { _, _ ->
                performLogout()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun performLogout() {
        // Clear authentication data
        val tokenManager = TokenManager.getInstance(this)
        tokenManager.logout()

        // Reset API client
        ApiClient.reset()

        // Navigate to login screen and clear back stack
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    // -----------------------------------------------------
    //        AFFICHAGE DES SECTIONS (TABS)
    // -----------------------------------------------------

    private fun showPersonalInfo() {
        // Afficher/masquer les sections
        binding.personalInfoSection.visibility = android.view.View.VISIBLE
        binding.systemSection.visibility = android.view.View.GONE

        // Style de l'onglet actif (Personal Info)
        binding.personalInfoTab.apply {
            setTextColor(getColor(R.color.black))
            setBackgroundColor(getColor(R.color.white))
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        // Style de l'onglet inactif (System)
        binding.teamsTab.apply {
            setTextColor(getColor(android.R.color.darker_gray))
            setBackgroundColor(getColor(android.R.color.transparent))
            setTypeface(null, android.graphics.Typeface.NORMAL)
        }
    }

    private fun showSystemInfo() {
        // Afficher/masquer les sections
        binding.personalInfoSection.visibility = android.view.View.GONE
        binding.systemSection.visibility = android.view.View.VISIBLE

        // Style de l'onglet actif (System)
        binding.teamsTab.apply {
            setTextColor(getColor(R.color.black))
            setBackgroundColor(getColor(R.color.white))
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        // Style de l'onglet inactif (Personal Info)
        binding.personalInfoTab.apply {
            setTextColor(getColor(android.R.color.darker_gray))
            setBackgroundColor(getColor(android.R.color.transparent))
            setTypeface(null, android.graphics.Typeface.NORMAL)
        }
    }
}