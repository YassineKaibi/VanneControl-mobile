package com.example.myapplicationv10

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        // ✅ Ajustement des marges système
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.loginLayout)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // ✅ Récupération des vues
        val emailField = findViewById<EditText>(R.id.emailField)
        val passwordField = findViewById<EditText>(R.id.passwordField)
        val loginButton = findViewById<Button>(R.id.loginButton)
        val registrationText = findViewById<TextView>(R.id.registration)

        // ✅ Clic sur "Se connecter"
        loginButton.setOnClickListener {
            val email = emailField.text.toString().trim()
            val password = passwordField.text.toString().trim()

            // Vérification des identifiants
            if (email == "admin" && password == "admin123") {
                Toast.makeText(this, "Connexion réussie !", Toast.LENGTH_SHORT).show()

                // 🔹 Aller vers le Dashboard
                val intent = Intent(this, DashboardActivity::class.java)
                startActivity(intent)
                finish() // facultatif, empêche de revenir à la page de login avec le bouton "Retour"
            } else {
                Toast.makeText(this, "Email ou mot de passe incorrect", Toast.LENGTH_SHORT).show()
            }
        }

        // ✅ Clic sur "Sign up now"
        registrationText.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}
