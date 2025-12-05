package com.example.myapplicationv10

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.myapplicationv10.network.NetworkResult
import com.example.myapplicationv10.viewmodel.LoginViewModel
import com.example.myapplicationv10.websocket.WebSocketManager
import kotlinx.coroutines.launch


/**
 * LoginActivity - Écran de connexion avec MVVM
 *
 * Utilise LoginViewModel pour gérer l'authentification
 * Observe les StateFlow pour mettre à jour l'UI de manière réactive
 */
class LoginActivity : BaseActivity() {

    private lateinit var emailField: EditText
    private lateinit var passwordField: EditText
    private lateinit var loginButton: Button
    private lateinit var registrationText: TextView
    private lateinit var loadingProgressBar: ProgressBar

    // ViewModel
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.loginLayout)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initializeViews()
        setupClickListeners()
        observeViewModel()
        /*
        // Vérifier si l'utilisateur est déjà connecté
        if (viewModel.isLoggedIn()) {
            navigateToDashboard()
        }
        */
    }


    private fun initializeViews() {
        emailField = findViewById(R.id.emailField)
        passwordField = findViewById(R.id.passwordField)
        loginButton = findViewById(R.id.loginButton)
        registrationText = findViewById(R.id.registration)

        // Créer un ProgressBar programmatiquement si nécessaire
        loadingProgressBar = ProgressBar(this).apply {
            visibility = View.GONE
        }
    }

    private fun setupClickListeners() {
        // Clic sur "Se connecter"
        loginButton.setOnClickListener {
            val email = emailField.text.toString().trim()
            val password = passwordField.text.toString().trim()

            viewModel.login(email, password)
        }

        // Clic sur "Sign up now"
        registrationText.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    /**
     * Observer les StateFlow du ViewModel
     */
    private fun observeViewModel() {
        // Observer l'état de connexion
        lifecycleScope.launch {
            viewModel.loginState.collect { result ->
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
                            this@LoginActivity,
                            "Connexion réussie!",
                            Toast.LENGTH_SHORT
                        ).show()

                        // Connecter au WebSocket
                        WebSocketManager.getInstance(this@LoginActivity).connect()

                        // Naviguer vers le Dashboard
                        navigateToDashboard()
                    }

                    is NetworkResult.Error -> {
                        hideLoading()
                        Toast.makeText(
                            this@LoginActivity,
                            result.message,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }

        // Observer les erreurs de champ email
        lifecycleScope.launch {
            viewModel.emailError.collect { error ->
                emailField.error = error
            }
        }

        // Observer les erreurs de champ password
        lifecycleScope.launch {
            viewModel.passwordError.collect { error ->
                passwordField.error = error
            }
        }
    }

    private fun showLoading() {
        loginButton.isEnabled = false
        loginButton.text = "Connexion en cours..."
        loadingProgressBar.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        loginButton.isEnabled = true
        loginButton.text = "Se connecter"
        loadingProgressBar.visibility = View.GONE
    }

    private fun navigateToDashboard() {
        val intent = Intent(this, DashboardActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.resetState()
    }
}