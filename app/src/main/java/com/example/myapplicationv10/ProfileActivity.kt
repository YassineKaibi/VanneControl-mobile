package com.example.myapplicationv10

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import coil.load
import coil.transform.CircleCropTransformation
import com.example.myapplicationv10.databinding.ActivityProfileBinding
import com.example.myapplicationv10.network.NetworkResult
import com.example.myapplicationv10.utils.Constants
import com.example.myapplicationv10.utils.ValveLimitManager
import com.example.myapplicationv10.viewmodel.ProfileViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

/**
 * ProfileActivity - Écran de profil utilisateur avec affichage d'avatar
 *
 * Fonctionnalités:
 * - Affichage des informations du profil
 * - Chargement de l'avatar avec Coil
 * - Navigation vers l'édition
 * - Déconnexion
 */
class ProfileActivity : BaseActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var editProfileLauncher: ActivityResultLauncher<Intent>
    private lateinit var viewModel: ProfileViewModel

    // Store current user data for passing to edit screen
    private var currentAvatarUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[ProfileViewModel::class.java]

        // Initialiser le launcher pour EditProfileActivity
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
        setupEditButton()
        observeProfileState()
        loadValveLimit()

        // Load user profile from backend
        viewModel.loadUserProfile()
    }

    private fun setupBackButton() {
        binding.backButton.setOnClickListener { finish() }
    }

    private fun setupTabNavigation() {
        binding.personalInfoTab.setOnClickListener {
            binding.personalInfoTab.setBackgroundResource(R.drawable.tab_selected_background)
            binding.personalInfoTab.setTextColor(getColor(R.color.white))
            binding.teamsTab.setBackgroundResource(android.R.color.transparent)
            binding.teamsTab.setTextColor(getColor(R.color.black))
        }

        binding.teamsTab.setOnClickListener {
            binding.teamsTab.setBackgroundResource(R.drawable.tab_selected_background)
            binding.teamsTab.setTextColor(getColor(R.color.white))
            binding.personalInfoTab.setBackgroundResource(android.R.color.transparent)
            binding.personalInfoTab.setTextColor(getColor(R.color.black))
            Snackbar.make(binding.root, "System settings coming soon", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun setupEditButton() {
        binding.editButton.setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java)

            // Pass current data to edit screen
            intent.putExtra("firstName", binding.firstNameValue.text.toString())
            intent.putExtra("lastName", binding.lastNameValue.text.toString())
            intent.putExtra("dateOfBirth", binding.dateOfBirthValue.text.toString())
            intent.putExtra("email", binding.emailValue.text.toString())
            intent.putExtra("phoneNumber", binding.phoneNumberValue.text.toString())
            intent.putExtra("location", binding.locationValue.text.toString())
            intent.putExtra("numberOfValves", binding.numberOfValvesValue.text.toString().toIntOrNull() ?: 8)
            intent.putExtra("avatarUrl", currentAvatarUrl)  // Pass avatar URL

            editProfileLauncher.launch(intent)
        }
    }

    private fun setupLogout() {
        binding.logoutButton.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout") { _, _ -> performLogout() }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun performLogout() {
        // Clear auth tokens
        com.example.myapplicationv10.utils.TokenManager.getInstance(this).logout()

        // Clear other preferences
        val prefs = getSharedPreferences("app_preferences", MODE_PRIVATE)
        prefs.edit().clear().apply()

        // Navigate to login
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun loadValveLimit() {
        val valveLimitManager = ValveLimitManager.getInstance(this)
        val limit = valveLimitManager.getValveLimit()
        binding.numberOfValvesValue.text = limit.toString()
    }

    private fun observeProfileState() {
        lifecycleScope.launch {
            viewModel.profileState.collect { result ->
                when (result) {
                    is NetworkResult.Idle -> {
                        binding.loadingIndicator.visibility = View.GONE
                    }

                    is NetworkResult.Loading -> {
                        binding.loadingIndicator.visibility = View.VISIBLE
                    }

                    is NetworkResult.Success -> {
                        binding.loadingIndicator.visibility = View.GONE

                        val user = result.data

                        // Store avatar URL for edit screen (fix localhost URLs)
                        currentAvatarUrl = Constants.fixAvatarUrl(user.avatarUrl)

                        // Update text fields
                        binding.firstNameValue.text = user.firstName ?: "N/A"
                        binding.lastNameValue.text = user.lastName ?: "N/A"
                        binding.emailValue.text = user.email
                        binding.phoneNumberValue.text = user.phoneNumber ?: "N/A"
                        binding.locationValue.text = user.location ?: "N/A"
                        binding.dateOfBirthValue.text = formatDateForDisplay(user.dateOfBirth)

                        // Update header with full name
                        val fullName = "${user.firstName ?: ""} ${user.lastName ?: ""}".trim()
                        binding.userFullName.text = fullName.ifEmpty { "User" }
                        binding.userEmailHeader.text = user.email

                        // Load avatar with Coil (already fixed above)
                        loadAvatar(currentAvatarUrl)

                        // Update valve limit from preferences if stored
                        val valveLimitManager = ValveLimitManager.getInstance(this@ProfileActivity)
                        val currentLimit = valveLimitManager.getValveLimit()
                        binding.numberOfValvesValue.text = currentLimit.toString()
                    }

                    is NetworkResult.Error -> {
                        binding.loadingIndicator.visibility = View.GONE
                        Snackbar.make(
                            binding.root,
                            "Failed to load profile: ${result.message}",
                            Snackbar.LENGTH_LONG
                        ).setAction("Retry") {
                            viewModel.loadUserProfile()
                        }.show()
                    }
                }
            }
        }
    }

    /**
     * Load avatar image using Coil
     */
    private fun loadAvatar(url: String?) {
        binding.profilePicture.load(url) {
            crossfade(true)
            placeholder(R.drawable.ic_avatar_placeholder)
            error(R.drawable.ic_avatar_placeholder)
            transformations(CircleCropTransformation())
        }
    }

    /**
     * Format date from API format (yyyy-MM-dd) to display format (dd/MM/yyyy)
     */
    private fun formatDateForDisplay(date: String?): String {
        if (date.isNullOrEmpty()) return "N/A"

        return try {
            val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            val outputFormat = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
            val parsedDate = inputFormat.parse(date)
            outputFormat.format(parsedDate!!)
        } catch (e: Exception) {
            date
        }
    }

    override fun onResume() {
        super.onResume()
        loadValveLimit()
    }
}