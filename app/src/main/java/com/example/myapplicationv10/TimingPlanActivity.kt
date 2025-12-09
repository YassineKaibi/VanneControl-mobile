package com.example.myapplicationv10

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.Toast
import com.example.myapplicationv10.databinding.ActivityTimingPlanBinding
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.*

class TimingPlanActivity : BaseActivity() {

    private lateinit var binding: ActivityTimingPlanBinding

    private var selectedValveNumber: Int? = null
    private var selectedAction: String? = null
    private var selectedDateTime: Calendar? = null

    private val dateTimeFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTimingPlanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupVanneButtons()
        setupActionButtons()
        setupDateTimePicker()
        setupSaveButton()
    }

    private fun setupVanneButtons() {
        val vanneButtons = listOf(
            binding.btnVanne1, binding.btnVanne2, binding.btnVanne3, binding.btnVanne4,
            binding.btnVanne5, binding.btnVanne6, binding.btnVanne7, binding.btnVanne8
        )

        vanneButtons.forEachIndexed { index, button ->
            button.setOnClickListener {
                // Désélectionner tous les boutons
                vanneButtons.forEach { it.isChecked = false }

                // Sélectionner le bouton cliqué
                button.isChecked = true
                selectedValveNumber = index + 1
            }
        }
    }

    private fun setupActionButtons() {
        binding.btnActionOpen.setOnClickListener {
            binding.btnActionOpen.isChecked = true
            binding.btnActionClose.isChecked = false
            selectedAction = "OPEN"
        }

        binding.btnActionClose.setOnClickListener {
            binding.btnActionClose.isChecked = true
            binding.btnActionOpen.isChecked = false
            selectedAction = "CLOSE"
        }
    }

    private fun setupDateTimePicker() {
        binding.etDateTime.setOnClickListener {
            showDatePicker()
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()

        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                // Afficher le TimePickerDialog après la sélection de la date
                showTimePicker(calendar)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        // Ne pas permettre de sélectionner une date passée
        datePickerDialog.datePicker.minDate = System.currentTimeMillis()
        datePickerDialog.show()
    }

    private fun showTimePicker(calendar: Calendar) {
        val timePickerDialog = TimePickerDialog(
            this,
            { _, hourOfDay, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)
                calendar.set(Calendar.SECOND, 0)

                selectedDateTime = calendar
                binding.etDateTime.setText(dateTimeFormat.format(calendar.time))
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true // Format 24h
        )

        timePickerDialog.show()
    }

    private fun setupSaveButton() {
        binding.btnSave.setOnClickListener {
            if (validateInputs()) {
                saveTiming()
            }
        }
    }

    private fun validateInputs(): Boolean {
        if (selectedValveNumber == null) {
            Toast.makeText(this, "Veuillez sélectionner une vanne", Toast.LENGTH_SHORT).show()
            return false
        }

        if (selectedAction == null) {
            Toast.makeText(this, "Veuillez sélectionner une action", Toast.LENGTH_SHORT).show()
            return false
        }

        if (selectedDateTime == null) {
            Toast.makeText(this, "Veuillez sélectionner la date et l'heure", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun saveTiming() {
        // TODO: Implémenter la sauvegarde dans la base de données ou l'envoi au serveur

        val message = """
            Plan enregistré:
            Vanne: $selectedValveNumber
            Action: $selectedAction
            Date/Heure: ${dateTimeFormat.format(selectedDateTime!!.time)}
        """.trimIndent()

        Toast.makeText(this, message, Toast.LENGTH_LONG).show()

        // Retour à l'activité précédente
        finish()
    }
}