package com.example.myapplicationv10

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.myapplicationv10.databinding.ActivityAddTimingBinding
import com.example.myapplicationv10.model.CreateScheduleRequest
import com.example.myapplicationv10.model.UpdateScheduleRequest
import com.example.myapplicationv10.network.NetworkResult
import com.example.myapplicationv10.viewmodel.ScheduleViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * AddTimingActivity - Screen for creating or editing scheduled valve operations
 *
 * Features:
 * - Schedule name input
 * - Valve selection
 * - Repeat type selection (once, everyday, weekdays, weekends, custom)
 * - Date picker for "Once" option
 * - Timed ON toggle with time picker
 * - Timed OFF toggle with time picker
 */
class AddTimingActivity : BaseActivity() {

    private lateinit var binding: ActivityAddTimingBinding
    private lateinit var viewModel: ScheduleViewModel

    // Intent extras
    private var deviceId: String? = null
    private var deviceName: String? = null
    private var isEditMode: Boolean = false
    private var scheduleId: String? = null

    // Selected values
    private var selectedValveNumber: Int = 1
    private var selectedRepeatType: RepeatType = RepeatType.EVERYDAY
    private var selectedCustomDays: MutableSet<Int> = mutableSetOf()
    private var onTimeHour: Int = 0
    private var onTimeMinute: Int = 0
    private var offTimeHour: Int = 0
    private var offTimeMinute: Int = 0

    // Date for "Once" option
    private var selectedDate: Calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    enum class RepeatType {
        ONCE, EVERYDAY, WEEKDAYS, WEEKENDS, CUSTOM
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddTimingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get intent extras
        deviceId = intent.getStringExtra("DEVICE_ID")
        deviceName = intent.getStringExtra("DEVICE_NAME")
        isEditMode = intent.getBooleanExtra("IS_EDIT_MODE", false)
        scheduleId = intent.getStringExtra("SCHEDULE_ID")

        // Initialize selected date to tomorrow by default
        selectedDate.add(Calendar.DAY_OF_MONTH, 1)

        setupViewModel()
        setupUI()
        setupValveSpinner()
        setupToggles()
        setupTimePickers()
        setupRepeatSelector()
        setupButtons()
        observeViewModel()

        // Load existing data if editing
        if (isEditMode) {
            loadExistingSchedule()
        }
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(
            this,
            ScheduleViewModel.Factory(applicationContext)
        )[ScheduleViewModel::class.java]
    }

    private fun setupUI() {
        binding.tvTitle.text = if (isEditMode) "Edit Timing" else "Add Timing"
    }

    private fun setupValveSpinner() {
        val sharedPreferences = getSharedPreferences("VanneControl", Context.MODE_PRIVATE)
        val numberOfValves = sharedPreferences.getInt("totalValves", 8)

        val valveOptions = (1..numberOfValves).map { "Valve $it" }
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            valveOptions
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        binding.spinnerValve.adapter = adapter
    }

