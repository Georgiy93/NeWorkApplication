package ru.netology.neworkapplication.adapter.events


import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.URLUtil
import android.widget.PopupMenu
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.netology.neworkapplication.R
import ru.netology.neworkapplication.auth.AppAuth
import ru.netology.neworkapplication.databinding.CardEventBinding
import ru.netology.neworkapplication.dto.Event
import ru.netology.neworkapplication.dto.FeedItemEvent
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
    private val context: Context,
    private val onInteractionListener: OnInteractionListenerEvent,
    private val appAuth: AppAuth

) : PagingDataAdapter<FeedItemEvent, RecyclerView.ViewHolder>(EventsDiffCallback()) {
    override fun getItemViewType(position: Int): Int =
        when (getItem(position)) {

            is Event -> R.layout.card_event
            null -> error(R.string.unknown_item_type)
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            R.layout.card_event -> {
                val binding =
                    CardEventBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return EventsViewHolder(context, binding, onInteractionListener, appAuth)
            }

            else -> error(context.getString(R.string.unknown_item_type_error, viewType))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {

            is Event -> (holder as? EventsViewHolder)?.bind(item)
            null -> error(R.string.unknown_item_type)
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
    private val context: Context,
    private val binding: CardEventBinding,
    private val onInteractionListener: OnInteractionListenerEvent,
    private val appAuth: AppAuth
) : RecyclerView.ViewHolder(binding.root) {
    private fun removeMicroseconds(date: String): String {
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
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
            val originalFormatWithMilliseconds =
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            val targetFormat = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault())
            originalFormatWithoutMilliseconds.timeZone =
                TimeZone.getTimeZone("UTC")
            originalFormatWithMilliseconds.timeZone =
                TimeZone.getTimeZone("UTC")

            var date = try {
                originalFormatWithMilliseconds.parse(removeMicroseconds(event.published))
            } catch (e: ParseException) {
                originalFormatWithoutMilliseconds.parse(event.published)
            }
            published.text = if (date != null) targetFormat.format(date)
            else context.getString(R.string.unknown_date)

            date = try {
                originalFormatWithMilliseconds.parse(removeMicroseconds(event.datetime))
            } catch (e: ParseException) {
                originalFormatWithoutMilliseconds.parse(event.datetime)
            }
            datetime.text = if (date != null) targetFormat.format(date) else
                context.getString(R.string.unknown_date)


//
            datetimeTitle.text = context.getString(R.string.datetime_title)
            authorJob.text = event.authorJob
            content.text = event.content
            type.text = event.type
            link.text = event.link
            URLUtil.isValidUrl(link.text.toString())


            link.setOnClickListener {
                val url = event.link
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(url)
                itemView.context.startActivity(intent)
            }
            event.authorAvatar?.let {
                Glide.with(itemView.context)
                    .load(it)
                    .placeholder(R.drawable.baseline_upload_file_24)
                    .error(R.drawable.baseline_error_outline_24)
                    .into(avatar)
            }

            if (event.attachment == null) {
                image.visibility = View.GONE
            } else {
                event.attachment.url.let {
                    Glide.with(itemView.context)
                        .load(it)
                        .placeholder(R.drawable.baseline_upload_file_24)
                        .error(R.drawable.baseline_error_outline_24)

                        .into(image)
                }
            }
            val currentUserId = try {
                appAuth.getId()
            } catch (_: Exception) {


            }

            menu.visibility = if (event.authorId == currentUserId)
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

                                onInteractionListener.onEditNavigate(event)


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
            val participantsAdapter = ParticipantsAdapter(context)
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