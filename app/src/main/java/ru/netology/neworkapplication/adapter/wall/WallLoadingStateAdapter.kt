package ru.netology.neworkapplication.adapter.wall

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.neworkapplication.databinding.ItemLoadingBinding


class WallLoadingStateAdapter(private val retryListener: () -> Unit) :
    LoadStateAdapter<WallLoadingViewHolder>() {
    override fun onBindViewHolder(holder: WallLoadingViewHolder, loadState: LoadState) {
        holder.bind(loadState)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        loadState: LoadState
    ): WallLoadingViewHolder =
        WallLoadingViewHolder(
            ItemLoadingBinding.inflate(
                LayoutInflater.from(parent.context),
                parent, false
            ),
            retryListener,
        )


}

class WallLoadingViewHolder(
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