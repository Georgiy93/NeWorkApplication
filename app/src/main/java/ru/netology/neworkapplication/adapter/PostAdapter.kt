package ru.netology.neworkapplication.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.URLUtil
import android.widget.PopupMenu

import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

import ru.netology.neworkapplication.R
import ru.netology.neworkapplication.auth.AppAuth

import ru.netology.neworkapplication.databinding.CardPostBinding
import ru.netology.neworkapplication.dto.FeedItem

import ru.netology.neworkapplication.dto.Post


import java.text.SimpleDateFormat
import java.util.*

interface OnInteractionListener {

    fun onLike(post: Post) {}
    fun onEdit(post: Post) {}
    fun onRemove(post: Post) {}
    fun onEditNavigate(post: Post) {}

}

class PostsAdapter(
    private val onInteractionListener: OnInteractionListener,
    private val appAuth: AppAuth,
    private val context: Context,
) : PagingDataAdapter<FeedItem, RecyclerView.ViewHolder>(PostDiffCallback()) {
    override fun getItemViewType(position: Int): Int =
        when (getItem(position)) {

            is Post -> R.layout.card_post
            null -> error(R.string.unknown_item_type)
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            R.layout.card_post -> {
                val binding =
                    CardPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return PostViewHolder(context, binding, onInteractionListener, appAuth)
            }

            else -> error(context.getString(R.string.unknown_item_type_error, viewType))
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {

            is Post -> (holder as? PostViewHolder)?.bind(item)
            null -> error(R.string.unknown_item_type)
        }

    }
}

class PostDiffCallback : DiffUtil.ItemCallback<FeedItem>() {
    override fun areItemsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean {
        if (oldItem::class != newItem::class) {
            return false
        }
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean {
        return oldItem == newItem
    }
}

class PostViewHolder(
    private val context: Context,
    private val binding: CardPostBinding,
    private val onInteractionListener: OnInteractionListener,
    private val appAuth: AppAuth,

    ) : RecyclerView.ViewHolder(binding.root) {

    fun bind(post: Post) {

        binding.apply {
            Glide.with(itemView.context).clear(image)
            author.text = post.author
            URLUtil.isValidUrl(link.text.toString())
            val originalFormat =
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault())
            val targetFormat = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault())
            originalFormat.timeZone =
                TimeZone.getTimeZone("UTC")
            val date = originalFormat.parse(post.published)

            published.text =
                if (date != null) targetFormat.format(date) else context.getString(R.string.unknown_date)

            authorJob.text = post.authorJob
            content.text = post.content
            post.authorAvatar?.let {
                Glide.with(itemView.context)
                    .load(it)
                    .placeholder(R.drawable.baseline_upload_file_24)
                    .error(R.drawable.baseline_error_outline_24)

                    .into(avatar)
            }

            like.isChecked = post.likedByMe
            if (post.attachment == null) {
                image.visibility = View.GONE
            } else {
                post.attachment.url.let {
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

            menu.visibility = if (post.authorId == currentUserId)
                View.VISIBLE else View.INVISIBLE

            menu.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.options_post)


                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.remove -> {
                                onInteractionListener.onRemove(post)
                                true
                            }
                            R.id.edit -> {

                                onInteractionListener.onEditNavigate(post)


                                true
                            }

                            else -> false
                        }
                    }
                }.show()
            }

            like.setOnClickListener {
                like.isChecked = post.likedByMe
                onInteractionListener.onLike(post)
            }
            like.isChecked = post.likedByMe

        }
    }
}