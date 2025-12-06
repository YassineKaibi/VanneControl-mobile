package com.example.myapplicationv10

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.myapplicationv10.databinding.ActivityLoginBinding
import com.example.myapplicationv10.network.NetworkResult
import com.example.myapplicationv10.viewmodel.LoginViewModel
import com.example.myapplicationv10.websocket.WebSocketManager
import kotlinx.coroutines.launch

class LoginActivity : BaseActivity() {


    private lateinit var loadingProgressBar: ProgressBar

    // ViewModel
    private val viewModel: LoginViewModel by viewModels()

    // View Binding
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.loginLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initializeViews()
        setupClickListeners()
        observeViewModel()
    }

    private fun initializeViews() {
        // Créer un ProgressBar programmatiquement si nécessaire
        loadingProgressBar = ProgressBar(this).apply {
            visibility = View.GONE
        }
        // Ajouter dynamiquement au layout si tu veux qu'il soit visible
        binding.loginLayout.addView(loadingProgressBar)
    }

    private fun setupClickListeners() {
        // Clic sur "Se connecter"
        binding.loginButton.setOnClickListener {
            val email = binding.emailField.text.toString().trim()
            val password = binding.passwordField.text.toString().trim()
            viewModel.login(email, password)
        }

        // Clic sur "Sign up now"
        binding.registration.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.loginState.collect { result ->
                when (result) {
                    is NetworkResult.Idle -> hideLoading()
                    is NetworkResult.Loading -> showLoading()
                    is NetworkResult.Success -> {
                        hideLoading()
                        Toast.makeText(this@LoginActivity, "Connexion réussie!", Toast.LENGTH_SHORT).show()
                        WebSocketManager.getInstance(this@LoginActivity).connect()
                        navigateToDashboard()
                    }
                    is NetworkResult.Error -> {
                        hideLoading()
                        Toast.makeText(this@LoginActivity, result.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewModel.emailError.collect { binding.emailField.error = it }
        }

        lifecycleScope.launch {
            viewModel.passwordError.collect { binding.passwordField.error = it }
        }
    }

    private fun showLoading() {
        binding.loginButton.isEnabled = false
        binding.loginButton.text = "Connexion en cours..."
        loadingProgressBar.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        binding.loginButton.isEnabled = true
        binding.loginButton.text = "Se connecter"
        loadingProgressBar.visibility = View.GONE
    }

    private fun navigateToDashboard() {
        startActivity(Intent(this, DashboardActivity::class.java))
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.resetState()
    }


}
