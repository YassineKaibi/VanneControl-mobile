package com.example.myapplicationv10

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class DashboardActivity : AppCompatActivity() {

    // Data class for valve information
    data class Valve(
        val id: Int,
        val name: String,
        var isActive: Boolean,
        var lastChanged: String = ""
    )

    // Sample active valves data
    private val activeValves = mutableListOf(
        Valve(1, "Valve 1", true, "10:30 AM"),
        Valve(3, "Valve 3", true, "11:15 AM"),
        Valve(5, "Valve 5", true, "09:45 AM"),
        Valve(7, "Valve 7", true, "12:20 PM")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dashboard)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupProfileButton()
        setupActiveValvesRecyclerView()
        setupNavigationButtons()
    }

    private fun setupProfileButton() {
        findViewById<ImageView>(R.id.profileIcon).setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupActiveValvesRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.activeValvesRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.adapter = ActiveValvesAdapter(activeValves)
    }

    private fun setupNavigationButtons() {
        // Valve Control Button
        findViewById<CardView>(R.id.valveControlCard).setOnClickListener {
            val intent = Intent(this, ValveManagementActivity::class.java)
            startActivity(intent)
        }

        // History Button
        findViewById<CardView>(R.id.historyCard).setOnClickListener {
            val intent = Intent(this, HistoryActivity::class.java)
            startActivity(intent)
        }

        // Statistics Button
        findViewById<CardView>(R.id.statisticsCard).setOnClickListener {
            val intent = Intent(this, StatisticsActivity::class.java)
            startActivity(intent)
        }

        // Notifications Button
        findViewById<CardView>(R.id.notificationsCard).setOnClickListener {
            // TODO: Create NotificationsActivity
            com.google.android.material.snackbar.Snackbar.make(it, "Opening Notifications...", com.google.android.material.snackbar.Snackbar.LENGTH_SHORT).show()
        }
    }
}