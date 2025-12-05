package com.example.myapplicationv10

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.myapplicationv10.network.NetworkResult
import com.example.myapplicationv10.viewmodel.DashboardViewModel
import com.example.myapplicationv10.websocket.WebSocketManager
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * DashboardActivity - Écran principal avec MVVM
 *
 * Utilise DashboardViewModel pour gérer les appareils
 * Observe les StateFlow pour mettre à jour l'UI de manière réactive
 * Intègre le WebSocket pour les mises à jour en temps réel
 */
class DashboardActivity : BaseActivity() {

    private lateinit var activeValvesRecyclerView: RecyclerView
    private lateinit var activeValvesAdapter: ActiveValvesAdapter
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    // ViewModel
    private val viewModel: DashboardViewModel by viewModels()

    // WebSocket manager
    private lateinit var webSocketManager: WebSocketManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dashboard)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initializeViews()
        setupProfileButton()
        setupActiveValvesRecyclerView()
        setupNavigationButtons()
        setupSwipeRefresh()
        observeViewModel()
        setupWebSocket()
    }

    private fun initializeViews() {
        activeValvesRecyclerView = findViewById(R.id.activeValvesRecyclerView)
        // Note: SwipeRefreshLayout doit être dans votre XML
        // Si absent, commentez cette ligne
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
    }

    private fun setupProfileButton() {
        findViewById<ImageView>(R.id.profileIcon).setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupActiveValvesRecyclerView() {
        activeValvesRecyclerView.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        activeValvesAdapter = ActiveValvesAdapter(emptyList())
        activeValvesRecyclerView.adapter = activeValvesAdapter
    }

    private fun setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener {
            viewModel.refreshDevices()
        }
    }

    private fun setupNavigationButtons() {
        // Valve Control Button
        findViewById<CardView>(R.id.valveControlCard).setOnClickListener {
            // Récupérer le premier appareil disponible
            val currentState = viewModel.devicesState.value
            if (currentState is NetworkResult.Success && currentState.data.isNotEmpty()) {
                val firstDevice = currentState.data.first()
                val intent = Intent(this, ValveManagementActivity::class.java)
                intent.putExtra("DEVICE_ID", firstDevice.id)
                intent.putExtra("DEVICE_NAME", firstDevice.name)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Aucun appareil disponible", Toast.LENGTH_SHORT).show()
            }
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
            Snackbar.make(it, "Notifications à venir...", Snackbar.LENGTH_SHORT).show()
        }
    }

    /**
     * Observer les StateFlow du ViewModel
     */
    private fun observeViewModel() {
        // Observer l'état des appareils
        lifecycleScope.launch {
            viewModel.devicesState.collect { result ->
                when (result) {
                    is NetworkResult.Idle -> {
                        // État initial - Ne rien faire
                        swipeRefreshLayout.isRefreshing = false
                    }

                    is NetworkResult.Loading -> {
                        // Afficher le chargement si pas en cours de rafraîchissement
                        if (!swipeRefreshLayout.isRefreshing) {
                            // Optionnel: Afficher un loading indicator
                        }
                    }

                    is NetworkResult.Success -> {
                        swipeRefreshLayout.isRefreshing = false

                        // Mettre à jour la liste des pistons actifs
                        val activePistons = viewModel.getActivePistons()
                        // TODO: Adapter ActiveValvesAdapter pour accepter des Pairs
                        // Pour l'instant, on garde l'ancienne structure

                        Toast.makeText(
                            this@DashboardActivity,
                            "${result.data.size} appareil(s) chargé(s)",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    is NetworkResult.Error -> {
                        swipeRefreshLayout.isRefreshing = false

                        Snackbar.make(
                            findViewById(R.id.main),
                            result.message,
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }

        // Observer l'état de rafraîchissement
        lifecycleScope.launch {
            viewModel.isRefreshing.collect { isRefreshing ->
                swipeRefreshLayout.isRefreshing = isRefreshing
            }
        }
    }

    /**
     * Configurer le WebSocket pour les mises à jour en temps réel
     */
    private fun setupWebSocket() {
        webSocketManager = WebSocketManager.getInstance(this)

        // Connecter au WebSocket
        webSocketManager.connect()

        // Écouter les mises à jour de pistons
        webSocketManager.addPistonUpdateListener { message ->
            lifecycleScope.launch(Dispatchers.Main) {
                // Rafraîchir les données quand un piston change
                viewModel.refreshDevices()

                Toast.makeText(
                    this@DashboardActivity,
                    "Piston ${message.pistonNumber} mis à jour: ${message.state}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        // Écouter les changements de statut d'appareil
        webSocketManager.addDeviceStatusListener { message ->
            lifecycleScope.launch(Dispatchers.Main) {
                viewModel.refreshDevices()

                Toast.makeText(
                    this@DashboardActivity,
                    "Appareil ${message.status}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Recharger les données quand on revient au dashboard
        viewModel.refreshDevices()

        // Reconnecter le WebSocket si déconnecté
        if (!webSocketManager.isConnected()) {
            webSocketManager.connect()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Déconnecter le WebSocket
        webSocketManager.disconnect()
    }

    /**
     * Data class pour représenter une valve dans l'adapter
     */
    data class Valve(
        val name: String,
        val lastChanged: String
    )
}