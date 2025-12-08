package com.example.myapplicationv10

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplicationv10.databinding.ActivityTimingPlanBinding
import java.text.SimpleDateFormat
import java.util.*

class TimingPlanActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTimingPlanBinding
    private val calendarStart = Calendar.getInstance()
    private val calendarEnd = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTimingPlanBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
        vanneButtons.forEach { button ->
            button.setOnClickListener {
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
    }

    private fun pickDateTime(calendar: Calendar, callback: (String) -> Unit) {
        val dateListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            val timeListener = TimePickerDialog.OnTimeSetListener { _, hour, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, minute)
                val format = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                callback(format.format(calendar.time))
            }

            TimePickerDialog(
                this,
                timeListener,
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            ).show()
        }

        DatePickerDialog(
            this,
            dateListener,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }
}
