package ru.netology.neworkapplication.adapter.job

import android.app.DatePickerDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ru.netology.neworkapplication.R
import ru.netology.neworkapplication.auth.AppAuth
import ru.netology.neworkapplication.databinding.CardJobBinding

import ru.netology.neworkapplication.dto.FeedItemJob
import ru.netology.neworkapplication.dto.Job

import java.text.SimpleDateFormat
import java.util.*

interface OnInteractionListener {


    fun onEditJob(job: Job) {}
    fun onRemoveJob(job: Job) {}
    fun onEditNavigateJob(job: Job) {}

}

class JobAdapter(
    private val onInteractionListener: OnInteractionListener,

    ) : RecyclerView.Adapter<JobAdapter.JobViewHolder>() {

    private var jobs: List<Job> = emptyList()

    fun submitList(jobs: List<Job>) {
        this.jobs = jobs
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = jobs.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobViewHolder {
        val binding = CardJobBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return JobViewHolder(binding, onInteractionListener)
    }

    override fun onBindViewHolder(holder: JobViewHolder, position: Int) {
        holder.bind(jobs[position])
    }




    class JobViewHolder(
        private val binding: CardJobBinding,
        private val onInteractionListener: OnInteractionListener,

        ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(job: Job) {
            binding.apply {
                name.text = job.name
                position.text = job.position
                start.text = job.start
                finish.text = job.finish
                link.text = job.link
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
}
