package ru.netology.neworkapplication.adapter.wall

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.netology.neworkapplication.R
import ru.netology.neworkapplication.auth.AppAuth
import ru.netology.neworkapplication.databinding.CardPostBinding


import ru.netology.neworkapplication.dto.FeedItem
import ru.netology.neworkapplication.dto.Post

import ru.netology.neworkapplication.view.loadCircleCrop
import java.text.SimpleDateFormat
import java.util.*



class WallAdapter(
    private val context: Context,
    private val appAuth: AppAuth

) : PagingDataAdapter<FeedItem, RecyclerView.ViewHolder>(WallDiffCallback()) {

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
                return WallViewHolder(binding, context, appAuth)
            }

            else -> error(context.getString(R.string.unknown_item_type_error, viewType))
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {

            is Post -> (holder as? WallViewHolder)?.bind(item)
            null -> error(R.string.unknown_item_type)
        }

    }
}

class WallDiffCallback : DiffUtil.ItemCallback<FeedItem>() {
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

class WallViewHolder(
    private val binding: CardPostBinding,
    private val context: Context,
    private val appAuth: AppAuth
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(post: Post) {
        val currentUserId = try {
            appAuth.getId()
        } catch (_: Exception) {


        }


        if (post.authorId == currentUserId) {
            binding.apply {
                author.text = post.author

                val originalFormat =
                    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault())
                val targetFormat = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault())
                originalFormat.timeZone =
                    TimeZone.getTimeZone("UTC")
                val date = originalFormat.parse(post.published)

                published.text = if (date != null) targetFormat.format(date)
                else context.getString(R.string.unknown_date)

                authorJob.text = post.authorJob
                content.text = post.content
                avatar.loadCircleCrop("${post.authorAvatar}")
                like.isChecked = post.likedByMe
                like.isClickable = false
                like.isEnabled = false
                if (post.attachment == null) {
                    Glide.with(itemView.context).clear(image)
                    image.visibility = View.GONE
                } else {
                    post.attachment.url.let {
                        Glide.with(itemView.context)
                            .load(it)
                            .into(image)
                    }
                    image.visibility = View.VISIBLE
                }
                menu.visibility = View.GONE


            }
        }

    }
}