package com.example.myapplicationv10

import android.app.AlertDialog
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
import com.example.myapplicationv10.network.NetworkResult
import com.example.myapplicationv10.utils.Constants
import com.example.myapplicationv10.viewmodel.ValveManagementViewModel
import com.example.myapplicationv10.websocket.WebSocketManager
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * ValveManagementActivity - Gestion des 8 pistons avec MVVM
 *
 * Utilise ValveManagementViewModel pour gérer les pistons
 * Observe les StateFlow pour mettre à jour l'UI de manière réactive
 * Intègre le WebSocket pour les mises à jour en temps réel
 */
class ValveManagementActivity : AppCompatActivity() {

    // ViewModel
    private val viewModel: ValveManagementViewModel by viewModels()

    // WebSocket manager
    private lateinit var webSocketManager: WebSocketManager

    // ID et nom de l'appareil
    private var deviceId: String? = null
    private var deviceName: String? = null

    // Map pour garder une référence des CardViews et ImageViews
    private val valveViews = mutableMapOf<Int, Pair<CardView, ImageView>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_valve_management)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.topBar)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Récupérer l'ID de l'appareil depuis l'Intent
        deviceId = intent.getStringExtra("DEVICE_ID")
        deviceName = intent.getStringExtra("DEVICE_NAME")

        if (deviceId == null) {
            Toast.makeText(this, "Erreur: Aucun appareil sélectionné", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        setupBackButton()
        setupValveControls()
        observeViewModel()
        setupWebSocket()

        // Charger les données de l'appareil
        viewModel.loadDevice(deviceId!!)
    }

    private fun setupBackButton() {
        findViewById<ImageView>(R.id.backButton).setOnClickListener {
            finish()
        }
    }

    private fun setupValveControls() {
        // Configuration des 8 valves
        for (i in 1..8) {
            val cardId = resources.getIdentifier("valve${i}Card", "id", packageName)
            val iconId = resources.getIdentifier("valve${i}Icon", "id", packageName)

            val card = findViewById<CardView>(cardId)
            val icon = findViewById<ImageView>(iconId)

            // Stocker les références
            valveViews[i] = Pair(card, icon)

            // Gérer le clic
            card.setOnClickListener {
                showConfirmationDialog(i)
            }
        }
    }

    /**
     * Observer les StateFlow du ViewModel
     */
    private fun observeViewModel() {
        // Observer l'état de l'appareil
        lifecycleScope.launch {
            viewModel.deviceState.collect { result ->
                when (result) {
                    is NetworkResult.Idle -> {
                        // État initial - Ne rien faire
                    }

                    is NetworkResult.Loading -> {
                        // Optionnel: Afficher un loading indicator
                    }

                    is NetworkResult.Success -> {
                        val device = result.data

                        // Mettre à jour l'UI pour chaque piston
                        device.pistons.forEach { piston ->
                            updatePistonUI(piston.pistonNumber, piston.state)
                        }
                    }

                    is NetworkResult.Error -> {
                        Snackbar.make(
                            findViewById(android.R.id.content),
                            result.message,
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }

        // Observer l'état du contrôle
        lifecycleScope.launch {
            viewModel.controlState.collect { result ->
                when (result) {
                    is NetworkResult.Idle -> {
                        // État initial - Ne rien faire
                    }

                    is NetworkResult.Loading -> {
                        // Optionnel: Afficher un loading indicator
                    }

                    is NetworkResult.Success -> {
                        val piston = result.data
                        val status = if (piston.state == Constants.STATE_ACTIVE)
                            "activé ✅" else "désactivé ❌"

                        Toast.makeText(
                            this@ValveManagementActivity,
                            "Piston ${piston.pistonNumber} $status",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    is NetworkResult.Error -> {
                        Toast.makeText(
                            this@ValveManagementActivity,
                            result.message,
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    null -> {
                        // Pas d'état de contrôle
                    }
                }
            }
        }
    }

    /**
     * Configurer le WebSocket pour les mises à jour en temps réel
     */
    private fun setupWebSocket() {
        webSocketManager = WebSocketManager.getInstance(this)

        // Écouter les mises à jour de pistons
        webSocketManager.addPistonUpdateListener { message ->
            // Vérifier si c'est pour notre appareil
            if (message.deviceId == deviceId) {
                lifecycleScope.launch(Dispatchers.Main) {
                    // Mettre à jour l'UI du piston
                    updatePistonUI(message.pistonNumber, message.state)

                    Toast.makeText(
                        this@ValveManagementActivity,
                        "Piston ${message.pistonNumber} mis à jour",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    /**
     * Afficher le dialogue de confirmation
     */
    private fun showConfirmationDialog(pistonNumber: Int) {
        val piston = viewModel.getPiston(pistonNumber)
        if (piston == null) {
            Toast.makeText(this, "Piston non trouvé", Toast.LENGTH_SHORT).show()
            return
        }

        val action = if (piston.state == Constants.STATE_ACTIVE)
            "désactiver" else "activer"

        AlertDialog.Builder(this)
            .setTitle("Confirmation")
            .setMessage("Voulez-vous vraiment $action le Piston $pistonNumber ?")
            .setPositiveButton("Oui") { _, _ ->
                viewModel.togglePiston(pistonNumber, piston.state)
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    /**
     * Mettre à jour l'UI d'un piston
     */
    private fun updatePistonUI(pistonNumber: Int, state: String) {
        val views = valveViews[pistonNumber] ?: return
        val (card, icon) = views

        val isActive = state == Constants.STATE_ACTIVE

        if (isActive) {
            // Piston activé: vert avec toggle ON
            card.setCardBackgroundColor(getColor(R.color.green))
            icon.setImageResource(R.drawable.ic_toggle_on)
        } else {
            // Piston désactivé: rouge avec toggle OFF
            card.setCardBackgroundColor(getColor(android.R.color.holo_red_light))
            icon.setImageResource(R.drawable.ic_toggle_off)
        }
    }

    override fun onResume() {
        super.onResume()
        // Rafraîchir l'appareil quand on revient sur l'écran
        deviceId?.let { viewModel.refreshDevice() }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.resetControlState()
    }
}
