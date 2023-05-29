package ru.netology.neworkapplication.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.netology.neworkapplication.BuildConfig
import ru.netology.neworkapplication.R

import ru.netology.neworkapplication.databinding.CardPostBinding
import ru.netology.neworkapplication.dto.FeedItem
import ru.netology.neworkapplication.dto.Job
import ru.netology.neworkapplication.dto.Post
import ru.netology.neworkapplication.util.TokenManager
import ru.netology.neworkapplication.view.loadCircleCrop
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
    private val tokenManager: TokenManager,

    ) : PagingDataAdapter<FeedItem, RecyclerView.ViewHolder>(PostDiffCallback()) {
    override fun getItemViewType(position: Int): Int =
        when (getItem(position)) {

            is Post -> R.layout.card_post
            null -> error("unknown item type")
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            R.layout.card_post -> {
                val binding =
                    CardPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return PostViewHolder(binding, onInteractionListener, tokenManager)
            }

            else -> error("unknown item type: $viewType")
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {

            is Post -> (holder as? PostViewHolder)?.bind(item)
            null -> error("unknown item type")
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
    private val binding: CardPostBinding,
    private val onInteractionListener: OnInteractionListener,
    private val tokenManager: TokenManager,
    //private val job: Job
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(post: Post) {

        binding.apply {
            author.text = post.author

            val originalFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.US)
            val targetFormat = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.US)
            originalFormat.timeZone = TimeZone.getTimeZone("UTC") // if the original time is in UTC
            val date = originalFormat.parse(post.published)

            published.text = if (date != null) targetFormat.format(date) else "Unknown date"

            authorJob.text = post.authorJob
            content.text = post.content
            post.authorAvatar?.let {
                Glide.with(itemView.context)
                    .load(it)
                    .into(avatar)
            }

            like.isChecked = post.likedByMe
            if (post.attachment == null) {
                image.visibility = View.GONE
            } else {
                post.attachment?.url?.let {
                    Glide.with(itemView.context)
                        .load(it)
                        .into(image)
                }
            }

            menu.visibility = if (post.authorId == tokenManager.getId())
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
                                if (post.authorId == tokenManager.getId()) {
                                    onInteractionListener.onEditNavigate(post)
                                }

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