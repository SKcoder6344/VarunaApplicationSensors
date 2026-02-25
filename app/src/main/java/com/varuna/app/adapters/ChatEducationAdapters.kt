package com.varuna.app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.varuna.app.R
import com.varuna.app.model.ChatMessage
import com.varuna.app.model.EducationItem
import java.text.SimpleDateFormat
import java.util.Locale

// =================== CHAT ADAPTER ===================
class ChatAdapter(private val messages: List<ChatMessage>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_USER = 0
        private const val VIEW_TYPE_BOT = 1
    }

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvMessage: TextView = itemView.findViewById(R.id.tv_message)
        val tvTime: TextView = itemView.findViewById(R.id.tv_time)
    }

    inner class BotViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvMessage: TextView = itemView.findViewById(R.id.tv_message)
        val tvTime: TextView = itemView.findViewById(R.id.tv_time)
    }

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].isBot) VIEW_TYPE_BOT else VIEW_TYPE_USER
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_BOT) {
            BotViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.item_chat_bot, parent, false)
            )
        } else {
            UserViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.item_chat_user, parent, false)
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val msg = messages[position]
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        val timeStr = sdf.format(msg.timestamp)

        when (holder) {
            is BotViewHolder -> {
                holder.tvMessage.text = msg.text
                holder.tvTime.text = timeStr
            }
            is UserViewHolder -> {
                holder.tvMessage.text = msg.text
                holder.tvTime.text = timeStr
            }
        }
    }

    override fun getItemCount() = messages.size
}


// =================== EDUCATION ADAPTER ===================
class EducationAdapter(private val items: List<EducationItem>) :
    RecyclerView.Adapter<EducationAdapter.EduViewHolder>() {

    class EduViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tv_edu_title)
        val tvContent: TextView = itemView.findViewById(R.id.tv_edu_content)
        val tvExpand: TextView = itemView.findViewById(R.id.tv_expand)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EduViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_education, parent, false)
        return EduViewHolder(view)
    }

    override fun onBindViewHolder(holder: EduViewHolder, position: Int) {
        val item = items[position]
        holder.tvTitle.text = item.title
        holder.tvContent.text = item.content
        holder.tvContent.visibility = View.GONE

        holder.tvExpand.setOnClickListener {
            val isExpanded = holder.tvContent.visibility == View.VISIBLE
            holder.tvContent.visibility = if (isExpanded) View.GONE else View.VISIBLE
            holder.tvExpand.text = if (isExpanded) "▼ Show more" else "▲ Show less"
        }

        holder.tvTitle.setOnClickListener {
            val isExpanded = holder.tvContent.visibility == View.VISIBLE
            holder.tvContent.visibility = if (isExpanded) View.GONE else View.VISIBLE
            holder.tvExpand.text = if (isExpanded) "▼ Show more" else "▲ Show less"
        }
    }

    override fun getItemCount() = items.size
}
