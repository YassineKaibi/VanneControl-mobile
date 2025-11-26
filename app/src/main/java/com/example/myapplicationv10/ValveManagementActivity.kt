package com.example.myapplicationv10

import android.app.AlertDialog
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.snackbar.Snackbar

class ValveManagementActivity : AppCompatActivity() {

    // Data class pour les valves
    data class Valve(
        val id: Int,
        val name: String,
        var isOpen: Boolean
    )

    // Liste des 8 valves avec leurs états initiaux
    private val valves = mutableListOf(
        Valve(1, "Valve 1", true),   // Vert (ouverte)
        Valve(2, "Valve 2", false),  // Rouge (fermée)
        Valve(3, "Valve 3", true),   // Vert (ouverte)
        Valve(4, "Valve 4", false),  // Rouge (fermée)
        Valve(5, "Valve 5", true),   // Vert (ouverte)
        Valve(6, "Valve 6", false),  // Rouge (fermée)
        Valve(7, "Valve 7", true),   // Vert (ouverte)
        Valve(8, "Valve 8", false)   // Rouge (fermée)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_valve_management)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.topBar)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupBackButton()
        setupValveControls()

        // Charger l'état initial des valves depuis le backend
        loadInitialValveStates()
    }

    private fun setupBackButton() {
        findViewById<ImageView>(R.id.backButton).setOnClickListener {
            finish()
        }
    }

    private fun setupValveControls() {
        // Configuration des 8 valves
        for (i in 1..8) {
            val valve = valves[i - 1]
            val cardId = resources.getIdentifier("valve${i}Card", "id", packageName)
            val iconId = resources.getIdentifier("valve${i}Icon", "id", packageName)

            val card = findViewById<CardView>(cardId)
            val icon = findViewById<ImageView>(iconId)

            // Définir la couleur et l'icône initiale
            updateValveAppearance(card, icon, valve.isOpen)

            // Gérer le clic
            card.setOnClickListener {
                showConfirmationDialog(valve, card, icon)
            }
        }
    }

    private fun loadInitialValveStates() {
        // Charger les états des valves depuis le backend en arrière-plan
        runOnNetwork {
            try {
                // TODO: Remplacer par un vrai appel API
                // val states = apiClient.getValveStates()

                // Simuler une requête réseau
                Thread.sleep(500)

                // Mettre à jour l'UI sur le thread principal
                runOnUiThread {
                    // TODO: Mettre à jour les valves avec les données réelles
                    // valves.forEachIndexed { index, valve ->
                    //     valve.isOpen = states[index]
                    //     updateValveUI(index)
                    // }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    Snackbar.make(
                        findViewById(android.R.id.content),
                        "Erreur de chargement des états",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun showConfirmationDialog(valve: Valve, card: CardView, icon: ImageView) {
        val action = if (valve.isOpen) "fermer" else "ouvrir"

        AlertDialog.Builder(this)
            .setTitle("Confirmation")
            .setMessage("Voulez-vous vraiment $action ${valve.name} ?")
            .setPositiveButton("Oui") { _, _ ->
                toggleValve(valve, card, icon)
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    private fun toggleValve(valve: Valve, card: CardView, icon: ImageView) {
        // Changer l'état de la valve localement
        valve.isOpen = !valve.isOpen

        // Mettre à jour l'UI immédiatement sur le thread principal
        runOnUiThread {
            updateValveAppearance(card, icon, valve.isOpen)

            val status = if (valve.isOpen) "ouverte ✅" else "fermée ❌"
            val message = "${valve.name} est maintenant $status"
            Snackbar.make(card, message, Snackbar.LENGTH_SHORT).show()
        }

        // Envoyer la commande au backend/MQTT en arrière-plan
        runOnNetwork {
            sendValveCommand(valve.id, valve.isOpen)
        }
    }

    private fun updateValveAppearance(card: CardView, icon: ImageView, isOpen: Boolean) {
        if (isOpen) {
            // Valve ouverte: vert avec toggle ON
            card.setCardBackgroundColor(getColor(R.color.green))
            icon.setImageResource(R.drawable.ic_toggle_on)
        } else {
            // Valve fermée: rouge avec toggle OFF
            card.setCardBackgroundColor(getColor(android.R.color.holo_red_light))
            icon.setImageResource(R.drawable.ic_toggle_off)
        }
    }

    // Fonction pour envoyer la commande (avec multithreading)
    private fun sendValveCommand(valveId: Int, isOpen: Boolean) {
        try {
            // TODO: Intégration avec MQTT/API backend
            // Exemple d'implémentation:

            // 1. Préparer le payload
            val action = if (isOpen) "open" else "close"
            val payload = """{"valve": $valveId, "action": "$action"}"""

            // 2. Envoyer via MQTT (déjà sur un thread réseau)
            // mqttClient.publish("devices/${deviceId}/commands", payload)

            // 3. Simuler l'envoi (à remplacer)
            Thread.sleep(200) // Simuler délai réseau

            // 4. Confirmation sur le thread principal
            runOnUiThread {
                // Log ou notification de succès
                println("✅ Commande envoyée: Valve $valveId -> $action")
            }

            // 5. Sauvegarder dans la base de données
            runOnDatabase {
                saveValveStateToDatabase(valveId, isOpen)
            }

        } catch (e: Exception) {
            e.printStackTrace()

            // En cas d'erreur, restaurer l'état précédent
            runOnUiThread {
                val valve = valves.find { it.id == valveId }
                valve?.let {
                    it.isOpen = !isOpen // Restaurer l'état précédent

                    // Mettre à jour l'UI
                    val cardId = resources.getIdentifier("valve${valveId}Card", "id", packageName)
                    val iconId = resources.getIdentifier("valve${valveId}Icon", "id", packageName)
                    val card = findViewById<CardView>(cardId)
                    val icon = findViewById<ImageView>(iconId)
                    updateValveAppearance(card, icon, it.isOpen)

                    Snackbar.make(
                        findViewById(android.R.id.content),
                        "Erreur: Impossible de contrôler la vanne",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun saveValveStateToDatabase(valveId: Int, isOpen: Boolean) {
        try {
            // TODO: Implémenter la sauvegarde en base de données
            // Exemple:
            // val timestamp = System.currentTimeMillis()
            // database.valveHistoryDao().insert(
            //     ValveHistory(
            //         valveId = valveId,
            //         action = if(isOpen) "opened" else "closed",
            //         timestamp = timestamp,
            //         user = "current_user"
            //     )
            // )

            println("💾 État sauvegardé: Valve $valveId -> ${if(isOpen) "open" else "closed"}")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Les threads seront nettoyés automatiquement par ThreadManager
    }
}