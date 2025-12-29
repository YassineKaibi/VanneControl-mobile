package com.example.myapplicationv10.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplicationv10.model.CreateScheduleRequest
import com.example.myapplicationv10.model.ScheduleResponse
import com.example.myapplicationv10.model.UpdateScheduleRequest
import com.example.myapplicationv10.network.NetworkResult
import com.example.myapplicationv10.repository.ScheduleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ScheduleViewModel - Manages schedule data and operations
 */
class ScheduleViewModel(private val repository: ScheduleRepository) : ViewModel() {

    private val _schedulesState = MutableStateFlow<NetworkResult<List<ScheduleResponse>>>(NetworkResult.Idle)
    val schedulesState: StateFlow<NetworkResult<List<ScheduleResponse>>> = _schedulesState.asStateFlow()

    private val _createState = MutableStateFlow<NetworkResult<ScheduleResponse>>(NetworkResult.Idle)
    val createState: StateFlow<NetworkResult<ScheduleResponse>> = _createState.asStateFlow()

    private val _updateState = MutableStateFlow<NetworkResult<ScheduleResponse>>(NetworkResult.Idle)
    val updateState: StateFlow<NetworkResult<ScheduleResponse>> = _updateState.asStateFlow()

    private val _deleteState = MutableStateFlow<NetworkResult<Boolean>>(NetworkResult.Idle)
    val deleteState: StateFlow<NetworkResult<Boolean>> = _deleteState.asStateFlow()

    /**
     * Load all schedules for the current user
     */
    fun loadSchedules() {
        viewModelScope.launch {
            _schedulesState.value = NetworkResult.Loading
            _schedulesState.value = repository.getSchedules()
        }
    }

    /**
     * Create a new schedule
     */
    fun createSchedule(request: CreateScheduleRequest) {
        viewModelScope.launch {
            _createState.value = NetworkResult.Loading
            _createState.value = repository.createSchedule(request)
        }
    }

    /**
     * Update an existing schedule
     */
    fun updateSchedule(scheduleId: String, request: UpdateScheduleRequest) {
        viewModelScope.launch {
            _updateState.value = NetworkResult.Loading
            _updateState.value = repository.updateSchedule(scheduleId, request)
        }
    }

    /**
     * Toggle schedule enabled/disabled
     */
    fun toggleSchedule(scheduleId: String, enabled: Boolean) {
        viewModelScope.launch {
            _updateState.value = NetworkResult.Loading
            val result = repository.toggleSchedule(scheduleId, enabled)
            _updateState.value = result
            
            // Refresh the list after toggling
            if (result is NetworkResult.Success) {
                loadSchedules()
            }
        }
    }

    /**
     * Delete a schedule
     */
    fun deleteSchedule(scheduleId: String) {
        viewModelScope.launch {
            _deleteState.value = NetworkResult.Loading
            val result = repository.deleteSchedule(scheduleId)
            _deleteState.value = when (result) {
                is NetworkResult.Success -> NetworkResult.Success(true)
                is NetworkResult.Error -> NetworkResult.Error(result.message, result.code)
                is NetworkResult.Loading -> NetworkResult.Loading
                is NetworkResult.Idle -> NetworkResult.Idle
            }
            
            // Refresh the list after deletion
            if (result is NetworkResult.Success) {
                loadSchedules()
            }
        }
    }

    /**
     * Reset create state
     */
    fun resetCreateState() {
        _createState.value = NetworkResult.Idle
    }

    /**
     * Reset update state
     */
    fun resetUpdateState() {
        _updateState.value = NetworkResult.Idle
    }

    /**
     * Reset delete state
     */
    fun resetDeleteState() {
        _deleteState.value = NetworkResult.Idle
    }

    /**
     * Factory for creating ScheduleViewModel with repository
     */
    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ScheduleViewModel::class.java)) {
                return ScheduleViewModel(ScheduleRepository(context)) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
