package com.example.myapplicationv10

import android.graphics.Color
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.myapplicationv10.model.Device
import com.example.myapplicationv10.network.NetworkResult
import com.example.myapplicationv10.viewmodel.StatisticsViewModel
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.snackbar.Snackbar
import com.example.myapplicationv10.databinding.ActivityStatisticsBinding
import kotlinx.coroutines.launch

class StatisticsActivity : BaseActivity() {


    private lateinit var binding: ActivityStatisticsBinding
    private lateinit var viewModel: StatisticsViewModel

    // Track which valves are selected for display (all selected by default)
    private val selectedValves = mutableSetOf(1, 2, 3, 4, 5, 6, 7, 8)

    // Real data from backend
    private var totalValves = 8
    private var activeValves = 0
    private var inactiveValves = 0
    private var maintenanceValves = 0
    private var currentDevice: Device? = null

    // Current period
    private var currentPeriod = "Last 24 hours"

    // Period types
    private val periods = arrayOf(
        "Last 24 hours",
        "Last 7 days",
        "Last 30 days",
        "Last 3 months",
        "Last year",
        "Custom range"
    )

    // Valve colors matching the chart
    private val valveColors = listOf(
        Color.parseColor("#FF5252"),  // Red
        Color.parseColor("#156f35"),  // Green
        Color.parseColor("#FF9800"),  // Orange
        Color.parseColor("#2196F3"),  // Blue
        Color.parseColor("#E91E63"),  // Pink
        Color.parseColor("#91BC21"),  // Lime
        Color.parseColor("#9C27B0"),  // Purple
        Color.parseColor("#00BCD4")   // Cyan
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStatisticsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.topBar) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize ViewModel
        viewModel = ViewModelProvider(this)[StatisticsViewModel::class.java]

        setupBackButton()
        setupStatisticsCards()
        setupValveChips()
        setupPeriodSpinner()
        setupExportButton()
        setupLineChart()

        // Observe data from backend
        observeViewModel()

