package com.example.myapplicationv10.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplicationv10.model.User
import com.example.myapplicationv10.network.NetworkResult
import com.example.myapplicationv10.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ProfileViewModel - ViewModel pour l'écran de profil
 *
 * Gère l'état de l'UI et les opérations de profil utilisateur
 * Utilise StateFlow pour la réactivité
 */
class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val userRepository = UserRepository(application)

    // État du chargement du profil
    private val _profileState = MutableStateFlow<NetworkResult<User>>(NetworkResult.Idle)
    val profileState: StateFlow<NetworkResult<User>> = _profileState.asStateFlow()

    // État de la mise à jour du profil
    private val _updateProfileState = MutableStateFlow<NetworkResult<User>>(NetworkResult.Idle)
    val updateProfileState: StateFlow<NetworkResult<User>> = _updateProfileState.asStateFlow()

    /**
     * Charger le profil utilisateur depuis l'API
     */
    fun loadUserProfile() {
        _profileState.value = NetworkResult.Loading

        viewModelScope.launch {
            val result = userRepository.getUserProfile()
            _profileState.value = result
        }
    }

    /**
     * Mettre à jour le profil utilisateur
     *
     * @param firstName Prénom
     * @param lastName Nom de famille
     * @param phoneNumber Numéro de téléphone
     * @param dateOfBirth Date de naissance (format yyyy-MM-dd)
     * @param location Localisation
     * @param avatarUrl URL de l'avatar
     */
    fun updateUserProfile(
        firstName: String? = null,
        lastName: String? = null,
        phoneNumber: String? = null,
        dateOfBirth: String? = null,
        location: String? = null,
        avatarUrl: String? = null
    ) {
        _updateProfileState.value = NetworkResult.Loading

        viewModelScope.launch {
            val request = com.example.myapplicationv10.model.UpdateProfileRequest(
                firstName = firstName,
                lastName = lastName,
                phoneNumber = phoneNumber,
                dateOfBirth = dateOfBirth,
                location = location,
                avatarUrl = avatarUrl
            )

            val result = userRepository.updateUserProfile(request)
            _updateProfileState.value = result

            // Si succès, recharger le profil
            if (result is NetworkResult.Success) {
                _profileState.value = result
            }
        }
    }

    /**
     * Réinitialiser l'état
     */
    fun resetState() {
        _profileState.value = NetworkResult.Idle
        _updateProfileState.value = NetworkResult.Idle
    }
}
