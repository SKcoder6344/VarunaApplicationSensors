package com.varuna.app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.varuna.app.R
import com.varuna.app.models.AlertModel
import java.text.SimpleDateFormat
import java.util.Locale

// =================== ALERT ADAPTER ===================
class AlertAdapter(private var alerts: List<AlertModel>) :
    RecyclerView.Adapter<AlertAdapter.AlertViewHolder>() {

    class AlertViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvType: TextView = itemView.findViewById(R.id.tv_alert_type)
        val tvMessage: TextView = itemView.findViewById(R.id.tv_alert_message)
        val tvVillage: TextView = itemView.findViewById(R.id.tv_alert_village)
        val tvSeverity: TextView = itemView.findViewById(R.id.tv_alert_severity)
        val tvTimestamp: TextView = itemView.findViewById(R.id.tv_alert_timestamp)
        val cardView: CardView = itemView.findViewById(R.id.card_alert)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlertViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_alert, parent, false)
        return AlertViewHolder(view)
    }

    override fun onBindViewHolder(holder: AlertViewHolder, position: Int) {
        val alert = alerts[position]
        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())

        holder.tvType.text = alert.type
        holder.tvMessage.text = alert.message
        holder.tvVillage.text = "📍 ${alert.village}"
        holder.tvSeverity.text = alert.severity
        holder.tvTimestamp.text = sdf.format(alert.timestamp)

        val bgColor = when (alert.severity) {
            "High" -> 0xFFFFEBEE.toInt()
            "Medium" -> 0xFFFFF8E1.toInt()
            else -> 0xFFE8F5E9.toInt()
        }
        holder.cardView.setCardBackgroundColor(bgColor)

        val severityColor = when (alert.severity) {
            "High" -> 0xFFB71C1C.toInt()
            "Medium" -> 0xFFF57F17.toInt()
            else -> 0xFF2E7D32.toInt()
        }
        holder.tvSeverity.setTextColor(severityColor)

        // Bold unread
        if (!alert.isRead) {
            holder.tvType.setTypeface(null, android.graphics.Typeface.BOLD)
        }
    }

    override fun getItemCount() = alerts.size

    fun updateData(newList: List<AlertModel>) {
        alerts = newList
        notifyDataSetChanged()
    }
}


// =================== CHAT MESSAGE MODEL ===================