        // Load devices and stats
        viewModel.loadDevices()
    }

    private fun setupBackButton() {
        binding.backButton.setOnClickListener { finish() }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.devicesState.collect { result ->
                when (result) {
                    is NetworkResult.Success -> {
                        if (result.data.isNotEmpty()) {
                            // Use the first device for statistics
                            currentDevice = result.data.first()
                            currentDevice?.let { device ->
                                // Calculate statistics from device pistons
                                totalValves = device.pistons.size
                                activeValves = device.pistons.count { it.state == "active" }
                                inactiveValves = device.pistons.count { it.state == "inactive" }
                                maintenanceValves = 0 // No maintenance state from backend

                                // Update UI
                                setupStatisticsCards()

                                // Load detailed stats from backend
                                viewModel.loadDeviceStats(device.id)

                                // Load chart data (still uses sample data for now)
                                loadChartData(currentPeriod)
                            }
                        }
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

        lifecycleScope.launch {
            viewModel.statsState.collect { result ->
                when (result) {
                    is NetworkResult.Success -> {
                        // Update with backend stats
                        val stats = result.data
                        totalValves = stats.totalPistons
                        activeValves = stats.activePistons
                        inactiveValves = stats.totalPistons - stats.activePistons
                        setupStatisticsCards()
                    }
                    is NetworkResult.Error -> {
                        // Stats failed to load, keep using device data
                    }
                    else -> {}
                }
            }
        }
    }

    private fun setupStatisticsCards() {
        binding.totalValvesValue.text = totalValves.toString()
        binding.activeValvesValue.text = activeValves.toString()
        binding.inactiveValvesValue.text = inactiveValves.toString()
        binding.maintenanceValvesValue.text = maintenanceValves.toString()
    }

    private fun setupValveChips() {
        binding.valveChipsContainer.removeAllViews()
        for (i in 1..8) binding.valveChipsContainer.addView(createValveChip(i, valveColors[i - 1]))
        binding.valveChipsContainer.addView(createSelectAllButton())
        binding.valveChipsContainer.addView(createDeselectAllButton())
    }

    private fun createValveChip(valveNumber: Int, color: Int): TextView {
        return TextView(this).apply {
            text = "Valve $valveNumber"
            textSize = 14f
            setTextColor(Color.WHITE)
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(24, 16, 24, 16)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(8, 8, 8, 8) }

            background = createChipBackground(color, true)
            setOnClickListener {
                if (selectedValves.contains(valveNumber)) {
                    selectedValves.remove(valveNumber)
                    background = createChipBackground(color, false)
                    setTextColor(color)
                } else {
                    selectedValves.add(valveNumber)
                    background = createChipBackground(color, true)
                    setTextColor(Color.WHITE)
                }
                loadChartData(currentPeriod)
            }
        }
    }

    private fun createSelectAllButton(): TextView {
        return TextView(this).apply {
            text = "✓ All"
            textSize = 14f
            setTextColor(Color.WHITE)
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(24, 16, 24, 16)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(8, 8, 8, 8) }
            background = createChipBackground(Color.parseColor("#4CAF50"), true)
            setOnClickListener {
                selectedValves.clear()
                selectedValves.addAll(1..8)
                updateAllChipsAppearance(true)
                loadChartData(currentPeriod)
            }
        }
    }

    private fun createDeselectAllButton(): TextView {
        return TextView(this).apply {
            text = "✗ None"
            textSize = 14f
            setTextColor(Color.WHITE)
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(24, 16, 24, 16)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(8, 8, 8, 8) }
            background = createChipBackground(Color.parseColor("#FF5252"), true)
            setOnClickListener {
                selectedValves.clear()
                updateAllChipsAppearance(false)
                loadChartData(currentPeriod)
            }
        }
    }

    private fun createChipBackground(color: Int, isSelected: Boolean) =
        android.graphics.drawable.GradientDrawable().apply {
            shape = android.graphics.drawable.GradientDrawable.RECTANGLE
            cornerRadius = 50f
            if (isSelected) setColor(color) else {
                setColor(Color.TRANSPARENT)
                setStroke(6, color)
            }
        }

    private fun updateAllChipsAppearance(selected: Boolean) {
        for (i in 0 until 8) {
            val chip = binding.valveChipsContainer.getChildAt(i) as? TextView
            chip?.let {
                val color = valveColors[i]
                it.background = createChipBackground(color, selected)
                it.setTextColor(if (selected) Color.WHITE else color)
            }
        }
    }

    private fun setupPeriodSpinner() {
        val adapter = android.widget.ArrayAdapter(this, android.R.layout.simple_spinner_item, periods)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.periodSpinner.adapter = adapter
        binding.periodSpinner.setSelection(0)

        binding.periodSpinner.setOnItemSelectedListener(object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                val selectedPeriod = periods[position]
                currentPeriod = selectedPeriod
                if (selectedPeriod == "Custom range") showCustomDateRangePicker() else loadChartData(selectedPeriod)
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        })
    }

    private fun setupExportButton() {
        binding.exportPdfButton.setOnClickListener { exportToPdf() }
    }

    private fun setupLineChart() {
        val lineChart = binding.activationsChart
        lineChart.description.isEnabled = false
        lineChart.setTouchEnabled(true)
        lineChart.setDragEnabled(true)
        lineChart.setScaleEnabled(true)
        lineChart.setPinchZoom(true)
        lineChart.setDrawGridBackground(false)
        lineChart.setBackgroundColor(Color.WHITE)
        val xAxis = lineChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.granularity = 1f
        xAxis.textColor = ContextCompat.getColor(this, R.color.black)
        val leftAxis = lineChart.axisLeft
        leftAxis.setDrawGridLines(true)
        leftAxis.gridColor = ContextCompat.getColor(this, R.color.black)
        leftAxis.gridLineWidth = 0.5f
        leftAxis.textColor = ContextCompat.getColor(this, R.color.black)
        lineChart.axisRight.isEnabled = false
        lineChart.legend.textColor = ContextCompat.getColor(this, R.color.black)
        lineChart.legend.textSize = 12f
    }

    private fun loadChartData(period: String) {
        val entries = mutableMapOf<String, MutableList<Entry>>()
        for (i in 1..8) entries["Valve $i"] = mutableListOf()

        val dataPoints = when (period) {
            "Last 24 hours" -> generateLast24HoursData(entries)
            "Last 7 days" -> generateLast7DaysData(entries)
            "Last 30 days" -> generateLast30DaysData(entries)
            "Last 3 months" -> generateLast3MonthsData(entries)
            "Last year" -> generateLastYearData(entries)
            else -> generateLast24HoursData(entries)
        }

        updateChart(entries, dataPoints, period)
    }

    private fun generateLast24HoursData(entries: MutableMap<String, MutableList<Entry>>): Int {
        val hours = 24
        for (i in 0 until hours) for (valve in 1..8) entries["Valve $valve"]?.add(Entry(i.toFloat(), (0..5).random().toFloat()))
        return hours
    }

    private fun generateLast7DaysData(entries: MutableMap<String, MutableList<Entry>>): Int {
        val days = 7
        for (i in 0 until days) for (valve in 1..8) entries["Valve $valve"]?.add(Entry(i.toFloat(), (5..50).random().toFloat()))
        return days
    }

    private fun generateLast30DaysData(entries: MutableMap<String, MutableList<Entry>>): Int {
        val days = 30
        for (i in 0 until days) for (valve in 1..8) entries["Valve $valve"]?.add(Entry(i.toFloat(), (10..100).random().toFloat()))
        return days
    }

    private fun generateLast3MonthsData(entries: MutableMap<String, MutableList<Entry>>): Int {
        val months = 3
        for (i in 0 until months) for (valve in 1..8) entries["Valve $valve"]?.add(Entry(i.toFloat(), (100..500).random().toFloat()))
        return months
    }

    private fun generateLastYearData(entries: MutableMap<String, MutableList<Entry>>): Int {
        val months = 12
        for (i in 0 until months) for (valve in 1..8) entries["Valve $valve"]?.add(Entry(i.toFloat(), (500..2000).random().toFloat()))
        return months
    }

    private fun updateChart(entries: MutableMap<String, MutableList<Entry>>, dataPoints: Int, period: String) {
        val dataSets = mutableListOf<LineDataSet>()
        entries.entries.forEachIndexed { index, (valveName, data) ->
            val valveNumber = index + 1
            if (selectedValves.contains(valveNumber)) {
                val dataSet = LineDataSet(data, valveName).apply {
                    color = valveColors[index % valveColors.size]
                    setCircleColor(valveColors[index % valveColors.size])
                    lineWidth = 3f
                    circleRadius = 4f
                    setDrawCircleHole(true)
                    circleHoleColor = Color.WHITE
                    circleHoleRadius = 2f
                    valueTextSize = 0f
                    setDrawValues(false)
                    mode = LineDataSet.Mode.CUBIC_BEZIER
                    cubicIntensity = 0.2f
                    setDrawFilled(true)
                    fillAlpha = 30
                    fillColor = valveColors[index % valveColors.size]
                }
                dataSets.add(dataSet)
            }
        }

        val lineChart = binding.activationsChart
        if (dataSets.isEmpty()) {
            lineChart.clear()
            lineChart.setNoDataText("Veuillez sélectionner au moins une vanne")
            lineChart.setNoDataTextColor(ContextCompat.getColor(this, R.color.black))
            lineChart.invalidate()
            return
        }

        lineChart.data = LineData(dataSets as List<LineDataSet>)
        lineChart.xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                val index = value.toInt()
                return when (period) {
                    "Last 24 hours" -> "${index}h"
                    "Last 7 days", "Last 30 days" -> "Day ${index + 1}"
                    "Last 3 months" -> "Month ${index + 1}"
                    "Last year" -> getMonthName(index)
                    else -> index.toString()
                }
            }
        }
        lineChart.invalidate()
        lineChart.animateX(1000)
    }

    private fun getMonthName(index: Int): String {
        val months = arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
        return if (index in months.indices) months[index] else ""
    }

    private fun showCustomDateRangePicker() {
        Snackbar.make(binding.main, "Custom date range picker - Coming soon", Snackbar.LENGTH_SHORT).show()
        binding.periodSpinner.setSelection(0)
    }

    private fun exportToPdf() {
        Snackbar.make(binding.main, "Exporting statistics to PDF...", Snackbar.LENGTH_SHORT).show()
    }


}
