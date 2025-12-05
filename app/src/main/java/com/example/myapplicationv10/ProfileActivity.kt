package com.example.myapplicationv10

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.myapplicationv10.utils.LocaleHelper

class ProfileActivity : BaseActivity() {

    private lateinit var backButton: View
    private lateinit var closeButton: View
    private lateinit var personalInfoTab: TextView
    private lateinit var teamsTab: TextView
    private lateinit var personalInfoSection: LinearLayout
    private lateinit var systemSection: LinearLayout
    private lateinit var languageRow: LinearLayout
    private lateinit var languageValueText: TextView
    private lateinit var logoutRow: LinearLayout

    // User info views
    private lateinit var firstNameValue: TextView
    private lateinit var lastNameValue: TextView
    private lateinit var emailValue: TextView
    private lateinit var phoneValue: TextView
    private lateinit var locationValue: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        initializeViews()
        setupTabNavigation()
        setupLanguageSelector()
        setupLogout()
        loadUserData()
        updateLanguageDisplay()
    }

    private fun initializeViews() {
        backButton = findViewById(R.id.backButton)
        closeButton = findViewById(R.id.closeButton)
        personalInfoTab = findViewById(R.id.personalInfoTab)
        teamsTab = findViewById(R.id.teamsTab)
        personalInfoSection = findViewById(R.id.personalInfoSection)
        systemSection = findViewById(R.id.systemSection)
        languageRow = findViewById(R.id.languageRow)
        languageValueText = findViewById(R.id.languageValue)
        logoutRow = findViewById(R.id.logoutRow)

        // User info
        firstNameValue = findViewById(R.id.firstNameValue)
        lastNameValue = findViewById(R.id.lastNameValue)
        emailValue = findViewById(R.id.emailValue)
        phoneValue = findViewById(R.id.phoneValue)
        locationValue = findViewById(R.id.locationValue)

        backButton.setOnClickListener { finish() }
        closeButton.setOnClickListener { finish() }
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

    private fun setupLanguageSelector() {
        languageRow.setOnClickListener {
            showLanguageDialog()
        }
    }

    private fun showLanguageDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_language_selector)
        dialog.window?.setBackgroundDrawableResource(android.R.drawable.dialog_holo_light_frame)

        val radioGroup = dialog.findViewById<RadioGroup>(R.id.languageRadioGroup)
        val radioEnglish = dialog.findViewById<RadioButton>(R.id.radioEnglish)
        val radioFrench = dialog.findViewById<RadioButton>(R.id.radioFrench)
        val cancelButton = dialog.findViewById<Button>(R.id.cancelButton)
        val confirmButton = dialog.findViewById<Button>(R.id.confirmButton)

        // Set current language selection
        val currentLanguage = LocaleHelper.getLanguage(this)
        when (currentLanguage) {
            "en" -> radioEnglish.isChecked = true
            "fr" -> radioFrench.isChecked = true
        }

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        confirmButton.setOnClickListener {
            val selectedLanguage = when (radioGroup.checkedRadioButtonId) {
                R.id.radioEnglish -> "en"
                R.id.radioFrench -> "fr"
                else -> "en"
            }

            if (selectedLanguage != currentLanguage) {
                changeLanguage(selectedLanguage)
            }
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun changeLanguage(languageCode: String) {
        // Save language preference
        LocaleHelper.setLocale(this, languageCode)

        // Show success message
        Toast.makeText(this, getString(R.string.language_changed), Toast.LENGTH_SHORT).show()

        // Restart the activity to apply changes
        recreate()

        // Optionally, restart the entire app to apply language to all activities
        // val intent = Intent(this, MainActivity::class.java)
        // intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        // startActivity(intent)
        // finish()
    }

    private fun updateLanguageDisplay() {
        val currentLanguage = LocaleHelper.getLanguage(this)
        languageValueText.text = LocaleHelper.getLanguageName(currentLanguage)
    }

    private fun setupLogout() {
        logoutRow.setOnClickListener {
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
        phoneValue.text = prefs.getString("user_phone", "+216 12 345 678") ?: "+216 12 345 678"
        locationValue.text = prefs.getString("user_location", "Sfax, Tunisia") ?: "Sfax, Tunisia"
    }
}