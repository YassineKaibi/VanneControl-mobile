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
 * RegisterViewModel - ViewModel pour l'écran d'inscription
 *
 * Gère l'état de l'UI et les opérations d'inscription
 * Utilise StateFlow pour la réactivité
 */
class RegisterViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository = AuthRepository(application)

    // État de l'inscription
    private val _registerState = MutableStateFlow<NetworkResult<AuthResponse>>(NetworkResult.Idle)
    val registerState: StateFlow<NetworkResult<AuthResponse>> = _registerState.asStateFlow()

    // État de validation des champs
    private val _firstNameError = MutableStateFlow<String?>(null)
    val firstNameError: StateFlow<String?> = _firstNameError.asStateFlow()

    private val _lastNameError = MutableStateFlow<String?>(null)
    val lastNameError: StateFlow<String?> = _lastNameError.asStateFlow()

    private val _emailError = MutableStateFlow<String?>(null)
    val emailError: StateFlow<String?> = _emailError.asStateFlow()

    private val _phoneError = MutableStateFlow<String?>(null)
    val phoneError: StateFlow<String?> = _phoneError.asStateFlow()

    private val _passwordError = MutableStateFlow<String?>(null)
    val passwordError: StateFlow<String?> = _passwordError.asStateFlow()

    private val _confirmPasswordError = MutableStateFlow<String?>(null)
    val confirmPasswordError: StateFlow<String?> = _confirmPasswordError.asStateFlow()

    /**
     * Inscription d'un nouvel utilisateur
     *
     * @param firstName Prénom de l'utilisateur
     * @param lastName Nom de famille de l'utilisateur
     * @param email Email de l'utilisateur
     * @param phone Numéro de téléphone
     * @param password Mot de passe
     * @param confirmPassword Confirmation du mot de passe
     */
    fun register(
        firstName: String,
        lastName: String,
        email: String,
        phone: String,
        password: String,
        confirmPassword: String
    ) {
        // Valider les champs
        if (!validateInput(firstName, lastName, email, phone, password, confirmPassword)) {
            return
        }

        // Effacer les erreurs précédentes
        clearErrors()

        // Afficher le chargement
        _registerState.value = NetworkResult.Loading

        // Effectuer l'inscription avec les nouvelles données
        viewModelScope.launch {
            val result = authRepository.register(firstName, lastName, email, phone, password)
            _registerState.value = result
        }
    }

    /**
     * Valider les champs de saisie
     */
    private fun validateInput(
        firstName: String,
        lastName: String,
        email: String,
        phone: String,
        password: String,
        confirmPassword: String
    ): Boolean {
        var isValid = true

        // Validation du prénom
        if (firstName.isEmpty()) {
            _firstNameError.value = "Prénom requis"
            isValid = false
        } else if (firstName.length < 2) {
            _firstNameError.value = "Prénom trop court (min. 2 caractères)"
            isValid = false
        } else {
            _firstNameError.value = null
        }

        // Validation du nom de famille
        if (lastName.isEmpty()) {
            _lastNameError.value = "Nom requis"
            isValid = false
        } else if (lastName.length < 2) {
            _lastNameError.value = "Nom trop court (min. 2 caractères)"
            isValid = false
        } else {
            _lastNameError.value = null
        }

        // Validation de l'email
        if (email.isEmpty()) {
            _emailError.value = "Email requis"
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailError.value = "Email invalide"
            isValid = false
        } else {
            _emailError.value = null
        }

        // Validation du téléphone
        if (phone.isEmpty()) {
            _phoneError.value = "Numéro de téléphone requis"
            isValid = false
        } else if (phone.length < 8) {
            _phoneError.value = "Numéro de téléphone invalide"
            isValid = false
        } else {
            _phoneError.value = null
        }

        // Validation du mot de passe
        if (password.isEmpty()) {
            _passwordError.value = "Mot de passe requis"
            isValid = false
        } else if (password.length < 6) {
            _passwordError.value = "Mot de passe trop court (min. 6 caractères)"
            isValid = false
        } else {
            _passwordError.value = null
        }

        // Validation de la confirmation du mot de passe
        if (confirmPassword.isEmpty()) {
            _confirmPasswordError.value = "Confirmation requise"
            isValid = false
        } else if (password != confirmPassword) {
            _confirmPasswordError.value = "Les mots de passe ne correspondent pas"
            isValid = false
        } else {
            _confirmPasswordError.value = null
        }

        return isValid
    }

    /**
     * Effacer toutes les erreurs
     */
    private fun clearErrors() {
        _firstNameError.value = null
        _lastNameError.value = null
        _emailError.value = null
        _phoneError.value = null
        _passwordError.value = null
        _confirmPasswordError.value = null
    }

    /**
     * Réinitialiser l'état d'inscription
     */
    fun resetState() {
        _registerState.value = NetworkResult.Idle
        clearErrors()
    }
}