package com.example.myapplicationv10

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class HistoryAdapter(private val historyList: List<HistoryActivity.ValveAction>) :
    RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val valveIcon: ImageView = view.findViewById(R.id.valveIcon)
        val valveName: TextView = view.findViewById(R.id.valveName)
        val actionText: TextView = view.findViewById(R.id.actionText)
        val timestamp: TextView = view.findViewById(R.id.timestampText)
        val userText: TextView = view.findViewById(R.id.userText)
        val statusIndicator: View = view.findViewById(R.id.statusIndicator)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val action = historyList[position]
        val context = holder.itemView.context

        // Nom de la vanne
        holder.valveName.text = action.valveName

        // Action (Opened/Closed)
        holder.actionText.text = action.action

        // Format de la date et heure
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        holder.timestamp.text = dateFormat.format(action.timestamp)

        // Utilisateur
        holder.userText.text = "Par: ${action.user}"

        // Couleur selon l'action
        if (action.action == "Opened") {
            holder.actionText.setTextColor(context.getColor(R.color.green))
            holder.statusIndicator.setBackgroundColor(context.getColor(R.color.green))
            holder.valveIcon.setImageResource(R.drawable.ic_toggle_on)
        } else {
            holder.actionText.setTextColor(context.getColor(android.R.color.holo_red_light))
            holder.statusIndicator.setBackgroundColor(context.getColor(android.R.color.holo_red_light))
            holder.valveIcon.setImageResource(R.drawable.ic_toggle_off)
        }
    }

    override fun getItemCount() = historyList.size
}