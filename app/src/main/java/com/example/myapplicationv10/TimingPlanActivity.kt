package com.example.myapplicationv10

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.myapplicationv10.databinding.ActivityTimingPlanBinding
import com.example.myapplicationv10.model.CreateScheduleRequest
import com.example.myapplicationv10.network.NetworkResult
import com.example.myapplicationv10.repository.DeviceRepository
import com.example.myapplicationv10.repository.ScheduleRepository
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class TimingPlanActivity : BaseActivity() {

    private lateinit var binding: ActivityTimingPlanBinding
    private val calendarStart = Calendar.getInstance()
    private val calendarEnd = Calendar.getInstance()

    private lateinit var scheduleRepository: ScheduleRepository
    private lateinit var deviceRepository: DeviceRepository

    private var selectedPistonNumber: Int? = null
    private var deviceId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTimingPlanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        scheduleRepository = ScheduleRepository(this)
        deviceRepository = DeviceRepository(this)

        // Récupérer le device ID de l'utilisateur
        loadUserDevice()

        // Segmented Button
        binding.segmentTop.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    binding.segVanne.id -> {
                        binding.sectionVannes.visibility = android.view.View.VISIBLE
                        binding.sectionStart.visibility = android.view.View.GONE
                        binding.sectionEnd.visibility = android.view.View.GONE
                    }
                    binding.segStart.id -> {
                        binding.sectionVannes.visibility = android.view.View.GONE
                        binding.sectionStart.visibility = android.view.View.VISIBLE
                        binding.sectionEnd.visibility = android.view.View.GONE
                    }
                    binding.segEnd.id -> {
                        binding.sectionVannes.visibility = android.view.View.GONE
                        binding.sectionStart.visibility = android.view.View.GONE
                        binding.sectionEnd.visibility = android.view.View.VISIBLE
                    }
                }
            }
        }

        // Boutons vannes
        val vanneButtons = listOf(
            binding.v1, binding.v2, binding.v3, binding.v4,
            binding.v5, binding.v6, binding.v7, binding.v8
        )
        vanneButtons.forEachIndexed { index, button ->
            button.setOnClickListener {
                selectedPistonNumber = index + 1
                highlightSelectedVanne(button, vanneButtons)
                Toast.makeText(this, "Vanne ${button.text} sélectionnée", Toast.LENGTH_SHORT).show()
            }
        }

        // Date + heure début
        binding.pickStart.setOnClickListener {
            pickDateTime(calendarStart) { formatted ->
                binding.startSelected.text = formatted
            }
        }

        // Date + heure fin
        binding.pickEnd.setOnClickListener {
            pickDateTime(calendarEnd) { formatted ->
                binding.endSelected.text = formatted
            }
        }

        // Bouton Appliquer le planning
        binding.btnApplySchedule.setOnClickListener {
            applySchedule()
        }
    }

    private fun loadUserDevice() {
        lifecycleScope.launch {
            when (val result = deviceRepository.getDevices()) {
                is NetworkResult.Success -> {
                    if (result.data.isNotEmpty()) {
                        deviceId = result.data[0].id
                        Log.d("TimingPlan", "Device ID loaded: $deviceId")
                    } else {
                        Toast.makeText(
                            this@TimingPlanActivity,
                            "Aucun appareil trouvé. Veuillez d'abord configurer un appareil.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
                is NetworkResult.Error -> {
                    Toast.makeText(
                        this@TimingPlanActivity,
                        "Erreur lors du chargement des appareils: ${result.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                else -> {}
            }
        }
    }

    private fun highlightSelectedVanne(selected: MaterialButton, allButtons: List<MaterialButton>) {
        allButtons.forEach { it.isChecked = false }
        selected.isChecked = true
    }

    private fun applySchedule() {
        // Validation
        if (deviceId == null) {
            Toast.makeText(this, "Chargement de l'appareil en cours...", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedPistonNumber == null) {
            Toast.makeText(this, "Veuillez sélectionner une vanne", Toast.LENGTH_SHORT).show()
            return
        }

        if (binding.startSelected.text == "Aucune date/heure sélectionnée") {
            Toast.makeText(this, "Veuillez sélectionner une date de début", Toast.LENGTH_SHORT).show()
            return
        }

        if (binding.endSelected.text == "Aucune date/heure sélectionnée") {
            Toast.makeText(this, "Veuillez sélectionner une date de fin", Toast.LENGTH_SHORT).show()
            return
        }

        // Créer deux plannings: un pour activer, un pour désactiver
        createSchedules()
    }

    private fun createSchedules() {
        val pistonNum = selectedPistonNumber ?: return
        val devId = deviceId ?: return

        lifecycleScope.launch {
            // Convertir les calendriers en expressions cron (format: minute hour day month dayOfWeek)
            val cronStart = calendarToCron(calendarStart)
            val cronEnd = calendarToCron(calendarEnd)

            // Planning pour activer (START)
            val activateRequest = CreateScheduleRequest(
                name = "Activation Vanne $pistonNum",
                deviceId = devId,
                pistonNumber = pistonNum,
                action = "ACTIVATE",
                cronExpression = cronStart,
                enabled = true
            )

            // Planning pour désactiver (END)
            val deactivateRequest = CreateScheduleRequest(
                name = "Désactivation Vanne $pistonNum",
                deviceId = devId,
                pistonNumber = pistonNum,
                action = "DEACTIVATE",
                cronExpression = cronEnd,
                enabled = true
            )

            Log.d("TimingPlan", "Creating ACTIVATE schedule: $activateRequest")
            Log.d("TimingPlan", "Creating DEACTIVATE schedule: $deactivateRequest")

            // Créer le premier planning (activation)
            when (val result1 = scheduleRepository.createSchedule(activateRequest)) {
                is NetworkResult.Success -> {
                    Log.i("TimingPlan", "Schedule d'activation créé avec succès: ${result1.data.id}")

                    // Créer le deuxième planning (désactivation)
                    when (val result2 = scheduleRepository.createSchedule(deactivateRequest)) {
                        is NetworkResult.Success -> {
                            Log.i("TimingPlan", "Schedule de désactivation créé avec succès: ${result2.data.id}")
                            Toast.makeText(
                                this@TimingPlanActivity,
                                "Plannings créés avec succès!",
                                Toast.LENGTH_LONG
                            ).show()
                            finish()
                        }
                        is NetworkResult.Error -> {
                            Log.e("TimingPlan", "Erreur création désactivation: ${result2.message}")
                            Toast.makeText(
                                this@TimingPlanActivity,
                                "Erreur: ${result2.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        else -> {}
                    }
                }
                is NetworkResult.Error -> {
                    Log.e("TimingPlan", "Erreur création activation: ${result1.message}")
                    Toast.makeText(
                        this@TimingPlanActivity,
                        "Erreur: ${result1.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                else -> {}
            }
        }
    }

    /**
     * Convertir un Calendar en expression cron
     * Format cron: "minute hour day month dayOfWeek year"
     * Exemple: "30 14 15 12 ? 2024" = 15 décembre 2024 à 14:30
     */
    private fun calendarToCron(calendar: Calendar): String {
        val minute = calendar.get(Calendar.MINUTE)
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val month = calendar.get(Calendar.MONTH) + 1 // Calendar.MONTH is 0-based
        val year = calendar.get(Calendar.YEAR)

        // Format Quartz: "second minute hour dayOfMonth month dayOfWeek year"
        return "0 $minute $hour $day $month ? $year"
    }

    private fun pickDateTime(calendar: Calendar, callback: (String) -> Unit) {
        DatePickerDialog(this, { _, year, month, day ->
            calendar.set(year, month, day)
            TimePickerDialog(this, { _, hour, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, minute)
                val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                callback(sdf.format(calendar.time))
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }
}
