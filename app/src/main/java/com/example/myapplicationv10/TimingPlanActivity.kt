package com.example.myapplicationv10

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
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
        val sharedPreferences = getSharedPreferences("UserProfile", Context.MODE_PRIVATE)
        val numberOfValves = sharedPreferences.getInt("numberOfValvesValue", 8)

        Log.d("TimingPlanActivity", "Number of valves: $numberOfValves")

        val allVanneButtons = listOf(
            binding.btnVanne1, binding.btnVanne2, binding.btnVanne3, binding.btnVanne4,
            binding.btnVanne5, binding.btnVanne6, binding.btnVanne7, binding.btnVanne8
        )

        val greenColor = ContextCompat.getColor(this, R.color.green)
        val blackColor = ContextCompat.getColor(this, R.color.black)
        val whiteColor = ContextCompat.getColor(this, R.color.white)

        allVanneButtons.forEachIndexed { index, button ->
            if (index < numberOfValves) {
                button.visibility = View.VISIBLE
                button.setOnClickListener {
                    allVanneButtons.take(numberOfValves).forEach {
                        it.setTextColor(blackColor)
                        it.strokeColor = ColorStateList.valueOf(blackColor)
                        it.setBackgroundColor(android.graphics.Color.TRANSPARENT)
                    }

                    button.setTextColor(whiteColor)
                    button.strokeColor = ColorStateList.valueOf(greenColor)
                    button.setBackgroundColor(greenColor)
                    selectedValveNumber = index + 1
                }
            } else {
                button.visibility = View.GONE
            }
        }
    }

    private fun setupActionButtons() {
        val greenColor = ContextCompat.getColor(this, R.color.green)
        val blackColor = ContextCompat.getColor(this, R.color.black)
        val whiteColor = ContextCompat.getColor(this, R.color.white)

        binding.btnActionOpen.setOnClickListener {
            binding.btnActionOpen.setTextColor(whiteColor)
            binding.btnActionOpen.strokeColor = ColorStateList.valueOf(greenColor)
            binding.btnActionOpen.setBackgroundColor(greenColor)

            binding.btnActionClose.setTextColor(blackColor)
            binding.btnActionClose.strokeColor = ColorStateList.valueOf(blackColor)
            binding.btnActionClose.setBackgroundColor(android.graphics.Color.TRANSPARENT)

            selectedAction = "OPEN"
        }

        binding.btnActionClose.setOnClickListener {
            binding.btnActionClose.setTextColor(whiteColor)
            binding.btnActionClose.strokeColor = ColorStateList.valueOf(greenColor)
            binding.btnActionClose.setBackgroundColor(greenColor)

            binding.btnActionOpen.setTextColor(blackColor)
            binding.btnActionOpen.strokeColor = ColorStateList.valueOf(blackColor)
            binding.btnActionOpen.setBackgroundColor(android.graphics.Color.TRANSPARENT)

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
                showTimePicker(calendar)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

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
            true
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
        val message = """
            Plan enregistré:
            Vanne: $selectedValveNumber
            Action: $selectedAction
            Date/Heure: ${dateTimeFormat.format(selectedDateTime!!.time)}
        """.trimIndent()

        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        finish()
    }
}