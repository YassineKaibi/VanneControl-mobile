package com.example.myapplicationv10

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplicationv10.databinding.ActivityProfileBinding
import com.google.android.material.button.MaterialButton

class ProfileActivity : BaseActivity() {


    private lateinit var binding: ActivityProfileBinding
    private lateinit var editProfileLauncher: ActivityResultLauncher<Intent>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1️⃣ Initialiser le launcher pour EditProfileActivity
        val editProfileLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                val data = result.data!!
                binding.firstNameValue.text = data.getStringExtra("firstName") ?: binding.firstNameValue.text
                binding.lastNameValue.text = data.getStringExtra("lastName") ?: binding.lastNameValue.text
                binding.dateOfBirthValue.text = data.getStringExtra("dateOfBirth") ?: binding.dateOfBirthValue.text
                binding.emailValue.text = data.getStringExtra("email") ?: binding.emailValue.text
                binding.phoneNumberValue.text = data.getStringExtra("phone") ?: binding.phoneNumberValue.text
                binding.locationValue.text = data.getStringExtra("location") ?: binding.locationValue.text
                binding.numberOfValvesValue.text = (data.getIntExtra("numberOfValves", 8)).toString()
            }
        }

        setupBackButton()
        setupTabNavigation()
        setupLogout()
        loadUserData()

        // 2️⃣ Lancer EditProfileActivity avec le launcher quand on clique sur editButton
        binding.editButton.setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java)

            // Passer les données actuelles
            intent.putExtra("firstName", binding.firstNameValue.text.toString())
            intent.putExtra("lastName", binding.lastNameValue.text.toString())
            intent.putExtra("dateOfBirth", binding.dateOfBirthValue.text.toString())
            intent.putExtra("email", binding.emailValue.text.toString())
            intent.putExtra("phoneNumber", binding.phoneNumberValue.text.toString())
            intent.putExtra("location", binding.locationValue.text.toString())
            intent.putExtra("numberOfValves", binding.numberOfValvesValue.text.toString().toIntOrNull() ?: 8)

            editProfileLauncher.launch(intent)
        }
    }


    private fun setupBackButton() {
        binding.backButton.setOnClickListener { finish() }
    }

    private fun setupTabNavigation() {
        binding.personalInfoTab.setOnClickListener { showPersonalInfo() }
        binding.teamsTab.setOnClickListener { showSystemSettings() }

        // Afficher les informations personnelles par défaut
        showPersonalInfo()
    }

    private fun showPersonalInfo() {
        binding.personalInfoSection.visibility = View.VISIBLE
        binding.systemSection.visibility = View.GONE

        binding.personalInfoTab.setTextColor(resources.getColor(R.color.black, null))
        binding.personalInfoTab.setBackgroundColor(resources.getColor(R.color.white, null))
        binding.personalInfoTab.setTypeface(null, android.graphics.Typeface.BOLD)

        binding.teamsTab.setTextColor(resources.getColor(android.R.color.darker_gray, null))
        binding.teamsTab.setBackgroundColor(resources.getColor(android.R.color.background_light, null))
        binding.teamsTab.setTypeface(null, android.graphics.Typeface.NORMAL)
    }

    private fun showSystemSettings() {
        binding.personalInfoSection.visibility = View.GONE
        binding.systemSection.visibility = View.VISIBLE

        binding.teamsTab.setTextColor(resources.getColor(R.color.black, null))
        binding.teamsTab.setBackgroundColor(resources.getColor(R.color.white, null))
        binding.teamsTab.setTypeface(null, android.graphics.Typeface.BOLD)

        binding.personalInfoTab.setTextColor(resources.getColor(android.R.color.darker_gray, null))
        binding.personalInfoTab.setBackgroundColor(resources.getColor(android.R.color.background_light, null))
        binding.personalInfoTab.setTypeface(null, android.graphics.Typeface.NORMAL)
    }

    private fun setupLogout() {
        binding.logoutButton.setOnClickListener { showLogoutConfirmation() }
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.logout))
            .setMessage(getString(R.string.logout_confirm))
            .setPositiveButton(getString(R.string.yes)) { _, _ -> logout() }
            .setNegativeButton(getString(R.string.no), null)
            .show()
    }

    private fun logout() {
        val prefs = getSharedPreferences("app_preferences", MODE_PRIVATE)
        prefs.edit().clear().apply()

        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun loadUserData() {
        val prefs = getSharedPreferences("app_preferences", MODE_PRIVATE)

        binding.firstNameValue.text = prefs.getString("user_first_name", "Yassine") ?: "Yassine"
        binding.lastNameValue.text = prefs.getString("user_last_name", "Kaibi") ?: "Kaibi"
        binding.emailValue.text = prefs.getString("user_email", "yassine@example.com") ?: "yassine@example.com"
        binding.phoneNumberValue.text = prefs.getString("user_phone", "+216 12 345 678") ?: "+216 12 345 678"
        binding.locationValue.text = prefs.getString("user_location", "Sfax, Tunisia") ?: "Sfax, Tunisia"
    }

}
