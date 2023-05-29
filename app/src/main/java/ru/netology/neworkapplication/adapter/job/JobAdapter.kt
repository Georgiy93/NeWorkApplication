package ru.netology.neworkapplication.adapter.job

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ru.netology.neworkapplication.R
import ru.netology.neworkapplication.databinding.CardJobBinding

import ru.netology.neworkapplication.dto.FeedItemJob
import ru.netology.neworkapplication.dto.Job
import ru.netology.neworkapplication.util.TokenManager

interface OnInteractionListener {


    fun onEditJob(job: Job) {}
    fun onRemoveJob(job: Job) {}
    fun onEditNavigateJob(job: Job) {}

}

class JobAdapter(
    private val onInteractionListener: OnInteractionListener,
    private val tokenManager: TokenManager

) : PagingDataAdapter<FeedItemJob, RecyclerView.ViewHolder>(JobDiffCallback()) {
    override fun getItemViewType(position: Int): Int =
        when (getItem(position)) {

            is Job -> R.layout.card_job
            null -> error("unknown item type")
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            R.layout.card_job -> {
                val binding =
                    CardJobBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return JobViewHolder(binding, onInteractionListener, tokenManager)
            }

            else -> error("unknown item type: $viewType")
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {

            is Job -> (holder as? JobViewHolder)?.bind(item)
            null -> error("unknown item type")
        }

    }
}

class JobDiffCallback : DiffUtil.ItemCallback<FeedItemJob>() {
    override fun areItemsTheSame(oldItem: FeedItemJob, newItem: FeedItemJob): Boolean {
        if (oldItem::class != newItem::class) {
            return false
        }
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: FeedItemJob, newItem: FeedItemJob): Boolean {
        return oldItem == newItem
    }
}

class JobViewHolder(
    private val binding: CardJobBinding,
    private val onInteractionListener: OnInteractionListener,
    private val tokenManager: TokenManager,

    ) : RecyclerView.ViewHolder(binding.root) {
    fun bind(job: Job) {

        binding.apply {
            name.text = job.name



            position.text = job.position
            start.text = job.start
            finish.text = job.finish






            menu.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.options_post)


                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.remove -> {
                                onInteractionListener.onRemoveJob(job)
                                true
                            }
                            R.id.edit -> {

                                onInteractionListener.onEditNavigateJob(job)


                                true
                            }

                            else -> false
                        }
                    }
                }.show()
            }


        }
    }
}