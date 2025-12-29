package com.example.myapplicationv10

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplicationv10.adapter.ScheduleAdapter
import com.example.myapplicationv10.databinding.ActivityTimingPlanBinding
import com.example.myapplicationv10.model.ScheduleResponse
import com.example.myapplicationv10.network.NetworkResult
import com.example.myapplicationv10.viewmodel.ScheduleViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

/**
 * TimingPlanActivity - Main screen for managing scheduled valve operations
 *
 * Displays list of existing schedules with ability to:
 * - View all schedules
 * - Toggle schedule enabled/disabled
 * - Edit a schedule
 * - Delete a schedule
 * - Add new schedule
 */
class TimingPlanActivity : BaseActivity() {

    private lateinit var binding: ActivityTimingPlanBinding
    private lateinit var viewModel: ScheduleViewModel
    private lateinit var scheduleAdapter: ScheduleAdapter

    private var deviceId: String? = null
    private var deviceName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTimingPlanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get device info from intent
        deviceId = intent.getStringExtra("DEVICE_ID")
        deviceName = intent.getStringExtra("DEVICE_NAME")

        setupViewModel()
        setupRecyclerView()
        setupButtons()
        observeViewModel()

        // Load schedules
        viewModel.loadSchedules()
    }

    override fun onResume() {
        super.onResume()
        // Refresh schedules when returning from AddTimingActivity
        viewModel.loadSchedules()
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(
            this,
            ScheduleViewModel.Factory(applicationContext)
        )[ScheduleViewModel::class.java]
    }

    private fun setupRecyclerView() {
        scheduleAdapter = ScheduleAdapter(
            onToggle = { schedule, enabled ->
                viewModel.toggleSchedule(schedule.id, enabled)
            },
            onEdit = { schedule ->
                navigateToEditSchedule(schedule)
            },
            onDelete = { schedule ->
                showDeleteConfirmation(schedule)
            }
        )

        binding.schedulesRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@TimingPlanActivity)
            adapter = scheduleAdapter
        }
    }

    private fun setupButtons() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnAdd.setOnClickListener {
            navigateToAddSchedule()
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.schedulesState.collect { result ->
                when (result) {
                    is NetworkResult.Idle -> {
                        binding.progressBar.visibility = View.GONE
                    }
                    is NetworkResult.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.emptyState.visibility = View.GONE
                    }
                    is NetworkResult.Success -> {
                        binding.progressBar.visibility = View.GONE
                        val schedules = result.data

                        if (schedules.isEmpty()) {
                            binding.emptyState.visibility = View.VISIBLE
                            binding.schedulesRecyclerView.visibility = View.GONE
                        } else {
                            binding.emptyState.visibility = View.GONE
                            binding.schedulesRecyclerView.visibility = View.VISIBLE

                            // Filter by device if deviceId is provided
                            val filteredSchedules = if (deviceId != null) {
                                schedules.filter { it.deviceId == deviceId }
                            } else {
                                schedules
                            }

                            scheduleAdapter.submitList(filteredSchedules)
                        }
                    }
                    is NetworkResult.Error -> {
                        binding.progressBar.visibility = View.GONE
                        binding.emptyState.visibility = View.VISIBLE
                        Toast.makeText(
                            this@TimingPlanActivity,
                            "Error: ${result.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewModel.deleteState.collect { result ->
                when (result) {
                    is NetworkResult.Success -> {
                        Toast.makeText(
                            this@TimingPlanActivity,
                            "Schedule deleted",
                            Toast.LENGTH_SHORT
                        ).show()
                        viewModel.resetDeleteState()
                    }
                    is NetworkResult.Error -> {
                        Toast.makeText(
                            this@TimingPlanActivity,
                            "Failed to delete: ${result.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                        viewModel.resetDeleteState()
                    }
                    else -> {}
                }
            }
        }

        lifecycleScope.launch {
            viewModel.updateState.collect { result ->
                when (result) {
                    is NetworkResult.Error -> {
                        Toast.makeText(
                            this@TimingPlanActivity,
                            "Failed to update: ${result.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                        viewModel.resetUpdateState()
                        // Refresh to restore original state
                        viewModel.loadSchedules()
                    }
                    else -> {}
                }
            }
        }
    }

    private fun navigateToAddSchedule() {
        val intent = Intent(this, AddTimingActivity::class.java).apply {
            putExtra("DEVICE_ID", deviceId)
            putExtra("DEVICE_NAME", deviceName)
        }
        startActivity(intent)
    }

    private fun navigateToEditSchedule(schedule: ScheduleResponse) {
        val intent = Intent(this, AddTimingActivity::class.java).apply {
            putExtra("DEVICE_ID", deviceId ?: schedule.deviceId)
            putExtra("DEVICE_NAME", deviceName)
            putExtra("SCHEDULE_ID", schedule.id)
            putExtra("SCHEDULE_NAME", schedule.name)
            putExtra("PISTON_NUMBER", schedule.pistonNumber)
            putExtra("ACTION", schedule.action)
            putExtra("CRON_EXPRESSION", schedule.cronExpression)
            putExtra("ENABLED", schedule.enabled)
            putExtra("IS_EDIT_MODE", true)
        }
        startActivity(intent)
    }

    private fun showDeleteConfirmation(schedule: ScheduleResponse) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Delete Schedule")
            .setMessage("Are you sure you want to delete \"${schedule.name}\"?")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteSchedule(schedule.id)
            }
            .show()
    }
}