package ru.netology.neworkapplication.adapter.job

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.neworkapplication.databinding.ItemLoadingBinding


class JobLoadingStateAdapter(private val retryListener: () -> Unit) :
    LoadStateAdapter<JobLoadingViewHolder>() {
    override fun onBindViewHolder(holder: JobLoadingViewHolder, loadState: LoadState) {
        holder.bind(loadState)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        loadState: LoadState
    ): JobLoadingViewHolder =
        JobLoadingViewHolder(
            ItemLoadingBinding.inflate(
                LayoutInflater.from(parent.context),
                parent, false
            ),
            retryListener,
        )


}

class JobLoadingViewHolder(
    private val itemLoadingBinding: ItemLoadingBinding,
    private val retryListener: () -> Unit
) :
    RecyclerView.ViewHolder(itemLoadingBinding.root) {
    fun bind(loadState: LoadState) {
        itemLoadingBinding.apply {
            progress.isVisible = loadState is LoadState.Loading
            retryButton.isVisible = loadState is LoadState.Error
            retryButton.setOnClickListener { retryListener() }
        }
    }

}