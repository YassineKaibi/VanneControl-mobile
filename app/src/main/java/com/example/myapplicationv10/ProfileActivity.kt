package com.example.myapplicationv10
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplicationv10.databinding.ActivityProfileBinding

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialisation du ViewBinding
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // -------------------------------
        // TOP BAR EVENTS
        // -------------------------------

        binding.backButton.setOnClickListener {
            finish() // Retour en arrière
        }

        binding.editButton.setOnClickListener {
            Toast.makeText(this, "Edit Profile clicked", Toast.LENGTH_SHORT).show()
        }

        binding.closeButton.setOnClickListener {
            Toast.makeText(this, "Close clicked", Toast.LENGTH_SHORT).show()
        }

        // -------------------------------
        // CHANGE PROFILE PICTURE
        // -------------------------------

        binding.changePhotoButton.setOnClickListener {
            Toast.makeText(this, "Change photo clicked", Toast.LENGTH_SHORT).show()
        }

        // -------------------------------
        // TABS SWITCHING
        // -------------------------------

        binding.personalInfoTab.setOnClickListener {
            showPersonalInfo()
        }

        binding.teamsTab.setOnClickListener {
            showSystemInfo()
        }

        // Afficher l’onglet par défaut
        showPersonalInfo()
    }

    // -----------------------------------------------------
    //        FONCTIONS POUR L'AFFICHAGE DES SECTIONS
    // -----------------------------------------------------

    private fun showPersonalInfo() {
        binding.personalInfoSection.visibility = android.view.View.VISIBLE
        binding.systemSection.visibility = android.view.View.GONE

        // Changer style onglets
        binding.personalInfoTab.setTextColor(getColor(android.R.color.black))
        binding.teamsTab.setTextColor(getColor(android.R.color.darker_gray))
        binding.teamsTab.setBackgroundColor(getColor(android.R.color.transparent))
    }

    private fun showSystemInfo() {
        binding.personalInfoSection.visibility = android.view.View.GONE
        binding.systemSection.visibility = android.view.View.VISIBLE

        // Changer style onglets
        binding.teamsTab.setTextColor(getColor(android.R.color.black))
        binding.personalInfoTab.setTextColor(getColor(android.R.color.darker_gray))
        binding.teamsTab.setBackgroundColor(getColor(android.R.color.holo_green_light))
    }
}
