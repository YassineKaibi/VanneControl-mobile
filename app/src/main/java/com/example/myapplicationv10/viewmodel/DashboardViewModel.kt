package com.example.myapplicationv10.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplicationv10.model.Device
import com.example.myapplicationv10.network.NetworkResult
import com.example.myapplicationv10.repository.AuthRepository
import com.example.myapplicationv10.repository.DeviceRepository
import com.example.myapplicationv10.utils.ValveLimitManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * DashboardViewModel - ViewModel pour l'écran Dashboard
 *
 * Gère l'affichage de la liste des appareils et des pistons actifs
 * Utilise StateFlow pour la réactivité
 */
class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val deviceRepository = DeviceRepository(application)
    private val authRepository = AuthRepository(application)

    // État de la liste des appareils
    private val _devicesState = MutableStateFlow<NetworkResult<List<Device>>>(NetworkResult.Idle)
    val devicesState: StateFlow<NetworkResult<List<Device>>> = _devicesState.asStateFlow()

    // État du rafraîchissement
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    // Email de l'utilisateur connecté
    private val _userEmail = MutableStateFlow<String?>(null)
    val userEmail: StateFlow<String?> = _userEmail.asStateFlow()

    init {
        // Charger l'email de l'utilisateur
        _userEmail.value = authRepository.getUserEmail()

        // Charger les appareils au démarrage
        loadDevices()
    }

    /**
     * Charger la liste des appareils
     */
    fun loadDevices() {
        viewModelScope.launch {
            _devicesState.value = NetworkResult.Loading
            val result = deviceRepository.getDevices()
            _devicesState.value = result
        }
    }

    /**
     * Rafraîchir la liste des appareils (pull-to-refresh)
     */
    fun refreshDevices() {
        viewModelScope.launch {
            _isRefreshing.value = true
            val result = deviceRepository.getDevices()
            _devicesState.value = result
            _isRefreshing.value = false
        }
    }

    /**
     * Obtenir les pistons actifs de tous les appareils
     * Filtre par état actif ET limite de valve configurée
     */
    fun getActivePistons(): List<Pair<Device, com.example.myapplicationv10.model.Piston>> {
        val currentState = _devicesState.value
        if (currentState is NetworkResult.Success) {
            val devices = currentState.data
            val activePistons = mutableListOf<Pair<Device, com.example.myapplicationv10.model.Piston>>()
            val valveLimitManager = ValveLimitManager.getInstance(getApplication())

            devices.forEach { device ->
                device.pistons
                    .filter { piston ->
                        // Must be active AND within enabled valve limit
                        piston.state == "active" &&
                        valveLimitManager.isValveEnabled(piston.pistonNumber)
                    }
                    .forEach { piston ->
                        activePistons.add(Pair(device, piston))
                    }
            }

            return activePistons
        }
        return emptyList()
    }

    /**
     * Obtenir le nombre total de pistons actifs
     */
    fun getActivePistonCount(): Int {
        return getActivePistons().size
    }

    /**
     * Déconnexion de l'utilisateur
     */
    fun logout() {
        authRepository.logout()
    }
}
