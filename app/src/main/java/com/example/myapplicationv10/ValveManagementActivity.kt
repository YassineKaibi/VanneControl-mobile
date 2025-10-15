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
    }

    private fun setupBackButton() {
        findViewById<ImageView>(R.id.backButton).setOnClickListener {
            finish() // Retour au Dashboard
        }
    }

    private fun setupValveControls() {
        // Configuration des 8 valves
        for (i in 1..8) {
            val valve = valves[i - 1]
            val cardId = resources.getIdentifier("valve${i}Card", "id", packageName)
            val card = findViewById<CardView>(cardId)

            // Définir la couleur initiale
            updateValveColor(card, valve.isOpen)

            // Gérer le clic
            card.setOnClickListener {
                showConfirmationDialog(valve, card)
            }
        }
    }

    private fun showConfirmationDialog(valve: Valve, card: CardView) {
        val action = if (valve.isOpen) "fermer" else "ouvrir"
        val actionCapital = if (valve.isOpen) "Fermer" else "Ouvrir"

        AlertDialog.Builder(this)
            .setTitle("Confirmation")
            .setMessage("Voulez-vous vraiment $action ${valve.name} ?")
            .setPositiveButton("Oui") { _, _ ->
                toggleValve(valve, card)
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    private fun toggleValve(valve: Valve, card: CardView) {
        // Changer l'état de la valve
        valve.isOpen = !valve.isOpen

        // Mettre à jour la couleur
        updateValveColor(card, valve.isOpen)

        // Afficher un message de confirmation
        val status = if (valve.isOpen) "ouverte ✅" else "fermée ❌"
        val message = "${valve.name} est maintenant $status"
        Snackbar.make(card, message, Snackbar.LENGTH_SHORT).show()

        // TODO: Envoyer la commande au backend/MQTT
        // sendValveCommand(valve.id, valve.isOpen)
    }

    private fun updateValveColor(card: CardView, isOpen: Boolean) {
        val color = if (isOpen) {
            getColor(R.color.green)  // Vert = ouverte
        } else {
            getColor(android.R.color.holo_red_light)  // Rouge = fermée
        }
        card.setCardBackgroundColor(color)
    }

    // Fonction pour envoyer la commande (à implémenter plus tard)
    private fun sendValveCommand(valveId: Int, isOpen: Boolean) {
        // TODO: Intégration avec MQTT/API backend
        // Exemple:
        // mqttClient.publish("devices/${deviceId}/commands",
        //     json { "valve" to valveId, "action" to if(isOpen) "open" else "close" })
    }
}