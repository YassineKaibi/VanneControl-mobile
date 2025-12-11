package com.example.myapplicationv10

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.myapplicationv10.network.NetworkResult
import com.example.myapplicationv10.viewmodel.RegisterViewModel
import com.example.myapplicationv10.websocket.WebSocketManager
import kotlinx.coroutines.launch

/**
 * RegisterActivity - Écran d'inscription avec MVVM
 *
 * Utilise RegisterViewModel pour gérer l'inscription
 * Observe les StateFlow pour mettre à jour l'UI de manière réactive
 */
class RegisterActivity : BaseActivity() {

    private lateinit var firstNameField: EditText
    private lateinit var lastNameField: EditText
    private lateinit var emailField: EditText
    private lateinit var phoneField: EditText
    private lateinit var passwordField: EditText
    private lateinit var confirmPasswordField: EditText
    private lateinit var registerButton: Button
    private lateinit var signInText: TextView

    // ViewModel
    private val viewModel: RegisterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        initializeViews()
        setupClickListeners()
        observeViewModel()
    }

    private fun initializeViews() {
        // Note: Ces IDs doivent correspondre à votre layout XML
        // Si les IDs sont différents, ajustez-les
        firstNameField = findViewById(R.id.firstNameField)
        lastNameField = findViewById(R.id.lastNameField)
        emailField = findViewById(R.id.emailField)
        phoneField = findViewById(R.id.phoneField)
        passwordField = findViewById(R.id.passwordField)
        confirmPasswordField = findViewById(R.id.confirmPasswordField)
        registerButton = findViewById(R.id.registerButton)
        signInText = findViewById(R.id.haveAccount)
    }

    private fun setupClickListeners() {
        // Clic sur "S'inscrire"
        registerButton.setOnClickListener {
            val firstName = firstNameField.text.toString().trim()
            val lastName = lastNameField.text.toString().trim()
            val email = emailField.text.toString().trim()
            val phone = phoneField.text.toString().trim()
            val password = passwordField.text.toString().trim()
            val confirmPassword = confirmPasswordField.text.toString().trim()

            viewModel.register(firstName, lastName, email, phone, password, confirmPassword)
        }

        // Clic sur "Already have an account? Sign in"
        signInText.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    /**
     * Observer les StateFlow du ViewModel
     */
    private fun observeViewModel() {
        // Observer l'état d'inscription
        lifecycleScope.launch {
            viewModel.registerState.collect { result ->
                when (result) {
                    is NetworkResult.Idle -> {
                        // État initial - Ne rien faire
                        hideLoading()
                    }

                    is NetworkResult.Loading -> {
                        showLoading()
                    }

                    is NetworkResult.Success -> {
                        hideLoading()
                        Toast.makeText(
                            this@RegisterActivity,
                            "Inscription réussie!",
                            Toast.LENGTH_SHORT
                        ).show()

                        // Connecter au WebSocket
                        WebSocketManager.getInstance(this@RegisterActivity).connect()

                        // Naviguer vers le Dashboard
                        navigateToDashboard()
                    }

                    is NetworkResult.Error -> {
                        hideLoading()
                        Toast.makeText(
                            this@RegisterActivity,
                            result.message,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }

        // Observer les erreurs de champs
        lifecycleScope.launch {
            viewModel.firstNameError.collect { error ->
                firstNameField.error = error
            }
        }

        lifecycleScope.launch {
            viewModel.lastNameError.collect { error ->
                lastNameField.error = error
            }
        }

        lifecycleScope.launch {
            viewModel.emailError.collect { error ->
                emailField.error = error
            }
        }

        lifecycleScope.launch {
            viewModel.phoneError.collect { error ->
                phoneField.error = error
            }
        }

        lifecycleScope.launch {
            viewModel.passwordError.collect { error ->
                passwordField.error = error
            }
        }

        lifecycleScope.launch {
            viewModel.confirmPasswordError.collect { error ->
                confirmPasswordField.error = error
            }
        }
    }

    private fun showLoading() {
        registerButton.isEnabled = false
        registerButton.text = "Inscription en cours..."
    }

    private fun hideLoading() {
        registerButton.isEnabled = true
        registerButton.text = "S'inscrire"
    }

    private fun navigateToDashboard() {
        val intent = Intent(this, DashboardActivity::class.java)
        // Clear back stack - user cannot go back to registration/login
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.resetState()
    }
}
