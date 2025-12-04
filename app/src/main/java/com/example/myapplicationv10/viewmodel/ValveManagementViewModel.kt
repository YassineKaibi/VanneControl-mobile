package com.example.myapplicationv10.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplicationv10.model.Device
import com.example.myapplicationv10.model.Piston
import com.example.myapplicationv10.network.NetworkResult
import com.example.myapplicationv10.repository.DeviceRepository
import com.example.myapplicationv10.repository.PistonRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ValveManagementViewModel - ViewModel pour l'écran de gestion des vannes
 *
 * Gère l'affichage et le contrôle des 8 pistons d'un appareil
 * Utilise StateFlow pour la réactivité
 */
class ValveManagementViewModel(application: Application) : AndroidViewModel(application) {

    private val deviceRepository = DeviceRepository(application)
    private val pistonRepository = PistonRepository(application)

    // État de l'appareil sélectionné
    private val _deviceState = MutableStateFlow<NetworkResult<Device>>(NetworkResult.Idle)
    val deviceState: StateFlow<NetworkResult<Device>> = _deviceState.asStateFlow()

    // État du contrôle de piston (pour afficher les messages de succès/erreur)
    private val _controlState = MutableStateFlow<NetworkResult<Piston>?>(null)
    val controlState: StateFlow<NetworkResult<Piston>?> = _controlState.asStateFlow()

    // ID de l'appareil actuellement géré
    private var currentDeviceId: String? = null

    /**
     * Charger les détails d'un appareil
     *
     * @param deviceId L'ID de l'appareil à charger
     */
    fun loadDevice(deviceId: String) {
        currentDeviceId = deviceId

        viewModelScope.launch {
            _deviceState.value = NetworkResult.Loading
            val result = deviceRepository.getDevice(deviceId)
            _deviceState.value = result
        }
    }

    /**
     * Rafraîchir les données de l'appareil
     */
    fun refreshDevice() {
        currentDeviceId?.let { deviceId ->
            viewModelScope.launch {
                val result = deviceRepository.refreshDevice(deviceId)
                _deviceState.value = result
            }
        }
    }

    /**
     * Basculer l'état d'un piston (activer/désactiver)
     *
     * @param pistonNumber Le numéro du piston (1-8)
     * @param currentState L'état actuel du piston
     */
    fun togglePiston(pistonNumber: Int, currentState: String) {
        val deviceId = currentDeviceId ?: return

        viewModelScope.launch {
            _controlState.value = NetworkResult.Loading

            val result = pistonRepository.togglePiston(
                deviceId = deviceId,
                pistonNumber = pistonNumber,
                currentState = currentState
            )

            _controlState.value = result

            // Si succès, rafraîchir l'appareil pour mettre à jour l'UI
            if (result is NetworkResult.Success) {
                refreshDevice()
            }
        }
    }

    /**
     * Activer un piston spécifique
     *
     * @param pistonNumber Le numéro du piston (1-8)
     */
    fun activatePiston(pistonNumber: Int) {
        val deviceId = currentDeviceId ?: return

        viewModelScope.launch {
            _controlState.value = NetworkResult.Loading

            val result = pistonRepository.activatePiston(deviceId, pistonNumber)
            _controlState.value = result

            // Si succès, rafraîchir l'appareil
            if (result is NetworkResult.Success) {
                refreshDevice()
            }
        }
    }

    /**
     * Désactiver un piston spécifique
     *
     * @param pistonNumber Le numéro du piston (1-8)
     */
    fun deactivatePiston(pistonNumber: Int) {
        val deviceId = currentDeviceId ?: return

        viewModelScope.launch {
            _controlState.value = NetworkResult.Loading

            val result = pistonRepository.deactivatePiston(deviceId, pistonNumber)
            _controlState.value = result

            // Si succès, rafraîchir l'appareil
            if (result is NetworkResult.Success) {
                refreshDevice()
            }
        }
    }

    /**
     * Activer tous les pistons d'un coup
     */
    fun activateAllPistons() {
        val deviceId = currentDeviceId ?: return
        val device = (_deviceState.value as? NetworkResult.Success)?.data ?: return

        viewModelScope.launch {
            device.pistons.forEach { piston ->
                if (piston.state != "active") {
                    pistonRepository.activatePiston(deviceId, piston.pistonNumber)
                }
            }
            refreshDevice()
        }
    }

    /**
     * Désactiver tous les pistons d'un coup
     */
    fun deactivateAllPistons() {
        val deviceId = currentDeviceId ?: return
        val device = (_deviceState.value as? NetworkResult.Success)?.data ?: return

        viewModelScope.launch {
            device.pistons.forEach { piston ->
                if (piston.state == "active") {
                    pistonRepository.deactivatePiston(deviceId, piston.pistonNumber)
                }
            }
            refreshDevice()
        }
    }

    /**
     * Réinitialiser l'état du contrôle
     * (efface les messages de succès/erreur)
     */
    fun resetControlState() {
        _controlState.value = null
    }

    /**
     * Obtenir un piston spécifique par son numéro
     */
    fun getPiston(pistonNumber: Int): Piston? {
        val device = (_deviceState.value as? NetworkResult.Success)?.data
        return device?.pistons?.find { it.pistonNumber == pistonNumber }
    }
}
