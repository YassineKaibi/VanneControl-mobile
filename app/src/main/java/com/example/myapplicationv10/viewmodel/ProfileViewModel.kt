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
     * Réinitialiser l'état
     */
    fun resetState() {
        _profileState.value = NetworkResult.Idle
    }
}
