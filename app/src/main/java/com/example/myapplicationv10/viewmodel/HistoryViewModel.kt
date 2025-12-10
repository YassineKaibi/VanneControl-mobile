package com.example.myapplicationv10.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplicationv10.model.TelemetryEvent
import com.example.myapplicationv10.network.NetworkResult
import com.example.myapplicationv10.repository.TelemetryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * HistoryViewModel - ViewModel pour l'activité de l'historique
 */
class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val telemetryRepository = TelemetryRepository(application)

    private val _historyState = MutableStateFlow<NetworkResult<List<TelemetryEvent>>>(NetworkResult.Idle)
    val historyState: StateFlow<NetworkResult<List<TelemetryEvent>>> = _historyState

    /**
     * Charger l'historique des événements avec filtres optionnels
     *
     * @param deviceId Filtrer par appareil
     * @param pistonNumber Filtrer par piston
     * @param action Filtrer par action ("activated" ou "deactivated")
     * @param startDate Date de début (ISO format)
     * @param endDate Date de fin (ISO format)
     * @param limit Nombre maximum de résultats
     */
    fun loadHistory(
        deviceId: String? = null,
        pistonNumber: Int? = null,
        action: String? = null,
        startDate: String? = null,
        endDate: String? = null,
        limit: Int? = 1000
    ) {
        viewModelScope.launch {
            _historyState.value = NetworkResult.Loading
            _historyState.value = telemetryRepository.getTelemetry(
                deviceId, pistonNumber, action, startDate, endDate, limit
            )
        }
    }

    /**
     * Rafraîchir l'historique
     */
    fun refresh() {
        loadHistory()
    }
}
