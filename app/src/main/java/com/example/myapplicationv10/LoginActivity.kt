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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class LoginActivity : AppCompatActivity() {

    private lateinit var emailField: EditText
    private lateinit var passwordField: EditText
    private lateinit var loginButton: Button
    private lateinit var registrationText: TextView
    private lateinit var loadingProgressBar: ProgressBar

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
    }

    private fun initializeViews() {
        emailField = findViewById(R.id.emailField)
        passwordField = findViewById(R.id.passwordField)
        loginButton = findViewById(R.id.loginButton)
        registrationText = findViewById(R.id.registration)

        // Créer un ProgressBar programmatiquement si nécessaire
        // ou l'ajouter dans le layout XML
        loadingProgressBar = ProgressBar(this).apply {
            visibility = View.GONE
        }
    }

    private fun setupClickListeners() {
        // Clic sur "Se connecter"
        loginButton.setOnClickListener {
            val email = emailField.text.toString().trim()
            val password = passwordField.text.toString().trim()

            if (validateInput(email, password)) {
                performLogin(email, password)
            }
        }

        // Clic sur "Sign up now"
        registrationText.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun validateInput(email: String, password: String): Boolean {
        if (email.isEmpty()) {
            emailField.error = "Email requis"
            return false
        }

        if (password.isEmpty()) {
            passwordField.error = "Mot de passe requis"
            return false
        }

        if (password.length < 6) {
            passwordField.error = "Mot de passe trop court"
            return false
        }

        return true
    }

    private fun performLogin(email: String, password: String) {
        // Désactiver le bouton et afficher le chargement
        runOnUiThread {
            loginButton.isEnabled = false
            loginButton.text = "Connexion en cours..."
            // loadingProgressBar.visibility = View.VISIBLE
        }

        // Effectuer l'authentification en arrière-plan
        runOnNetwork {
            try {
                // Simuler un appel API
                val loginResult = authenticateUser(email, password)

                // Traiter le résultat sur le thread principal
                runOnUiThread {
                    handleLoginResult(loginResult)
                }

            } catch (e: Exception) {
                e.printStackTrace()

                // Gérer l'erreur sur le thread principal
                runOnUiThread {
                    handleLoginError(e)
                }
            }
        }
    }

    private fun authenticateUser(email: String, password: String): LoginResult {
        // TODO: Remplacer par un vrai appel API
        // Exemple:
        // val response = apiClient.login(email, password)
        // return LoginResult(
        //     success = response.isSuccessful,
        //     token = response.body?.token,
        //     userId = response.body?.userId,
        //     message = response.message
        // )

        // Simuler un délai réseau
        Thread.sleep(1500)

        // Vérification temporaire (à remplacer)
        return if (email == "admin" && password == "admin123") {
            LoginResult(
                success = true,
                token = "fake_jwt_token_12345",
                userId = "user_001",
                message = "Connexion réussie"
            )
        } else {
            LoginResult(
                success = false,
                token = null,
                userId = null,
                message = "Email ou mot de passe incorrect"
            )
        }
    }

    private fun handleLoginResult(result: LoginResult) {
        // Réactiver le bouton
        loginButton.isEnabled = true
        loginButton.text = "Se connecter"

        if (result.success) {
            Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show()

            // Sauvegarder le token en arrière-plan
            runOnDatabase {
                saveAuthToken(result.token, result.userId)
            }

            // Naviguer vers le Dashboard
            val intent = Intent(this, DashboardActivity::class.java)
            startActivity(intent)
            finish()

        } else {
            Toast.makeText(this, result.message, Toast.LENGTH_LONG).show()
        }
    }

    private fun handleLoginError(error: Exception) {
        // Réactiver le bouton
        loginButton.isEnabled = true
        loginButton.text = "Se connecter"

        Toast.makeText(
            this,
            "Erreur de connexion: ${error.message}",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun saveAuthToken(token: String?, userId: String?) {
        try {
            // TODO: Sauvegarder le token de manière sécurisée
            // Exemple avec SharedPreferences (ou mieux: EncryptedSharedPreferences)
            // val prefs = getSharedPreferences("auth", Context.MODE_PRIVATE)
            // prefs.edit()
            //     .putString("auth_token", token)
            //     .putString("user_id", userId)
            //     .apply()

            println("💾 Token sauvegardé: $token")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Data class pour le résultat de l'authentification
    data class LoginResult(
        val success: Boolean,
        val token: String?,
        val userId: String?,
        val message: String
    )
}