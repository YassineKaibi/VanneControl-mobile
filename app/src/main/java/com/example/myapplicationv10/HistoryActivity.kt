package com.example.myapplicationv10

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplicationv10.databinding.ActivityHistoryBinding
import com.example.myapplicationv10.model.TelemetryEvent
import com.example.myapplicationv10.network.NetworkResult
import com.example.myapplicationv10.viewmodel.HistoryViewModel
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

class HistoryActivity : BaseActivity() {


    data class ValveAction(
        val valveId: Int,
        val valveName: String,
        val action: String,
        val timestamp: Date,
        val user: String,
        val currentState: Boolean
    )

    private lateinit var binding: ActivityHistoryBinding
    private lateinit var adapter: HistoryAdapter
    private lateinit var viewModel: HistoryViewModel

    private val fullHistory = mutableListOf<ValveAction>()
    private var filteredHistory = mutableListOf<ValveAction>()

    private val selectedValves = mutableSetOf<Int>()
    private var selectedAction: String? = null
    private var startDate: Calendar? = null
    private var endDate: Calendar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.topBar) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize ViewModel
        viewModel = ViewModelProvider(this)[HistoryViewModel::class.java]

        setupBackButton()
        setupFilterPanel()
        setupRecyclerView()

        // Observe history data
        observeViewModel()

        // Load history from backend
        viewModel.loadHistory()
    }

    private fun setupBackButton() {
        binding.backButton.setOnClickListener { finish() }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.historyState.collect { result ->
                when (result) {
                    is NetworkResult.Success -> {
                        loadHistoryFromTelemetry(result.data)
                    }
                    is NetworkResult.Error -> {
                        Snackbar.make(binding.root, result.message, Snackbar.LENGTH_LONG).show()
                    }
                    is NetworkResult.Loading -> {
                        // Show loading state if needed
                    }
                    is NetworkResult.Idle -> {
                        // Initial state
                    }
                }
            }
        }
    }

    private fun loadHistoryFromTelemetry(telemetryEvents: List<TelemetryEvent>) {
        fullHistory.clear()
        val gson = Gson()

        for (event in telemetryEvents) {
            try {
                // Parse payload to extract piston number
                val payload = event.payload?.let { gson.fromJson(it, Map::class.java) as? Map<String, Any> }
                val pistonNumber = (payload?.get("piston_number") as? Double)?.toInt() ?: 1

                // Parse timestamp
                val instant = Instant.parse(event.createdAt)
                val date = Date.from(instant)

                // Map event type to action
                val action = when (event.eventType) {
                    "activated" -> "Opened"
                    "deactivated" -> "Closed"
                    else -> event.eventType
                }

                fullHistory.add(
                    ValveAction(
                        valveId = pistonNumber,
                        valveName = "Valve $pistonNumber",
                        action = action,
                        timestamp = date,
                        user = "System", // Backend doesn't track user in telemetry
                        currentState = event.eventType == "activated"
                    )
                )
            } catch (e: Exception) {
                // Skip malformed events
                e.printStackTrace()
            }
        }

        fullHistory.sortByDescending { it.timestamp }
        filteredHistory.clear()
        filteredHistory.addAll(fullHistory)
        adapter.notifyDataSetChanged()
        updateResultsCount()
    }

    private fun setupFilterPanel() {
        binding.filterButton.setOnClickListener {
            binding.filterPanel.visibility =
                if (binding.filterPanel.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }

        setupValveChips()
        setupActionChips()
        setupDateButtons()

        binding.applyFiltersButton.setOnClickListener {
            applyFilters()
            binding.filterPanel.visibility = View.GONE
        }

        binding.clearFiltersButton.setOnClickListener { clearAllFilters() }
    }

    private fun setupValveChips() {
        for (i in 1..8) {
            val chip = Chip(this).apply {
                text = "Valve $i"
                isCheckable = true
                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) selectedValves.add(i) else selectedValves.remove(i)
                }
            }
            binding.valveChipGroup.addView(chip)
        }
    }

    private fun setupActionChips() {
        val openChip = binding.chipOpened
        val closeChip = binding.chipClosed

        openChip.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                selectedAction = "Opened"
                closeChip.isChecked = false
            } else if (selectedAction == "Opened") selectedAction = null
        }

        closeChip.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                selectedAction = "Closed"
                openChip.isChecked = false
            } else if (selectedAction == "Closed") selectedAction = null
        }
    }

    private fun setupDateButtons() {
        binding.startDateButton.setOnClickListener {
            showDatePicker { selectedDate ->
                startDate = selectedDate
                updateDateButtonText(binding.startDateButton, selectedDate, "Date de début")
            }
        }

        binding.endDateButton.setOnClickListener {
            showDatePicker { selectedDate ->
                endDate = selectedDate
                updateDateButtonText(binding.endDateButton, selectedDate, "Date de fin")
            }
        }
    }

    private fun showDatePicker(onDateSelected: (Calendar) -> Unit) {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val selectedCalendar = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                onDateSelected(selectedCalendar)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun updateDateButtonText(button: Button, date: Calendar, defaultText: String) {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        button.text = dateFormat.format(date.time)
    }

    private fun applyFilters() {
        // Prepare filter parameters for backend
        val pistonNumber = if (selectedValves.size == 1) selectedValves.first() else null
        val action = when (selectedAction) {
            "Opened" -> "activated"
            "Closed" -> "deactivated"
            else -> null
        }

        // Convert dates to ISO format for backend
        val startDateIso = startDate?.let {
            Instant.ofEpochMilli(it.timeInMillis).toString()
        }
        val endDateIso = endDate?.let {
            val endOfDay = it.clone() as Calendar
            endOfDay.set(Calendar.HOUR_OF_DAY, 23)
            endOfDay.set(Calendar.MINUTE, 59)
            endOfDay.set(Calendar.SECOND, 59)
            Instant.ofEpochMilli(endOfDay.timeInMillis).toString()
        }

        // Load filtered data from backend
        viewModel.loadHistory(
            deviceId = null,
            pistonNumber = pistonNumber,
            action = action,
            startDate = startDateIso,
            endDate = endDateIso,
            limit = 1000
        )

        updateActiveFiltersChips()
    }

    private fun updateActiveFiltersChips() {
        binding.activeFiltersChipGroup.removeAllViews()

        selectedValves.forEach { valveId ->
            addActiveFilterChip("Valve $valveId") {
                selectedValves.remove(valveId)
                (binding.valveChipGroup.getChildAt(valveId - 1) as? Chip)?.isChecked = false
                applyFilters()
            }
        }

        if (selectedAction != null) {
            val actionText = if (selectedAction == "Opened") "Ouvertures" else "Fermetures"
            addActiveFilterChip(actionText) {
                selectedAction = null
                binding.chipOpened.isChecked = false
                binding.chipClosed.isChecked = false
                applyFilters()
            }
        }

        if (startDate != null) {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            addActiveFilterChip("Depuis: ${dateFormat.format(startDate!!.time)}") {
                startDate = null
                binding.startDateButton.text = "Date de début"
                applyFilters()
            }
        }

        if (endDate != null) {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            addActiveFilterChip("Jusqu'à: ${dateFormat.format(endDate!!.time)}") {
                endDate = null
                binding.endDateButton.text = "Date de fin"
                applyFilters()
            }
        }

        binding.activeFiltersSection.visibility =
            if (binding.activeFiltersChipGroup.childCount > 0) View.VISIBLE else View.GONE
    }

    private fun addActiveFilterChip(text: String, onClose: () -> Unit) {
        val chip = Chip(this).apply {
            this.text = text
            isCloseIconVisible = true
            setOnCloseIconClickListener { onClose() }
        }
        binding.activeFiltersChipGroup.addView(chip)
    }

    private fun clearAllFilters() {
        selectedValves.clear()
        selectedAction = null
        startDate = null
        endDate = null

        for (i in 0 until binding.valveChipGroup.childCount) {
            (binding.valveChipGroup.getChildAt(i) as? Chip)?.isChecked = false
        }

        binding.chipOpened.isChecked = false
        binding.chipClosed.isChecked = false
        binding.startDateButton.text = "Date de début"
        binding.endDateButton.text = "Date de fin"

        // Reload all history from backend without filters
        viewModel.loadHistory()

        binding.activeFiltersChipGroup.removeAllViews()
        binding.activeFiltersSection.visibility = View.GONE
    }

    private fun updateResultsCount() {
        val count = filteredHistory.size
        val total = fullHistory.size
        binding.resultsCountText.text = "$count résultat(s) sur $total"
    }

    private fun setupRecyclerView() {
        binding.historyRecyclerView.layoutManager = LinearLayoutManager(this)
        adapter = HistoryAdapter(filteredHistory)
        binding.historyRecyclerView.adapter = adapter
    }


}
