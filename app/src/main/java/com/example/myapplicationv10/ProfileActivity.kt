package com.example.myapplicationv10

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplicationv10.databinding.ActivityProfileBinding
import com.google.android.material.snackbar.Snackbar

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding

    companion object {
        private const val EDIT_PROFILE_REQUEST = 100
    }

    // Data class pour les informations du profil
    data class UserProfile(
        var firstName: String,
        var lastName: String,
        var dateOfBirth: String,
        var email: String,
        var phoneNumber: String,
        var location: String,
        var numberOfValves: Int
    )

    // Données du profil
    private var userProfile = UserProfile(
        firstName = "Yassine",
        lastName = "Channa",
        dateOfBirth = "15/03/1995",
        email = "admin@vannecontrol.com",
        phoneNumber = "+216 XX XXX XXX",
        location = "Houmt Souk, Medenine, Tunisia",
        numberOfValves = 8
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialisation du ViewBinding
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Charger les données du profil
        loadProfileData()

        // Configuration des événements
        setupTopBarEvents()
        setupPhotoChangeButton()
        setupTabs()

        // Afficher l'onglet par défaut
        showPersonalInfo()
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

    // -----------------------------------------------------
    //        CHARGEMENT DES DONNÉES
    // -----------------------------------------------------

    private fun loadProfileData() {
        // Mettre à jour l'en-tête
        binding.userFullName.text = "${userProfile.firstName} ${userProfile.lastName}"
        binding.userEmailHeader.text = userProfile.email

        // Mettre à jour Personal Info
        binding.firstNameValue.text = userProfile.firstName
        binding.lastNameValue.text = userProfile.lastName
        binding.dateOfBirthValue.text = userProfile.dateOfBirth
        binding.emailValue.text = userProfile.email
        binding.phoneNumberValue.text = userProfile.phoneNumber
        binding.locationValue.text = userProfile.location

        // Mettre à jour System Info
        binding.numberOfValvesValue.text = "${userProfile.numberOfValves} valves to manage"
    }

    // -----------------------------------------------------
    //        NAVIGATION VERS EDIT PROFILE
    // -----------------------------------------------------

    private fun openEditProfile() {
        val intent = Intent(this, EditProfileActivity::class.java)

        // Passer les données actuelles
        intent.putExtra("firstName", userProfile.firstName)
        intent.putExtra("lastName", userProfile.lastName)
        intent.putExtra("dateOfBirth", userProfile.dateOfBirth)
        intent.putExtra("email", userProfile.email)
        intent.putExtra("phoneNumber", userProfile.phoneNumber)
        intent.putExtra("location", userProfile.location)
        intent.putExtra("numberOfValves", userProfile.numberOfValves)

        startActivityForResult(intent, EDIT_PROFILE_REQUEST)
    }

    // -----------------------------------------------------
    //        RÉCEPTION DES DONNÉES MODIFIÉES
    // -----------------------------------------------------

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == EDIT_PROFILE_REQUEST && resultCode == RESULT_OK) {
            data?.let {
                // Récupérer les nouvelles données
                userProfile.firstName = it.getStringExtra("firstName") ?: userProfile.firstName
                userProfile.lastName = it.getStringExtra("lastName") ?: userProfile.lastName
                userProfile.dateOfBirth = it.getStringExtra("dateOfBirth") ?: userProfile.dateOfBirth
                userProfile.email = it.getStringExtra("email") ?: userProfile.email
                userProfile.phoneNumber = it.getStringExtra("phone") ?: userProfile.phoneNumber
                userProfile.location = it.getStringExtra("location") ?: userProfile.location
                userProfile.numberOfValves = it.getIntExtra("numberOfValves", userProfile.numberOfValves)

                // Recharger l'interface
                loadProfileData()

                // Afficher un message de confirmation
                Snackbar.make(
                    binding.root,
                    "Profile updated successfully!",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }
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