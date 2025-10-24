package com.example.myapplicationv10

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.util.*

class StatisticsActivity : AppCompatActivity() {

    private lateinit var lineChart: LineChart
    private lateinit var periodSpinner: Spinner
    private lateinit var exportButton: Button

    // Statistics Cards
    private lateinit var totalValvesText: TextView
    private lateinit var activeValvesText: TextView
    private lateinit var inactiveValvesText: TextView
    private lateinit var maintenanceValvesText: TextView

    // Sample data
    private var totalValves = 8
    private var activeValves = 4
    private var inactiveValves = 3
    private var maintenanceValves = 1

    // Period types
    private val periods = arrayOf(
        "Last 24 hours",
        "Last 7 days",
        "Last 30 days",
        "Last 3 months",
        "Last year",
        "Custom range"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistics)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.topBar)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initializeViews()
        setupBackButton()
        setupStatisticsCards()
        setupPeriodSpinner()
        setupExportButton()
        setupLineChart()
        loadChartData("Last 24 hours")
    }

    private fun initializeViews() {
        lineChart = findViewById(R.id.activationsChart)
        periodSpinner = findViewById(R.id.periodSpinner)
        exportButton = findViewById(R.id.exportPdfButton)

        totalValvesText = findViewById(R.id.totalValvesValue)
        activeValvesText = findViewById(R.id.activeValvesValue)
        inactiveValvesText = findViewById(R.id.inactiveValvesValue)
        maintenanceValvesText = findViewById(R.id.maintenanceValvesValue)
    }

    private fun setupBackButton() {
        findViewById<ImageView>(R.id.backButton).setOnClickListener {
            finish()
        }
    }

    private fun setupStatisticsCards() {
        totalValvesText.text = totalValves.toString()
        activeValvesText.text = activeValves.toString()
        inactiveValvesText.text = inactiveValves.toString()
        maintenanceValvesText.text = maintenanceValves.toString()
    }

    private fun setupPeriodSpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, periods)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        periodSpinner.adapter = adapter

        periodSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedPeriod = periods[position]
                if (selectedPeriod == "Custom range") {
                    showCustomDateRangePicker()
                } else {
                    loadChartData(selectedPeriod)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupExportButton() {
        exportButton.setOnClickListener {
            exportToPdf()
        }
    }

    private fun setupLineChart() {
        // Chart styling
        lineChart.description.isEnabled = false
        lineChart.setTouchEnabled(true)
        lineChart.setDragEnabled(true)
        lineChart.setScaleEnabled(true)
        lineChart.setPinchZoom(true)
        lineChart.setDrawGridBackground(false)
        lineChart.setBackgroundColor(Color.WHITE)

        // X-Axis configuration
        val xAxis = lineChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.granularity = 1f
        xAxis.textColor = ContextCompat.getColor(this, R.color.black)

        // Y-Axis configuration
        val leftAxis = lineChart.axisLeft
        leftAxis.setDrawGridLines(true)
        leftAxis.gridColor = ContextCompat.getColor(this, R.color.black)
        leftAxis.gridLineWidth = 0.5f
        leftAxis.textColor = ContextCompat.getColor(this, R.color.black)

        val rightAxis = lineChart.axisRight
        rightAxis.isEnabled = false

        // Legend
        lineChart.legend.textColor = ContextCompat.getColor(this, R.color.black)
        lineChart.legend.textSize = 12f
    }

    private fun loadChartData(period: String) {
        val entries = mutableMapOf<String, MutableList<Entry>>()

        // Initialize entries for each valve
        for (i in 1..8) {
            entries["Valve $i"] = mutableListOf()
        }

        // Generate sample data based on period
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
        for (i in 0 until hours) {
            for (valve in 1..8) {
                val activations = (0..5).random()
                entries["Valve $valve"]?.add(Entry(i.toFloat(), activations.toFloat()))
            }
        }
        return hours
    }

    private fun generateLast7DaysData(entries: MutableMap<String, MutableList<Entry>>): Int {
        val days = 7
        for (i in 0 until days) {
            for (valve in 1..8) {
                val activations = (5..50).random()
                entries["Valve $valve"]?.add(Entry(i.toFloat(), activations.toFloat()))
            }
        }
        return days
    }

    private fun generateLast30DaysData(entries: MutableMap<String, MutableList<Entry>>): Int {
        val days = 30
        for (i in 0 until days) {
            for (valve in 1..8) {
                val activations = (10..100).random()
                entries["Valve $valve"]?.add(Entry(i.toFloat(), activations.toFloat()))
            }
        }
        return days
    }

    private fun generateLast3MonthsData(entries: MutableMap<String, MutableList<Entry>>): Int {
        val months = 3
        for (i in 0 until months) {
            for (valve in 1..8) {
                val activations = (100..500).random()
                entries["Valve $valve"]?.add(Entry(i.toFloat(), activations.toFloat()))
            }
        }
        return months
    }

    private fun generateLastYearData(entries: MutableMap<String, MutableList<Entry>>): Int {
        val months = 12
        for (i in 0 until months) {
            for (valve in 1..8) {
                val activations = (500..2000).random()
                entries["Valve $valve"]?.add(Entry(i.toFloat(), activations.toFloat()))
            }
        }
        return months
    }

    private fun updateChart(
        entries: MutableMap<String, MutableList<Entry>>,
        dataPoints: Int,
        period: String
    ) {
        val dataSets = mutableListOf<LineDataSet>()

        // Bright and vibrant colors with red accents
        val colors = listOf(
            Color.parseColor("#FF5252"),  // Bright Red - Valve 1
            Color.parseColor("#156f35"),  // Green - Valve 2
            Color.parseColor("#FF9800"),  // Bright Orange - Valve 3
            Color.parseColor("#2196F3"),  // Bright Blue - Valve 4
            Color.parseColor("#E91E63"),  // Pink/Magenta - Valve 5
            Color.parseColor("#91BC21"),  // Lime Green - Valve 6
            Color.parseColor("#9C27B0"),  // Purple - Valve 7
            Color.parseColor("#00BCD4")   // Cyan - Valve 8
        )

        entries.entries.forEachIndexed { index, (valveName, data) ->
            val dataSet = LineDataSet(data, valveName)
            dataSet.color = colors[index % colors.size]
            dataSet.setCircleColor(colors[index % colors.size])
            dataSet.lineWidth = 3f
            dataSet.circleRadius = 4f
            dataSet.setDrawCircleHole(true)
            dataSet.circleHoleColor = Color.WHITE
            dataSet.circleHoleRadius = 2f
            dataSet.valueTextSize = 0f
            dataSet.setDrawValues(false)
            dataSet.mode = LineDataSet.Mode.CUBIC_BEZIER
            dataSet.cubicIntensity = 0.2f

            // Add fill for more visual impact
            dataSet.setDrawFilled(true)
            dataSet.fillAlpha = 30
            dataSet.fillColor = colors[index % colors.size]

            dataSets.add(dataSet)
        }

        val lineData = LineData(dataSets as List<LineDataSet>)
        lineChart.data = lineData

        // X-Axis formatter based on period
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
        // TODO: Implement custom date range picker
        Snackbar.make(
            findViewById(R.id.main),
            "Custom date range picker - Coming soon",
            Snackbar.LENGTH_SHORT
        ).show()

        // Reset spinner to default
        periodSpinner.setSelection(0)
    }

    private fun exportToPdf() {
        // TODO: Implement PDF export functionality
        Snackbar.make(
            findViewById(R.id.main),
            "Exporting statistics to PDF...",
            Snackbar.LENGTH_SHORT
        ).show()

        // In a real implementation, you would:
        // 1. Create a PDF document
        // 2. Add statistics cards data
        // 3. Add chart as image
        // 4. Save to device storage
        // 5. Share or open the PDF
    }
}