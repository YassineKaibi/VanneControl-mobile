package com.example.myapplicationv10

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar

class DashboardActivity : AppCompatActivity() {

    // Data class for valve information
    data class Valve(
        val id: Int,
        val name: String,
        var isActive: Boolean,
        var lastChanged: String = ""
    )

    // Sample active valves data
    private val activeValves = mutableListOf<Valve>()

    private lateinit var activeValvesRecyclerView: RecyclerView
    private lateinit var activeValvesAdapter: ActiveValvesAdapter

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

        // Charger les données des valves actives
        loadActiveValves()

        // Démarrer la mise à jour périodique des états
        startPeriodicStatusUpdate()
    }

    private fun setupProfileButton() {
        findViewById<ImageView>(R.id.profileIcon).setOnClickListener {
            // TODO: Navigate to profile activity
            Snackbar.make(it, "Profile clicked", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun setupActiveValvesRecyclerView() {
        activeValvesRecyclerView = findViewById(R.id.activeValvesRecyclerView)
        activeValvesRecyclerView.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        activeValvesAdapter = ActiveValvesAdapter(activeValves)
        activeValvesRecyclerView.adapter = activeValvesAdapter
    }

    private fun loadActiveValves() {
        // Charger les valves actives depuis le backend
        runOnNetwork {
            try {
                // TODO: Remplacer par un vrai appel API
                // val response = apiClient.getActiveValves()

                // Simuler un appel réseau
                Thread.sleep(800)

                // Simuler des données (à remplacer)
                val fetchedValves = listOf(
                    Valve(1, "Valve 1", true, "10:30 AM"),
                    Valve(3, "Valve 3", true, "11:15 AM"),
                    Valve(5, "Valve 5", true, "09:45 AM"),
                    Valve(7, "Valve 7", true, "12:20 PM")
                )

                // Mettre à jour l'UI sur le thread principal
                runOnUiThread {
                    activeValves.clear()
                    activeValves.addAll(fetchedValves)
                    activeValvesAdapter.notifyDataSetChanged()
                }

            } catch (e: Exception) {
                e.printStackTrace()

                runOnUiThread {
                    Snackbar.make(
                        findViewById(R.id.main),
                        "Erreur de chargement des valves",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun startPeriodicStatusUpdate() {
        // Mettre à jour l'état des valves toutes les 10 secondes
        ThreadManager.runOnMainThreadDelayed(10000) {
            if (!isFinishing) {
                refreshValveStatus()
                startPeriodicStatusUpdate() // Relancer
            }
        }
    }

    private fun refreshValveStatus() {
        runOnNetwork {
            try {
                // TODO: Récupérer les états mis à jour depuis le backend
                // val updatedStates = apiClient.getValveStates()

                // Simuler un appel réseau léger
                Thread.sleep(200)

                // Mettre à jour l'UI sur le thread principal
                runOnUiThread {
                    // TODO: Mettre à jour activeValves avec les nouvelles données
                    // activeValvesAdapter.notifyDataSetChanged()
                }

            } catch (e: Exception) {
                // Silent fail pour les mises à jour périodiques
                e.printStackTrace()
            }
        }
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
            Snackbar.make(it, "Opening Notifications...", Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        // Recharger les valves actives quand on revient au dashboard
        loadActiveValves()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Les threads seront automatiquement nettoyés par ThreadManager
    }
}