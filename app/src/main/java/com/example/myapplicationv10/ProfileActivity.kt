package com.example.myapplicationv10

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.google.android.material.button.MaterialButton

class ProfileActivity : BaseActivity() {

    private lateinit var backButton: View
    private lateinit var personalInfoTab: TextView
    private lateinit var teamsTab: TextView
    private lateinit var personalInfoSection: LinearLayout
    private lateinit var systemSection: LinearLayout
    private lateinit var logoutButton: MaterialButton

    // User info views
    private lateinit var firstNameValue: TextView
    private lateinit var lastNameValue: TextView
    private lateinit var emailValue: TextView
    private lateinit var phoneNumberValue: TextView
    private lateinit var locationValue: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        initializeViews()
        setupTabNavigation()
        setupLogout()
        loadUserData()
    }

    private fun initializeViews() {
        backButton = findViewById(R.id.backButton)
        personalInfoTab = findViewById(R.id.personalInfoTab)
        teamsTab = findViewById(R.id.teamsTab)
        personalInfoSection = findViewById(R.id.personalInfoSection)
        systemSection = findViewById(R.id.systemSection)
        logoutButton = findViewById(R.id.logoutButton)

        // User info
        firstNameValue = findViewById(R.id.firstNameValue)
        lastNameValue = findViewById(R.id.lastNameValue)
        emailValue = findViewById(R.id.emailValue)
        phoneNumberValue = findViewById(R.id.phoneNumberValue)
        locationValue = findViewById(R.id.locationValue)

        backButton.setOnClickListener { finish() }
    }

    private fun setupTabNavigation() {
        personalInfoTab.setOnClickListener {
            showPersonalInfo()
        }

        teamsTab.setOnClickListener {
            showSystemSettings()
        }

        // Show personal info by default
        showPersonalInfo()
    }

    private fun showPersonalInfo() {
        personalInfoSection.visibility = View.VISIBLE
        systemSection.visibility = View.GONE

        personalInfoTab.setTextColor(resources.getColor(R.color.black, null))
        personalInfoTab.setBackgroundColor(resources.getColor(R.color.white, null))
        personalInfoTab.setTypeface(null, android.graphics.Typeface.BOLD)

        teamsTab.setTextColor(resources.getColor(android.R.color.darker_gray, null))
        teamsTab.setBackgroundColor(resources.getColor(android.R.color.background_light, null))
        teamsTab.setTypeface(null, android.graphics.Typeface.NORMAL)
    }

    private fun showSystemSettings() {
        personalInfoSection.visibility = View.GONE
        systemSection.visibility = View.VISIBLE

        teamsTab.setTextColor(resources.getColor(R.color.black, null))
        teamsTab.setBackgroundColor(resources.getColor(R.color.white, null))
        teamsTab.setTypeface(null, android.graphics.Typeface.BOLD)

        personalInfoTab.setTextColor(resources.getColor(android.R.color.darker_gray, null))
        personalInfoTab.setBackgroundColor(resources.getColor(android.R.color.background_light, null))
        personalInfoTab.setTypeface(null, android.graphics.Typeface.NORMAL)
    }

    private fun setupLogout() {
        logoutButton.setOnClickListener {
            showLogoutConfirmation()
        }
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.logout))
            .setMessage(getString(R.string.logout_confirm))
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                logout()
            }
            .setNegativeButton(getString(R.string.no), null)
            .show()
    }

    private fun logout() {
        // Clear user session
        val prefs = getSharedPreferences("app_preferences", MODE_PRIVATE)
        prefs.edit().clear().apply()

        // Navigate to login
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun loadUserData() {
        // Load user data from SharedPreferences or database
        val prefs = getSharedPreferences("app_preferences", MODE_PRIVATE)

        firstNameValue.text = prefs.getString("user_first_name", "Yassine") ?: "Yassine"
        lastNameValue.text = prefs.getString("user_last_name", "Kaibi") ?: "Kaibi"
        emailValue.text = prefs.getString("user_email", "yassine@example.com") ?: "yassine@example.com"
        phoneNumberValue.text = prefs.getString("user_phone", "+216 12 345 678") ?: "+216 12 345 678"
        locationValue.text = prefs.getString("user_location", "Sfax, Tunisia") ?: "Sfax, Tunisia"
    }
}