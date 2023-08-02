package ru.netology.neworkapplication.adapter.events

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ru.netology.neworkapplication.R
import ru.netology.neworkapplication.databinding.CardEventBinding
import ru.netology.neworkapplication.dto.Event
import ru.netology.neworkapplication.dto.FeedItemEvent
import ru.netology.neworkapplication.util.TokenManager
import ru.netology.neworkapplication.view.loadCircleCrop
import java.text.SimpleDateFormat
import java.util.*

interface OnInteractionListener2 {

    fun onLike(event: Event) {}
    fun onEdit(event: Event) {}
    fun onRemove(event: Event) {}
    fun onEditNavigate(event: Event) {}

}

class EventsAdapter(
    private val onInteractionListener: OnInteractionListener2,
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

//
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
    private val onInteractionListener: OnInteractionListener2,
    private val tokenManager: TokenManager
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(event: Event) {

        binding.apply {
            author.text = event.author

            val originalFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.US)
            val targetFormat = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.US)
            originalFormat.timeZone = TimeZone.getTimeZone("UTC") // if the original time is in UTC
            val date = originalFormat.parse(event.published)
            val dateEvent = originalFormat.parse(event.datetime)
            published.text = if (date != null) targetFormat.format(date) else "Unknown date"
//
            datetime.text = if (date != null) targetFormat.format(dateEvent) else "Unknown date"
            authorJob.text = event.authorJob
            content.text = event.content
            avatar.loadCircleCrop("${event.authorAvatar}")
            like.isChecked = event.likedByMe

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

            like.setOnClickListener {
                like.isChecked = event.likedByMe
                onInteractionListener.onLike(event)
            }
            like.isChecked = event.likedByMe

        }
    }
}