package ru.netology.neworkapplication.adapter.events

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ru.netology.neworkapplication.R
import ru.netology.neworkapplication.dto.UserPreview

class ParticipantsAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_ITEM = 1
    }

    private val participants = mutableListOf<UserPreview>()

    fun setParticipants(participantList: List<UserPreview>) {
        participants.clear()
        participants.addAll(participantList)
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) TYPE_HEADER else TYPE_ITEM
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_HEADER -> HeaderViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.header_participants, parent, false)
            )
            else -> ParticipantViewHolder(TextView(parent.context))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ParticipantViewHolder && position > 0) {
            val participant = participants[position - 1]
            holder.textView.text = participant.name
        } else if (holder is HeaderViewHolder) {
            holder.headerText.text = "Participants"
        }
    }

    override fun getItemCount(): Int = participants.size + 1

    class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val headerText: TextView = view.findViewById(R.id.headerText)
    }

    class ParticipantViewHolder(val textView: TextView) : RecyclerView.ViewHolder(textView)
}