    private fun setupToggles() {
        // Timed On toggle
        binding.switchTimedOn.setOnCheckedChangeListener { _, isChecked ->
            binding.layoutOnTime.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        // Timed Off toggle
        binding.switchTimedOff.setOnCheckedChangeListener { _, isChecked ->
            binding.layoutOffTime.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        // Initial state
        binding.layoutOnTime.visibility = if (binding.switchTimedOn.isChecked) View.VISIBLE else View.GONE
        binding.layoutOffTime.visibility = if (binding.switchTimedOff.isChecked) View.VISIBLE else View.GONE
    }

    private fun setupTimePickers() {
        binding.tvOnTime.setOnClickListener {
            showTimePicker(onTimeHour, onTimeMinute) { hour, minute ->
                onTimeHour = hour
                onTimeMinute = minute
                binding.tvOnTime.text = formatTime(hour, minute)
            }
        }

        binding.tvOffTime.setOnClickListener {
            showTimePicker(offTimeHour, offTimeMinute) { hour, minute ->
                offTimeHour = hour
                offTimeMinute = minute
                binding.tvOffTime.text = formatTime(hour, minute)
            }
        }
    }

    private fun showTimePicker(initialHour: Int, initialMinute: Int, onTimeSet: (Int, Int) -> Unit) {
        TimePickerDialog(
            this,
            { _, hourOfDay, minute ->
                onTimeSet(hourOfDay, minute)
            },
            initialHour,
            initialMinute,
            true // 24-hour format
        ).show()
    }

    private fun formatTime(hour: Int, minute: Int): String {
        return String.format("%02d:%02d:00", hour, minute)
    }

    private fun setupRepeatSelector() {
        binding.tvRepeatValue.text = getRepeatDisplayName(selectedRepeatType)

        // Make the entire repeat row clickable
        binding.tvRepeatValue.rootView.findViewById<View>(R.id.tvRepeatValue).parent.let { parent ->
            (parent as? View)?.setOnClickListener {
                showRepeatDialog()
            }
        }

        binding.tvRepeatValue.setOnClickListener {
            showRepeatDialog()
        }
    }

    private fun showRepeatDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_repeat_selection, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("OK") { _, _ ->
                // If "Once" is selected, show date picker
                if (selectedRepeatType == RepeatType.ONCE) {
                    showDatePickerForOnce()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()

        val radioOnce = dialogView.findViewById<RadioButton>(R.id.radioOnce)
        val radioEveryday = dialogView.findViewById<RadioButton>(R.id.radioEveryday)
        val radioWeekdays = dialogView.findViewById<RadioButton>(R.id.radioWeekdays)
        val radioWeekends = dialogView.findViewById<RadioButton>(R.id.radioWeekends)
        val radioCustom = dialogView.findViewById<RadioButton>(R.id.radioCustom)
        val layoutCustomDays = dialogView.findViewById<View>(R.id.layoutCustomDays)

        // Set initial selection
        when (selectedRepeatType) {
            RepeatType.ONCE -> radioOnce.isChecked = true
            RepeatType.EVERYDAY -> radioEveryday.isChecked = true
            RepeatType.WEEKDAYS -> radioWeekdays.isChecked = true
            RepeatType.WEEKENDS -> radioWeekends.isChecked = true
            RepeatType.CUSTOM -> {
                radioCustom.isChecked = true
                layoutCustomDays.visibility = View.VISIBLE
            }
        }

        // Setup custom days checkboxes
        val dayCheckboxes = listOf(
            dialogView.findViewById<CheckBox>(R.id.cbMon) to 2,
            dialogView.findViewById<CheckBox>(R.id.cbTue) to 3,
            dialogView.findViewById<CheckBox>(R.id.cbWed) to 4,
            dialogView.findViewById<CheckBox>(R.id.cbThu) to 5,
            dialogView.findViewById<CheckBox>(R.id.cbFri) to 6,
            dialogView.findViewById<CheckBox>(R.id.cbSat) to 7,
            dialogView.findViewById<CheckBox>(R.id.cbSun) to 1
        )

        dayCheckboxes.forEach { (checkbox, day) ->
            checkbox.isChecked = selectedCustomDays.contains(day)
            checkbox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) selectedCustomDays.add(day)
                else selectedCustomDays.remove(day)
            }
        }

        // Radio button listeners
        radioOnce.setOnClickListener {
            selectedRepeatType = RepeatType.ONCE
            layoutCustomDays.visibility = View.GONE
            binding.tvRepeatValue.text = getRepeatDisplayName(selectedRepeatType)
        }
        radioEveryday.setOnClickListener {
            selectedRepeatType = RepeatType.EVERYDAY
            layoutCustomDays.visibility = View.GONE
            binding.tvRepeatValue.text = getRepeatDisplayName(selectedRepeatType)
        }
        radioWeekdays.setOnClickListener {
            selectedRepeatType = RepeatType.WEEKDAYS
            layoutCustomDays.visibility = View.GONE
            binding.tvRepeatValue.text = getRepeatDisplayName(selectedRepeatType)
        }
        radioWeekends.setOnClickListener {
            selectedRepeatType = RepeatType.WEEKENDS
            layoutCustomDays.visibility = View.GONE
            binding.tvRepeatValue.text = getRepeatDisplayName(selectedRepeatType)
        }
        radioCustom.setOnClickListener {
            selectedRepeatType = RepeatType.CUSTOM
            layoutCustomDays.visibility = View.VISIBLE
            binding.tvRepeatValue.text = getRepeatDisplayName(selectedRepeatType)
        }

        dialog.show()
    }

    /**
     * Show date picker when "Once" is selected
     */
    private fun showDatePickerForOnce() {
        val today = Calendar.getInstance()

        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                selectedDate.set(Calendar.YEAR, year)
                selectedDate.set(Calendar.MONTH, month)
                selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                // Update the display to show the selected date
                binding.tvRepeatValue.text = "Once (${dateFormat.format(selectedDate.time)})"
            },
            selectedDate.get(Calendar.YEAR),
            selectedDate.get(Calendar.MONTH),
            selectedDate.get(Calendar.DAY_OF_MONTH)
        )

