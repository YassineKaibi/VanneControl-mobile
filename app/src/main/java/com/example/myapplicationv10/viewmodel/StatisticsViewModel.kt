package com.example.myapplicationv10.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplicationv10.model.Device
import com.example.myapplicationv10.model.DeviceStatsResponse
import com.example.myapplicationv10.model.TelemetryEvent
import com.example.myapplicationv10.network.NetworkResult
import com.example.myapplicationv10.repository.DeviceRepository
import com.example.myapplicationv10.repository.TelemetryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class StatisticsViewModel(application: Application) : AndroidViewModel(application) {

    private val deviceRepository = DeviceRepository(application)
    private val telemetryRepository = TelemetryRepository(application)

    private val _devicesState = MutableStateFlow<NetworkResult<List<Device>>>(NetworkResult.Idle)
    val devicesState: StateFlow<NetworkResult<List<Device>>> = _devicesState

    private val _statsState = MutableStateFlow<NetworkResult<DeviceStatsResponse>>(NetworkResult.Idle)
    val statsState: StateFlow<NetworkResult<DeviceStatsResponse>> = _statsState

    private val _historyState = MutableStateFlow<NetworkResult<List<TelemetryEvent>>>(NetworkResult.Idle)
    val historyState: StateFlow<NetworkResult<List<TelemetryEvent>>> = _historyState

    fun loadDevices() {
        viewModelScope.launch {
            _devicesState.value = NetworkResult.Loading
            _devicesState.value = deviceRepository.getDevices()
        }
    }

    fun loadDeviceStats(deviceId: String) {
        viewModelScope.launch {
            _statsState.value = NetworkResult.Loading
            _statsState.value = telemetryRepository.getDeviceStats(deviceId)
        }
    }

    fun loadHistory(
        deviceId: String? = null,
        pistonNumber: Int? = null,
        action: String? = null,
        startDate: String? = null,
        endDate: String? = null,
        limit: Int? = 5000
    ) {
        viewModelScope.launch {
            _historyState.value = NetworkResult.Loading
            _historyState.value = telemetryRepository.getTelemetry(
                deviceId, pistonNumber, action, startDate, endDate, limit
            )
        }
    }

    fun refresh() {
        loadDevices()
    }
}