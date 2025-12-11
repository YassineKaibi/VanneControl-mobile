package com.example.myapplicationv10

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.myapplicationv10.databinding.ActivityProfileBinding
import com.example.myapplicationv10.network.NetworkResult
import com.example.myapplicationv10.viewmodel.ProfileViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class ProfileActivity : BaseActivity() {


    private lateinit var binding: ActivityProfileBinding
    private lateinit var editProfileLauncher: ActivityResultLauncher<Intent>
    private lateinit var viewModel: ProfileViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize ViewModel
        viewModel = ViewModelProvider(this)[ProfileViewModel::class.java]

        // 1️⃣ Initialiser le launcher pour EditProfileActivity
        editProfileLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                // Reload profile data from backend after edit
                viewModel.loadUserProfile()
            }
        }

        setupBackButton()
        setupTabNavigation()
        setupLogout()
        observeProfileState()

        // Load user profile from backend
        viewModel.loadUserProfile()

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

    private fun observeProfileState() {
        lifecycleScope.launch {
            viewModel.profileState.collect { result ->
                when (result) {
                    is NetworkResult.Idle -> {
                        // Initial state
                        binding.loadingIndicator.visibility = View.GONE
                    }

                    is NetworkResult.Loading -> {
                        // Show loading indicator
                        binding.loadingIndicator.visibility = View.VISIBLE
                    }

                    is NetworkResult.Success -> {
                        // Hide loading indicator
                        binding.loadingIndicator.visibility = View.GONE

                        // Update UI with real backend data
                        val user = result.data
                        binding.firstNameValue.text = user.firstName ?: "N/A"
                        binding.lastNameValue.text = user.lastName ?: "N/A"
                        binding.emailValue.text = user.email
                        binding.phoneNumberValue.text = user.phoneNumber ?: "N/A"
                        binding.locationValue.text = user.location ?: "N/A"
                        binding.dateOfBirthValue.text = user.dateOfBirth ?: "N/A"

                        // Update header with full name
                        val fullName = "${user.firstName ?: ""} ${user.lastName ?: ""}".trim()
                        binding.userFullName.text = fullName.ifEmpty { "User" }
                        binding.userEmailHeader.text = user.email

                        // Save to SharedPreferences as cache
                        val prefs = getSharedPreferences("app_preferences", MODE_PRIVATE)
                        prefs.edit().apply {
                            putString("user_first_name", user.firstName)
                            putString("user_last_name", user.lastName)
                            putString("user_email", user.email)
                            putString("user_phone", user.phoneNumber)
                            putString("user_location", user.location)
                            putString("user_date_of_birth", user.dateOfBirth)
                            apply()
                        }
                    }

                    is NetworkResult.Error -> {
                        // Hide loading indicator
                        binding.loadingIndicator.visibility = View.GONE

                        // Show error message
                        Snackbar.make(
                            binding.root,
                            "Failed to load profile: ${result.message}",
                            Snackbar.LENGTH_LONG
                        ).show()

                        // Load cached data from SharedPreferences as fallback
                        loadCachedData()
                    }
                }
            }
        }
    }

    private fun loadCachedData() {
        val prefs = getSharedPreferences("app_preferences", MODE_PRIVATE)

        binding.firstNameValue.text = prefs.getString("user_first_name", "N/A") ?: "N/A"
        binding.lastNameValue.text = prefs.getString("user_last_name", "N/A") ?: "N/A"
        binding.emailValue.text = prefs.getString("user_email", "N/A") ?: "N/A"
        binding.phoneNumberValue.text = prefs.getString("user_phone", "N/A") ?: "N/A"
        binding.locationValue.text = prefs.getString("user_location", "N/A") ?: "N/A"
        binding.dateOfBirthValue.text = prefs.getString("user_date_of_birth", "N/A") ?: "N/A"

        val fullName = "${prefs.getString("user_first_name", "")} ${prefs.getString("user_last_name", "")}".trim()
        binding.userFullName.text = fullName.ifEmpty { "User" }
        binding.userEmailHeader.text = prefs.getString("user_email", "N/A") ?: "N/A"
    }

}
