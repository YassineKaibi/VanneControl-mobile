package com.example.myapplicationv10.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplicationv10.R
import com.example.myapplicationv10.model.ScheduleResponse
import com.google.android.material.switchmaterial.SwitchMaterial

/**
 * ScheduleAdapter - RecyclerView adapter for schedule items
 */
class ScheduleAdapter(
    private val onToggle: (ScheduleResponse, Boolean) -> Unit,
    private val onEdit: (ScheduleResponse) -> Unit,
    private val onDelete: (ScheduleResponse) -> Unit
) : ListAdapter<ScheduleResponse, ScheduleAdapter.ScheduleViewHolder>(ScheduleDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_schedule, parent, false)
        return ScheduleViewHolder(view)
    }

    override fun onBindViewHolder(holder: ScheduleViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ScheduleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvScheduleName: TextView = itemView.findViewById(R.id.tvScheduleName)
        private val tvValveInfo: TextView = itemView.findViewById(R.id.tvValveInfo)
        private val switchEnabled: SwitchMaterial = itemView.findViewById(R.id.switchEnabled)
        private val layoutOnTime: View = itemView.findViewById(R.id.layoutOnTime)
        private val tvOnTime: TextView = itemView.findViewById(R.id.tvOnTime)
        private val layoutOffTime: View = itemView.findViewById(R.id.layoutOffTime)
        private val tvOffTime: TextView = itemView.findViewById(R.id.tvOffTime)
        private val tvRepeat: TextView = itemView.findViewById(R.id.tvRepeat)
        private val btnEdit: ImageButton = itemView.findViewById(R.id.btnEdit)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)

        fun bind(schedule: ScheduleResponse) {
            tvScheduleName.text = schedule.name
            tvValveInfo.text = "Valve ${schedule.pistonNumber}"
            
            // Set switch without triggering listener
            switchEnabled.setOnCheckedChangeListener(null)
            switchEnabled.isChecked = schedule.enabled
            switchEnabled.setOnCheckedChangeListener { _, isChecked ->
                onToggle(schedule, isChecked)
            }

            // Parse cron expression to display time and repeat info
            val cronParts = schedule.cronExpression.split(" ")
            val time = formatTimeFromCron(cronParts)
            val repeat = formatRepeatFromCron(cronParts)

            // Determine if this is an ACTIVATE or DEACTIVATE schedule
            if (schedule.action == "ACTIVATE") {
                layoutOnTime.visibility = View.VISIBLE
                layoutOffTime.visibility = View.GONE
                tvOnTime.text = time
            } else {
                layoutOnTime.visibility = View.GONE
                layoutOffTime.visibility = View.VISIBLE
                tvOffTime.text = time
            }

            tvRepeat.text = repeat

            btnEdit.setOnClickListener { onEdit(schedule) }
            btnDelete.setOnClickListener { onDelete(schedule) }
        }

        /**
         * Format time from cron expression
         * Cron format: second minute hour dayOfMonth month dayOfWeek [year]
         */
        private fun formatTimeFromCron(cronParts: List<String>): String {
            return try {
                val minute = cronParts.getOrNull(1)?.toIntOrNull() ?: 0
                val hour = cronParts.getOrNull(2)?.toIntOrNull() ?: 0
                String.format("%02d:%02d", hour, minute)
            } catch (e: Exception) {
                "00:00"
            }
        }

        /**
         * Format repeat info from cron expression
         */
        private fun formatRepeatFromCron(cronParts: List<String>): String {
            return try {
                val dayOfWeek = cronParts.getOrNull(5) ?: "*"
                val dayOfMonth = cronParts.getOrNull(3) ?: "*"

                when {
                    dayOfWeek == "*" && dayOfMonth == "*" -> "Everyday"
                    dayOfWeek == "?" && dayOfMonth != "*" -> "Once"
                    dayOfWeek == "MON-FRI" || dayOfWeek == "2-6" -> "Weekdays"
                    dayOfWeek == "SAT,SUN" || dayOfWeek == "1,7" -> "Weekends"
                    dayOfWeek.contains(",") -> formatCustomDays(dayOfWeek)
                    else -> "Custom"
                }
            } catch (e: Exception) {
                "Custom"
            }
        }

        private fun formatCustomDays(dayOfWeek: String): String {
            val dayMap = mapOf(
                "1" to "Sun", "2" to "Mon", "3" to "Tue", "4" to "Wed",
                "5" to "Thu", "6" to "Fri", "7" to "Sat",
                "SUN" to "Sun", "MON" to "Mon", "TUE" to "Tue", "WED" to "Wed",
                "THU" to "Thu", "FRI" to "Fri", "SAT" to "Sat"
            )
            val days = dayOfWeek.split(",").mapNotNull { dayMap[it.trim()] }
            return if (days.isNotEmpty()) days.joinToString(", ") else "Custom"
        }
    }

    class ScheduleDiffCallback : DiffUtil.ItemCallback<ScheduleResponse>() {
        override fun areItemsTheSame(oldItem: ScheduleResponse, newItem: ScheduleResponse): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ScheduleResponse, newItem: ScheduleResponse): Boolean {
            return oldItem == newItem
        }
    }
}