        // Don't allow past dates
        datePickerDialog.datePicker.minDate = today.timeInMillis
        datePickerDialog.show()
    }

    private fun getRepeatDisplayName(type: RepeatType): String {
        return when (type) {
            RepeatType.ONCE -> {
                // Show date if already selected
                if (selectedRepeatType == RepeatType.ONCE) {
                    "Once (${dateFormat.format(selectedDate.time)})"
                } else {
                    "Once"
                }
            }
            RepeatType.EVERYDAY -> "Everyday"
            RepeatType.WEEKDAYS -> "Weekdays"
            RepeatType.WEEKENDS -> "Weekends"
            RepeatType.CUSTOM -> "Custom"
        }
    }

    private fun setupButtons() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnCancel.setOnClickListener {
            finish()
        }

        binding.btnSave.setOnClickListener {
            saveSchedule()
        }
    }

    private fun loadExistingSchedule() {
        val name = intent.getStringExtra("SCHEDULE_NAME") ?: ""
        val pistonNumber = intent.getIntExtra("PISTON_NUMBER", 1)
        val action = intent.getStringExtra("ACTION") ?: "ACTIVATE"
        val cronExpression = intent.getStringExtra("CRON_EXPRESSION") ?: ""

        binding.etScheduleName.setText(name)
        binding.spinnerValve.setSelection(pistonNumber - 1)
        selectedValveNumber = pistonNumber

        // Parse cron expression to set times and repeat
        parseCronExpression(cronExpression, action)
    }

    private fun parseCronExpression(cron: String, action: String) {
        try {
            val parts = cron.split(" ")
            val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
            val hour = parts.getOrNull(2)?.toIntOrNull() ?: 0
            val dayOfMonth = parts.getOrNull(3) ?: "?"
            val month = parts.getOrNull(4) ?: "*"
            val dayOfWeek = parts.getOrNull(5) ?: "*"

            // Set time based on action
            if (action == "ACTIVATE") {
                binding.switchTimedOn.isChecked = true
                binding.switchTimedOff.isChecked = false
                onTimeHour = hour
                onTimeMinute = minute
                binding.tvOnTime.text = formatTime(hour, minute)
            } else {
                binding.switchTimedOn.isChecked = false
                binding.switchTimedOff.isChecked = true
                offTimeHour = hour
                offTimeMinute = minute
                binding.tvOffTime.text = formatTime(hour, minute)
            }

            // Parse repeat type
            selectedRepeatType = when {
                dayOfWeek == "*" && dayOfMonth == "?" -> RepeatType.EVERYDAY
                dayOfWeek == "?" && dayOfMonth != "*" -> {
                    // This is a "Once" schedule - parse the date
                    val day = dayOfMonth.toIntOrNull() ?: 1
                    val mon = month.toIntOrNull() ?: 1
                    selectedDate.set(Calendar.DAY_OF_MONTH, day)
                    selectedDate.set(Calendar.MONTH, mon - 1) // Calendar months are 0-based
                    RepeatType.ONCE
                }
                dayOfWeek == "MON-FRI" || dayOfWeek == "2-6" -> RepeatType.WEEKDAYS
                dayOfWeek == "SAT,SUN" || dayOfWeek == "1,7" -> RepeatType.WEEKENDS
                else -> {
                    // Parse custom days
                    selectedCustomDays.clear()
                    dayOfWeek.split(",").forEach { day ->
                        day.trim().toIntOrNull()?.let { selectedCustomDays.add(it) }
                    }
                    RepeatType.CUSTOM
                }
            }
            binding.tvRepeatValue.text = getRepeatDisplayName(selectedRepeatType)

        } catch (e: Exception) {
            // Use defaults if parsing fails
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.createState.collect { result ->
                when (result) {
                    is NetworkResult.Loading -> {
                        binding.btnSave.isEnabled = false
                    }
                    is NetworkResult.Success -> {
                        Toast.makeText(
                            this@AddTimingActivity,
                            "Schedule created successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                        viewModel.resetCreateState()
                        finish()
                    }
                    is NetworkResult.Error -> {
                        binding.btnSave.isEnabled = true
                        Toast.makeText(
                            this@AddTimingActivity,
                            "Error: ${result.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                        viewModel.resetCreateState()
                    }
                    else -> {
                        binding.btnSave.isEnabled = true
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewModel.updateState.collect { result ->
                when (result) {
                    is NetworkResult.Loading -> {
                        binding.btnSave.isEnabled = false
                    }
                    is NetworkResult.Success -> {
                        Toast.makeText(
                            this@AddTimingActivity,
                            "Schedule updated successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                        viewModel.resetUpdateState()
                        finish()
                    }
                    is NetworkResult.Error -> {
                        binding.btnSave.isEnabled = true
                        Toast.makeText(
                            this@AddTimingActivity,
                            "Error: ${result.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                        viewModel.resetUpdateState()
                    }
                    else -> {
                        binding.btnSave.isEnabled = true
                    }
                }
            }
        }
    }

    private fun saveSchedule() {
        val name = binding.etScheduleName.text.toString().trim()
        if (name.isEmpty()) {
            binding.etScheduleName.error = "Please enter a schedule name"
            return
        }

        if (deviceId == null) {
            Toast.makeText(this, "No device selected", Toast.LENGTH_SHORT).show()
            return
        }

        val timedOnEnabled = binding.switchTimedOn.isChecked
        val timedOffEnabled = binding.switchTimedOff.isChecked

        if (!timedOnEnabled && !timedOffEnabled) {
            Toast.makeText(this, "Please enable at least one timer", Toast.LENGTH_SHORT).show()
            return
        }

        // Validate "Once" date is in the future
        if (selectedRepeatType == RepeatType.ONCE) {
            val now = Calendar.getInstance()
            if (timedOnEnabled) {
                selectedDate.set(Calendar.HOUR_OF_DAY, onTimeHour)
                selectedDate.set(Calendar.MINUTE, onTimeMinute)
            } else {
                selectedDate.set(Calendar.HOUR_OF_DAY, offTimeHour)
                selectedDate.set(Calendar.MINUTE, offTimeMinute)
            }

            if (selectedDate.before(now)) {
                Toast.makeText(this, "Please select a future date and time", Toast.LENGTH_SHORT).show()
                return
            }
        }

        selectedValveNumber = binding.spinnerValve.selectedItemPosition + 1

        // Create schedules based on toggles
        // For now, we create one schedule per action (backend supports single action per schedule)
        if (timedOnEnabled) {
            val cronExpression = buildCronExpression(onTimeHour, onTimeMinute)

            if (isEditMode && scheduleId != null) {
                val request = UpdateScheduleRequest(
                    name = name,
                    action = "ACTIVATE",
                    cronExpression = cronExpression
                )
                viewModel.updateSchedule(scheduleId!!, request)
            } else {
                val request = CreateScheduleRequest(
                    name = name,
                    deviceId = deviceId!!,
                    pistonNumber = selectedValveNumber,
                    action = "ACTIVATE",
                    cronExpression = cronExpression,
                    enabled = true
                )
                viewModel.createSchedule(request)
            }
        }

        // If both are enabled, create a second schedule for OFF
        // Note: In edit mode, this creates a new schedule instead of updating
        if (timedOffEnabled && !isEditMode) {
            val offCronExpression = buildCronExpression(offTimeHour, offTimeMinute)
            val offRequest = CreateScheduleRequest(
                name = "$name (Off)",
                deviceId = deviceId!!,
                pistonNumber = selectedValveNumber,
                action = "DEACTIVATE",
                cronExpression = offCronExpression,
                enabled = true
            )
            viewModel.createSchedule(offRequest)
        }
    }

    /**
     * Build cron expression from selected time and repeat type
     * Quartz cron format: second minute hour dayOfMonth month dayOfWeek [year]
     */
    private fun buildCronExpression(hour: Int, minute: Int): String {
        return when (selectedRepeatType) {
            RepeatType.ONCE -> {
                // Use the user-selected date
                val day = selectedDate.get(Calendar.DAY_OF_MONTH)
                val month = selectedDate.get(Calendar.MONTH) + 1 // Calendar months are 0-based
                val year = selectedDate.get(Calendar.YEAR)
                "0 $minute $hour $day $month ? $year"
            }
            RepeatType.EVERYDAY -> {
                "0 $minute $hour ? * *"
            }
            RepeatType.WEEKDAYS -> {
                "0 $minute $hour ? * MON-FRI"
            }
            RepeatType.WEEKENDS -> {
                "0 $minute $hour ? * SAT,SUN"
            }
            RepeatType.CUSTOM -> {
                val days = if (selectedCustomDays.isEmpty()) {
                    "*" // Default to everyday if no days selected
                } else {
                    selectedCustomDays.sorted().joinToString(",")
                }
                "0 $minute $hour ? * $days"
            }
        }
    }
}