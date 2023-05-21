package ru.netology.neworkapplication.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

import kotlinx.coroutines.flow.collectLatest
import ru.netology.neworkapplication.adapter.OnInteractionListener
import ru.netology.neworkapplication.adapter.PostsAdapter
import ru.netology.neworkapplication.dto.Post
import ru.netology.neworkapplication.R

import ru.netology.neworkapplication.adapter.PostLoadingStateAdapter
import ru.netology.neworkapplication.databinding.FragmentFeedBinding


import ru.netology.neworkapplication.viewmodel.PostViewModel

@AndroidEntryPoint
class FeedFragment : Fragment() {
    private val viewModel: PostViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentFeedBinding.inflate(inflater, container, false)

        val adapter = PostsAdapter(object : OnInteractionListener {
            override fun onEdit(post: Post) {
                viewModel.edit(post)
            }

            override fun onLike(post: Post) {
                viewModel.likeById(post)
            }

            override fun onRemove(post: Post) {
                viewModel.removeById(post.id)
            }


        })
        viewModel.messageError.observe(
            viewLifecycleOwner,
            Observer { message -> Toast.makeText(context, message, Toast.LENGTH_SHORT).show() })

        binding.list.adapter =
            adapter.withLoadStateHeaderAndFooter(header = PostLoadingStateAdapter {
                adapter.retry()
            }, footer = PostLoadingStateAdapter { adapter.retry() })

        viewModel.dataState.observe(viewLifecycleOwner) { state ->
            binding.progress.isVisible = state.loading
            // binding.swiperefresh.isRefreshing = state.refreshing
            if (state.error) {
                Snackbar.make(binding.root, R.string.error_loading, Snackbar.LENGTH_LONG)
                    .setAction(R.string.retry_loading) { viewModel.loadPosts() }
                    .show()
            }
        }
        lifecycleScope.launchWhenCreated {
            viewModel.data.collectLatest { adapter.submitData(it) }
        }

//Refreshing SwipeRefreshLayout is displayed only with manual Refresh
        lifecycleScope.launchWhenCreated {
            adapter.loadStateFlow.collectLatest {
                binding.swiperefresh.isRefreshing =
                    it.refresh is LoadState.Loading &&
                            it.append !is LoadState.Loading &&
                            it.prepend !is LoadState.Loading
            }
        }

        binding.swiperefresh.setOnRefreshListener {
            adapter.refresh()
        }

        binding.fab.setOnClickListener {
            findNavController().navigate(R.id.action_feedFragment_to_newPostFragment)
        }

        return binding.root
    }
}
