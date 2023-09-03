package ru.netology.neworkapplication.adapter.events

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.netology.neworkapplication.R
import ru.netology.neworkapplication.dto.UserPreview

class ParticipantsAdapter(private val context: Context) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_ITEM = 1
    }

    private val participants = mutableListOf<UserPreview>()


    @SuppressLint("NotifyDataSetChanged")
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
            TYPE_ITEM -> ParticipantViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(
                        R.layout.item_participant,
                        parent,
                        false
                    )
            )
            else -> throw IllegalArgumentException(
                context.getString(
                    R.string.unknown_item_type_error,
                    viewType
                )
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ParticipantViewHolder && position > 0 && position <= participants.size) {
            val participant = participants[position - 1]
            holder.textView.text = participant.name
            Glide.with(holder.avatarImageView.context)
                .load(participant.avatar)
                .into(holder.avatarImageView)
        } else if (holder is HeaderViewHolder) {
            holder.headerText.text = context.getString(R.string.participants_header)
        }
    }

    override fun getItemCount(): Int = participants.size + 1

    class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val headerText: TextView = view.findViewById(R.id.headerText)
    }

    class ParticipantViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.participantName)
        val avatarImageView: ImageView = view.findViewById(R.id.participantAvatar)
    }
}
