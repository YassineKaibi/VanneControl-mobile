package com.example.myapplicationv10

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.snackbar.Snackbar

class ProfileActivity : AppCompatActivity() {

    // Données de profil (à remplacer par des vraies données de la base de données plus tard)
    data class UserProfile(
        val firstName: String,
        val lastName: String,
        val dateOfBirth: String,
        val location: String,
        val phoneNumber: String,
        val email: String,
        val numberOfValves: Int,
        val gender: String
    )

    private lateinit var userProfile: UserProfile

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.topBar)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Données de profil exemple
        userProfile = UserProfile(
            firstName = "Yassine",
            lastName = "Channa",
            dateOfBirth = "15/03/1995",
            location = "Houmt Souk, Medenine, Tunisia",
            phoneNumber = "+216 XX XXX XXX",
            email = "admin@vannecontrol.com",
            numberOfValves = 8,
            gender = "Male"
        )

        setupBackButton()
        setupEditButton()
        loadProfileData()
    }

    private fun setupBackButton() {
        findViewById<ImageView>(R.id.backButton).setOnClickListener {
            finish()
        }
    }

    private fun setupEditButton() {
        findViewById<CardView>(R.id.editProfileButton).setOnClickListener {
            // TODO: Ouvrir l'écran d'édition du profil
            Snackbar.make(it, "Edit profile - Coming soon", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun loadProfileData() {
        // Afficher les données du profil
        findViewById<TextView>(R.id.userFullName).text = "${userProfile.firstName} ${userProfile.lastName}"
        findViewById<TextView>(R.id.userEmail).text = userProfile.email

        findViewById<TextView>(R.id.firstNameValue).text = userProfile.firstName
        findViewById<TextView>(R.id.lastNameValue).text = userProfile.lastName
        findViewById<TextView>(R.id.dateOfBirthValue).text = userProfile.dateOfBirth
        findViewById<TextView>(R.id.locationValue).text = userProfile.location
        findViewById<TextView>(R.id.phoneNumberValue).text = userProfile.phoneNumber
        findViewById<TextView>(R.id.emailValue).text = userProfile.email
        findViewById<TextView>(R.id.numberOfValvesValue).text = userProfile.numberOfValves.toString()
        findViewById<TextView>(R.id.genderValue).text = userProfile.gender
    }
}