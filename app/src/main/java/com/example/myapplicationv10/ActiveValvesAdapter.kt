package com.example.myapplicationv10

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

class ActiveValvesAdapter(private var valves: List<DashboardActivity.Valve>) :
    RecyclerView.Adapter<ActiveValvesAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val valveIndicator: CardView = view.findViewById(R.id.valveIndicator)
        val valveStatusIcon: ImageView = view.findViewById(R.id.valveStatusIcon)
        val valveName: TextView = view.findViewById(R.id.valveNameText)
        val valveStatus: TextView = view.findViewById(R.id.valveStatusText)
        val valveTime: TextView = view.findViewById(R.id.valveTimeText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_active_valve, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val valve = valves[position]
        val context = holder.itemView.context

        holder.valveName.text = valve.name
        holder.valveStatus.text = "Active"
        holder.valveTime.text = valve.lastChanged

        // Set green background for active valves
        holder.valveIndicator.setCardBackgroundColor(context.getColor(R.color.green))

        // Set water icon
        holder.valveStatusIcon.setImageResource(R.drawable.ic_water)
        holder.valveStatusIcon.clearColorFilter()

        // Set green color for active status text
        holder.valveStatus.setTextColor(context.getColor(R.color.green))
    }

    override fun getItemCount() = valves.size

    /**
     * Update the list of valves and refresh the adapter
     */
    fun updateValves(newValves: List<DashboardActivity.Valve>) {
        valves = newValves
        notifyDataSetChanged()
    }
}