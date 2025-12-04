package com.example.myapplicationv10

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ActiveValvesAdapter(private var valves: List<DashboardActivity.Valve>) :
    RecyclerView.Adapter<ActiveValvesAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
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
        holder.valveName.text = valve.name
        holder.valveStatus.text = "Active"
        holder.valveTime.text = valve.lastChanged

        // Set green color for active status
        val context = holder.itemView.context
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