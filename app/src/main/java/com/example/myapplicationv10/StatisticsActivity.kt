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
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.android.material.snackbar.Snackbar
import com.example.myapplicationv10.databinding.ActivityStatisticsBinding
import com.example.myapplicationv10.model.TelemetryEvent
import com.google.gson.Gson
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class StatisticsActivity : BaseActivity() {

    private lateinit var binding: ActivityStatisticsBinding
    private lateinit var viewModel: StatisticsViewModel

    private val selectedValves = mutableSetOf<Int>()

    private var totalValves = 8
    private var activeValves = 0
    private var inactiveValves = 0
    private var maintenanceValves = 0
    private var currentDevice: Device? = null

    private var currentPeriod = "Last 24 hours"

    private val periods = arrayOf(
        "Last 24 hours",
        "Last 7 days",
        "Last 30 days",
        "Last 3 months",
        "Last year",
        "Custom range"
    )

    private val valveColors = listOf(
        Color.parseColor("#FF5252"),
        Color.parseColor("#156f35"),
        Color.parseColor("#FF9800"),
        Color.parseColor("#2196F3"),
        Color.parseColor("#E91E63"),
        Color.parseColor("#91BC21"),
        Color.parseColor("#9C27B0"),
        Color.parseColor("#00BCD4")
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

        viewModel = ViewModelProvider(this)[StatisticsViewModel::class.java]

        val sharedPref = getSharedPreferences("VanneControl", MODE_PRIVATE)
        totalValves = sharedPref.getInt("totalValves", 8)

        setupBackButton()
        setupStatisticsCards()
        setupValveChips()
        setupPeriodSpinner()
        setupExportButton()
        setupLineChart()

        observeViewModel()
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
                            currentDevice = result.data.first()
                            currentDevice?.let { device ->
                                totalValves = device.pistons.size
                                activeValves = device.pistons.count { it.state == "active" }
                                inactiveValves = device.pistons.count { it.state == "inactive" }
                                maintenanceValves = 0
                                setupStatisticsCards()
                                setupValveChips()

                                viewModel.loadDeviceStats(device.id)
                                viewModel.loadHistory(deviceId = device.id, limit = 5000)
                            }
                        }
                    }
                    is NetworkResult.Error -> {
                        Snackbar.make(binding.root, result.message, Snackbar.LENGTH_LONG).show()
                    }
                    else -> {}
                }
            }
        }

        lifecycleScope.launch {
            viewModel.statsState.collect { result ->
                when (result) {
                    is NetworkResult.Success -> {
                        val stats = result.data
                        totalValves = stats.totalPistons
                        activeValves = stats.activePistons
                        inactiveValves = stats.totalPistons - stats.activePistons
                        setupStatisticsCards()
                    }
                    else -> {}
                }
            }
        }

        lifecycleScope.launch {
            viewModel.historyState.collect { result ->
                when (result) {
                    is NetworkResult.Success -> {
                        updateChartFromHistory(result.data)
                    }
                    else -> {
                        binding.activationsChart.clear()
                        binding.activationsChart.setNoDataText("Aucune donnée disponible")
                        binding.activationsChart.invalidate()
                    }
                }
            }
        }
    }

    private fun updateChartFromHistory(events: List<TelemetryEvent>) {
        val gson = Gson()
        val valveData = mutableMapOf<Int, MutableList<Entry>>()

        for (event in events) {
            try {
                @Suppress("UNCHECKED_CAST")
                val payload = event.payload?.let { gson.fromJson(it, Map::class.java) as? Map<String, Any> }
                val pistonNumber = (payload?.get("piston_number") as? Double)?.toInt() ?: continue

                if (pistonNumber !in selectedValves) continue

                val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                val timestamp = dateFormat.parse(event.createdAt.replace("Z", ""))?.time ?: continue

                valveData.getOrPut(pistonNumber) { mutableListOf() }
                    .add(Entry(timestamp.toFloat(), 1f))
            } catch (e: Exception) {
                continue
            }
        }

        val dataSets = mutableListOf<LineDataSet>()

        for ((valveNum, entries) in valveData.toSortedMap()) {
            if (entries.isEmpty()) continue

            entries.sortBy { it.x }
            var cumulative = 0f
            val cumulativeEntries = entries.mapIndexed { index, _ ->
                cumulative++
                Entry(index.toFloat(), cumulative)
            }

            val colorIndex = (valveNum - 1) % valveColors.size
            val dataSet = LineDataSet(cumulativeEntries, "Valve $valveNum").apply {
                color = valveColors[colorIndex]
                setCircleColor(valveColors[colorIndex])
                lineWidth = 3f
                circleRadius = 4f
                setDrawCircleHole(true)
                circleHoleColor = Color.WHITE
                circleHoleRadius = 2f
                setDrawValues(false)
                mode = LineDataSet.Mode.CUBIC_BEZIER
                cubicIntensity = 0.2f
                setDrawFilled(true)
                fillAlpha = 30
                fillColor = valveColors[colorIndex]
            }
            dataSets.add(dataSet)
        }

        val lineChart = binding.activationsChart
        if (dataSets.isEmpty()) {
            lineChart.clear()
            lineChart.setNoDataText("Aucune donnée disponible pour la période sélectionnée")
            lineChart.setNoDataTextColor(ContextCompat.getColor(this, R.color.black))
            lineChart.invalidate()
            return
        }

        lineChart.data = LineData(dataSets as List<LineDataSet>)
        lineChart.invalidate()
        lineChart.animateX(1000)
    }

    private fun setupStatisticsCards() {
        binding.totalValvesValue.text = totalValves.toString()
        binding.activeValvesValue.text = activeValves.toString()
        binding.inactiveValvesValue.text = inactiveValves.toString()
        binding.maintenanceValvesValue.text = maintenanceValves.toString()
    }

    private fun setupValveChips() {
        binding.valveChipsContainer.removeAllViews()
        selectedValves.clear()

        for (i in 1..totalValves) {
            val colorIndex = (i - 1) % valveColors.size
            binding.valveChipsContainer.addView(createValveChip(i, valveColors[colorIndex]))
            selectedValves.add(i)
        }

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
                currentDevice?.id?.let { viewModel.loadHistory(deviceId = it, limit = 5000) }
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
                selectedValves.addAll(1..totalValves)
                updateAllChipsAppearance(true)
                currentDevice?.id?.let { viewModel.loadHistory(deviceId = it, limit = 5000) }
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
                currentDevice?.id?.let { viewModel.loadHistory(deviceId = it, limit = 5000) }
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
        for (i in 0 until totalValves) {
            val chip = binding.valveChipsContainer.getChildAt(i) as? TextView
            chip?.let {
                val colorIndex = i % valveColors.size
                val color = valveColors[colorIndex]
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
                if (selectedPeriod == "Custom range") {
                    showCustomDateRangePicker()
                } else {
                    currentDevice?.id?.let { viewModel.loadHistory(deviceId = it, limit = 5000) }
                }
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

    private fun showCustomDateRangePicker() {
        Snackbar.make(binding.main, "Custom date range picker - Coming soon", Snackbar.LENGTH_SHORT).show()
        binding.periodSpinner.setSelection(0)
    }

    private fun exportToPdf() {
        Snackbar.make(binding.main, "Exporting statistics to PDF...", Snackbar.LENGTH_SHORT).show()
    }
}