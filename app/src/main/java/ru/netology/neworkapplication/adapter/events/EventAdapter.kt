package ru.netology.neworkapplication.adapter.events

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.netology.neworkapplication.R
import ru.netology.neworkapplication.databinding.CardEventBinding
import ru.netology.neworkapplication.dto.Event
import ru.netology.neworkapplication.dto.FeedItemEvent
import ru.netology.neworkapplication.util.TokenManager
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

interface OnInteractionListenerEvent {

    fun onLike(event: Event) {}
    fun onEdit(event: Event) {}
    fun onRemove(event: Event) {}
    fun onEditNavigate(event: Event) {}

}

class EventsAdapter(
    private val onInteractionListener: OnInteractionListenerEvent,
    private val tokenManager: TokenManager

) : PagingDataAdapter<FeedItemEvent, RecyclerView.ViewHolder>(EventsDiffCallback()) {
    override fun getItemViewType(position: Int): Int =
        when (getItem(position)) {

            is Event -> R.layout.card_event
            null -> error("unknown item type")
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            R.layout.card_event -> {
                val binding =
                    CardEventBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return EventsViewHolder(binding, onInteractionListener, tokenManager)
            }

            else -> error("unknown item type: $viewType")
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {

            is Event -> (holder as? EventsViewHolder)?.bind(item)
            null -> error("unknown item type")
        }

    }
}


class EventsDiffCallback : DiffUtil.ItemCallback<FeedItemEvent>() {
    override fun areItemsTheSame(oldItem: FeedItemEvent, newItem: FeedItemEvent): Boolean {
        if (oldItem::class != newItem::class) {
            return false
        }
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: FeedItemEvent, newItem: FeedItemEvent): Boolean {
        return oldItem == newItem
    }
}

class EventsViewHolder(

    private val binding: CardEventBinding,
    private val onInteractionListener: OnInteractionListenerEvent,
    private val tokenManager: TokenManager
) : RecyclerView.ViewHolder(binding.root) {
    fun removeMicroseconds(date: String): String {
        val zIdx = date.lastIndexOf("Z")
        val dotIdx = date.lastIndexOf(".")
        return if (dotIdx != -1 && zIdx != -1 && zIdx - dotIdx > 4) {
            date.substring(0, dotIdx + 4) + "Z"
        } else {
            date
        }
    }

    fun bind(event: Event) {

        binding.apply {
            Glide.with(itemView.context).clear(image)
            author.text = event.author

            val originalFormatWithoutMilliseconds =
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale("ru", "RU"))
            val originalFormatWithMilliseconds =
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale("ru", "RU"))
            val targetFormat = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("ru", "RU"))
            originalFormatWithoutMilliseconds.timeZone =
                TimeZone.getTimeZone("Europe/Moscow")  // if the original time is in UTC
            originalFormatWithMilliseconds.timeZone =
                TimeZone.getTimeZone("Europe/Moscow")  // if the original time is in UTC

            var date = try {
                originalFormatWithMilliseconds.parse(removeMicroseconds(event.published))
            } catch (e: ParseException) {
                originalFormatWithoutMilliseconds.parse(event.published)
            }
            published.text = if (date != null) targetFormat.format(date) else "Unknown date"

            date = try {
                originalFormatWithMilliseconds.parse(removeMicroseconds(event.datetime))
            } catch (e: ParseException) {
                originalFormatWithoutMilliseconds.parse(event.datetime)
            }
            datetime.text = if (date != null) targetFormat.format(date) else "Unknown date"


//
            datetimeTitle.text = "Дата проведения события"
            authorJob.text = event.authorJob
            content.text = event.content
            type.text = event.type
            link.text = event.link



            link.setOnClickListener {
                val url = event.link
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(url)
                itemView.context.startActivity(intent)
            }
            event.authorAvatar?.let {
                Glide.with(itemView.context)
                    .load(it)
                    .placeholder(R.drawable.baseline_upload_file_24) // замените на ваш ресурс
                    .error(R.drawable.baseline_error_outline_24)
                    .into(avatar)
            }

            if (event.attachment == null) {
                image.visibility = View.GONE
            } else {
                event.attachment?.url?.let {
                    Glide.with(itemView.context)
                        .load(it)
                        .placeholder(R.drawable.baseline_upload_file_24) // замените на ваш ресурс
                        .error(R.drawable.baseline_error_outline_24)

                        .into(image)
                }
            }


            menu.visibility = if (event.authorId == tokenManager.getId())
                View.VISIBLE else View.INVISIBLE

            menu.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.options_post)


                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.remove -> {
                                onInteractionListener.onRemove(event)
                                true
                            }
                            R.id.edit -> {
                                if (event.authorId == tokenManager.getId()) {
                                    onInteractionListener.onEditNavigate(event)
                                }

                                true
                            }

                            else -> false
                        }
                    }
                }.show()
            }

            like.isChecked = event.likedByMe
            like.setOnClickListener {

                onInteractionListener.onLike(event)
            }
            val participantsAdapter = ParticipantsAdapter()
            val participants = event.users.values.toList()

            participantsAdapter.setParticipants(participants)

            participantsList.layoutManager = LinearLayoutManager(itemView.context)
            participantsList.adapter = participantsAdapter

            event.speakerIds
                .orEmpty().mapNotNull {
                    event.users[it]
                }
                .let(participantsAdapter::setParticipants)

        }
    }
}