package com.example.myapplicationv10.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplicationv10.model.AuthResponse
import com.example.myapplicationv10.network.NetworkResult
import com.example.myapplicationv10.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * LoginViewModel - ViewModel pour l'écran de connexion
 *
 * Gère l'état de l'UI et les opérations de connexion
 * Utilise StateFlow pour la réactivité
 */
class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository = AuthRepository(application)

    // État de la connexion
    private val _loginState = MutableStateFlow<NetworkResult<AuthResponse>>(NetworkResult.Idle)
    val loginState: StateFlow<NetworkResult<AuthResponse>> = _loginState.asStateFlow()

    // État de validation des champs
    private val _emailError = MutableStateFlow<String?>(null)
    val emailError: StateFlow<String?> = _emailError.asStateFlow()

    private val _passwordError = MutableStateFlow<String?>(null)
    val passwordError: StateFlow<String?> = _passwordError.asStateFlow()

    /**
     * Connexion de l'utilisateur
     *
     * @param email Email de l'utilisateur
     * @param password Mot de passe
     */
    fun login(email: String, password: String) {
        // Valider les champs
        if (!validateInput(email, password)) {
            return
        }

        // Effacer les erreurs précédentes
        _emailError.value = null
        _passwordError.value = null

        // Afficher le chargement
        _loginState.value = NetworkResult.Loading

        // Effectuer la connexion
        viewModelScope.launch {
            val result = authRepository.login(email, password)
            _loginState.value = result
        }
    }

    /**
     * Valider les champs de saisie
     */
    private fun validateInput(email: String, password: String): Boolean {
        var isValid = true

        if (email.isEmpty()) {
            _emailError.value = "Email requis"
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailError.value = "Email invalide"
            isValid = false
        } else {
            _emailError.value = null
        }

        if (password.isEmpty()) {
            _passwordError.value = "Mot de passe requis"
            isValid = false
        } else if (password.length < 6) {
            _passwordError.value = "Mot de passe trop court (min. 6 caractères)"
            isValid = false
        } else {
            _passwordError.value = null
        }

        return isValid
    }

    /**
     * Réinitialiser l'état de connexion
     * Utile pour effacer les messages d'erreur après navigation
     */
    fun resetState() {
        _loginState.value = NetworkResult.Idle
        _emailError.value = null
        _passwordError.value = null
    }

    /**
     * Vérifier si l'utilisateur est déjà connecté
     */
    fun isLoggedIn(): Boolean {
        return authRepository.isLoggedIn()
    }
}
