package com.example.myapplicationv10

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import java.text.SimpleDateFormat
import java.util.*

class HistoryActivity : AppCompatActivity() {

    // Data class pour une action historique
    data class ValveAction(
        val valveId: Int,
        val valveName: String,
        val action: String,
        val timestamp: Date,
        val user: String,
        val currentState: Boolean
    )

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: HistoryAdapter
    private lateinit var filterButton: ImageView
    private lateinit var filterPanel: CardView
    private lateinit var applyFiltersButton: Button
    private lateinit var clearFiltersButton: Button
    private lateinit var valveChipGroup: ChipGroup
    private lateinit var actionChipGroup: ChipGroup
    private lateinit var startDateButton: Button
    private lateinit var endDateButton: Button
    private lateinit var activeFiltersChipGroup: ChipGroup
    private lateinit var resultsCountText: TextView

    private val fullHistory = mutableListOf<ValveAction>()
    private var filteredHistory = mutableListOf<ValveAction>()

    // Filtres actifs
    private val selectedValves = mutableSetOf<Int>()
    private var selectedAction: String? = null // "Opened", "Closed", ou null
    private var startDate: Calendar? = null
    private var endDate: Calendar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_history)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.topBar)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initializeViews()
        setupBackButton()
        generateSampleHistory()
        setupFilterPanel()
        setupRecyclerView()
        updateResultsCount()
    }

    private fun initializeViews() {
        recyclerView = findViewById(R.id.historyRecyclerView)
        filterButton = findViewById(R.id.filterButton)
        filterPanel = findViewById(R.id.filterPanel)
        applyFiltersButton = findViewById(R.id.applyFiltersButton)
        clearFiltersButton = findViewById(R.id.clearFiltersButton)
        valveChipGroup = findViewById(R.id.valveChipGroup)
        actionChipGroup = findViewById(R.id.actionChipGroup)
        startDateButton = findViewById(R.id.startDateButton)
        endDateButton = findViewById(R.id.endDateButton)
        activeFiltersChipGroup = findViewById(R.id.activeFiltersChipGroup)
        resultsCountText = findViewById(R.id.resultsCountText)
    }

    private fun setupBackButton() {
        findViewById<ImageView>(R.id.backButton).setOnClickListener {
            finish()
        }
    }

    private fun generateSampleHistory() {
        val calendar = Calendar.getInstance()
        val users = listOf("Admin", "Operateur A", "Operateur B", "Système Auto")

        for (i in 1..100) {
            calendar.add(Calendar.HOUR, -i * 2)
            calendar.add(Calendar.MINUTE, -(i * 3))

            if (i % 10 == 0) {
                calendar.add(Calendar.DAY_OF_MONTH, -15)
            }

            if (i % 30 == 0) {
                calendar.add(Calendar.YEAR, -1)
            }

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
        // Bouton pour afficher/masquer le panel de filtrage
        filterButton.setOnClickListener {
            if (filterPanel.visibility == View.VISIBLE) {
                filterPanel.visibility = View.GONE
            } else {
                filterPanel.visibility = View.VISIBLE
            }
        }

        // Configuration des chips de vannes
        setupValveChips()

        // Configuration des chips d'action
        setupActionChips()

        // Configuration des boutons de date
        setupDateButtons()

        // Bouton appliquer les filtres
        applyFiltersButton.setOnClickListener {
            applyFilters()
            filterPanel.visibility = View.GONE
        }

        // Bouton effacer les filtres
        clearFiltersButton.setOnClickListener {
            clearAllFilters()
        }
    }

    private fun setupValveChips() {
        for (i in 1..8) {
            val chip = Chip(this).apply {
                text = "Valve $i"
                isCheckable = true
                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        selectedValves.add(i)
                    } else {
                        selectedValves.remove(i)
                    }
                }
            }
            valveChipGroup.addView(chip)
        }
    }

    private fun setupActionChips() {
        val openChip = findViewById<Chip>(R.id.chipOpened)
        val closeChip = findViewById<Chip>(R.id.chipClosed)

        openChip.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                selectedAction = "Opened"
                closeChip.isChecked = false
            } else if (selectedAction == "Opened") {
                selectedAction = null
            }
        }

        closeChip.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                selectedAction = "Closed"
                openChip.isChecked = false
            } else if (selectedAction == "Closed") {
                selectedAction = null
            }
        }
    }

    private fun setupDateButtons() {
        startDateButton.setOnClickListener {
            showDatePicker { selectedDate ->
                startDate = selectedDate
                updateDateButtonText(startDateButton, selectedDate, "Date de début")
            }
        }

        endDateButton.setOnClickListener {
            showDatePicker { selectedDate ->
                endDate = selectedDate
                updateDateButtonText(endDateButton, selectedDate, "Date de fin")
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

        // Filtre par vannes
        if (selectedValves.isNotEmpty()) {
            result = result.filter { it.valveId in selectedValves }
        }

        // Filtre par action
        if (selectedAction != null) {
            result = result.filter { it.action == selectedAction }
        }

        // Filtre par date de début
        if (startDate != null) {
            result = result.filter {
                val actionCalendar = Calendar.getInstance().apply { time = it.timestamp }
                actionCalendar.timeInMillis >= startDate!!.timeInMillis
            }
        }

        // Filtre par date de fin
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
        activeFiltersChipGroup.removeAllViews()

        // Chips pour les vannes sélectionnées
        selectedValves.forEach { valveId ->
            addActiveFilterChip("Valve $valveId") {
                selectedValves.remove(valveId)
                (valveChipGroup.getChildAt(valveId - 1) as? Chip)?.isChecked = false
                applyFilters()
            }
        }

        // Chip pour l'action
        if (selectedAction != null) {
            val actionText = if (selectedAction == "Opened") "Ouvertures" else "Fermetures"
            addActiveFilterChip(actionText) {
                selectedAction = null
                findViewById<Chip>(R.id.chipOpened).isChecked = false
                findViewById<Chip>(R.id.chipClosed).isChecked = false
                applyFilters()
            }
        }

        // Chip pour la date de début
        if (startDate != null) {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            addActiveFilterChip("Depuis: ${dateFormat.format(startDate!!.time)}") {
                startDate = null
                startDateButton.text = "Date de début"
                applyFilters()
            }
        }

        // Chip pour la date de fin
        if (endDate != null) {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            addActiveFilterChip("Jusqu'à: ${dateFormat.format(endDate!!.time)}") {
                endDate = null
                endDateButton.text = "Date de fin"
                applyFilters()
            }
        }

        // Afficher/masquer la section des filtres actifs
        findViewById<LinearLayout>(R.id.activeFiltersSection).visibility =
            if (activeFiltersChipGroup.childCount > 0) View.VISIBLE else View.GONE
    }

    private fun addActiveFilterChip(text: String, onClose: () -> Unit) {
        val chip = Chip(this).apply {
            this.text = text
            isCloseIconVisible = true
            setOnCloseIconClickListener {
                onClose()
            }
        }
        activeFiltersChipGroup.addView(chip)
    }

    private fun clearAllFilters() {
        selectedValves.clear()
        selectedAction = null
        startDate = null
        endDate = null

        // Réinitialiser l'interface
        for (i in 0 until valveChipGroup.childCount) {
            (valveChipGroup.getChildAt(i) as? Chip)?.isChecked = false
        }
        findViewById<Chip>(R.id.chipOpened).isChecked = false
        findViewById<Chip>(R.id.chipClosed).isChecked = false
        startDateButton.text = "Date de début"
        endDateButton.text = "Date de fin"

        applyFilters()
    }

    private fun updateResultsCount() {
        val count = filteredHistory.size
        val total = fullHistory.size
        resultsCountText.text = "$count résultat(s) sur $total"
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = HistoryAdapter(filteredHistory)
        recyclerView.adapter = adapter
    }
}