package com.example.myapplicationv10

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplicationv10.databinding.ActivityHistoryBinding
import com.google.android.material.chip.Chip
import java.text.SimpleDateFormat
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

        setupBackButton()
        generateSampleHistory()
        setupFilterPanel()
        setupRecyclerView()
        updateResultsCount()
    }

    private fun setupBackButton() {
        binding.backButton.setOnClickListener { finish() }
    }

    private fun generateSampleHistory() {
        val calendar = Calendar.getInstance()
        val users = listOf("Admin", "Operateur A", "Operateur B", "Système Auto")

        for (i in 1..100) {
            calendar.add(Calendar.HOUR, -i * 2)
            calendar.add(Calendar.MINUTE, -(i * 3))
            if (i % 10 == 0) calendar.add(Calendar.DAY_OF_MONTH, -15)
            if (i % 30 == 0) calendar.add(Calendar.YEAR, -1)
            val valveId = (1..8).random()
            val isOpening = i % 2 == 0

            fullHistory.add(
                ValveAction(
                    valveId = valveId,
                    valveName = "Valve $valveId",
                    action = if (isOpening) "Opened" else "Closed",
                    timestamp = calendar.time.clone() as Date,
                    user = users.random(),
                    currentState = isOpening
                )
            )
        }

        fullHistory.sortByDescending { it.timestamp }
        filteredHistory.addAll(fullHistory)
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
        filteredHistory.clear()
        var result = fullHistory.toList()

        if (selectedValves.isNotEmpty()) result = result.filter { it.valveId in selectedValves }
        if (selectedAction != null) result = result.filter { it.action == selectedAction }
        if (startDate != null) result = result.filter {
            val actionCalendar = Calendar.getInstance().apply { time = it.timestamp }
            actionCalendar.timeInMillis >= startDate!!.timeInMillis
        }
        if (endDate != null) {
            val endOfDay = (endDate!!.clone() as Calendar).apply {
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
            }
            result = result.filter {
                val actionCalendar = Calendar.getInstance().apply { time = it.timestamp }
                actionCalendar.timeInMillis <= endOfDay.timeInMillis
            }
        }

        filteredHistory.addAll(result)
        adapter.notifyDataSetChanged()
        updateActiveFiltersChips()
        updateResultsCount()
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

        applyFilters()
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
