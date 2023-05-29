package ru.netology.neworkapplication.ui


import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

import kotlinx.coroutines.flow.collectLatest
import ru.netology.neworkapplication.adapter.OnInteractionListener
import ru.netology.neworkapplication.adapter.PostsAdapter
import ru.netology.neworkapplication.dto.Post
import ru.netology.neworkapplication.R

import ru.netology.neworkapplication.adapter.PostLoadingStateAdapter
import ru.netology.neworkapplication.auth.AppAuth
import ru.netology.neworkapplication.databinding.FragmentFeedBinding
import ru.netology.neworkapplication.dto.Job
import ru.netology.neworkapplication.ui.job.JobFragment
import ru.netology.neworkapplication.ui.wall.WallFeedFragment

import ru.netology.neworkapplication.util.TokenManager


import ru.netology.neworkapplication.viewmodel.PostViewModel
import javax.inject.Inject

@AndroidEntryPoint
class FeedFragment : Fragment() {
    private val viewModel: PostViewModel by activityViewModels()

    @Inject
    lateinit var tokenManager: TokenManager

    @Inject
    lateinit var auth: AppAuth
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentFeedBinding.inflate(inflater, container, false)

        val adapter = PostsAdapter(object : OnInteractionListener {

            override fun onEdit(post: Post) {
                viewModel.edit(post.id)
            }

            override fun onLike(post: Post) {
                viewModel.likeById(post)
            }

            override fun onRemove(post: Post) {
                viewModel.removeById(post.id)
            }

            override fun onEditNavigate(post: Post) {
                parentFragmentManager.commit {
                    replace(R.id.container, EditPostFragment().apply {
                        arguments = Bundle().apply {
                            putString("content", post.content)
                            // pass post content
                            putInt("id", post.id) // pass post id
                        }
                    })
                    addToBackStack(null)
                }
            }

        }, tokenManager)
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
            parentFragmentManager.commit {
                replace(R.id.container, NewPostFragment())
                addToBackStack(null)
            }
        }


        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Load the post you want to edit when the fragment is created

        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_main, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
                when (menuItem.itemId) {
                    R.id.signout -> {
                        val intent = Intent(context, AuthActivity::class.java)
                        startActivity(intent)
                        requireActivity().finish()
                        true
                    }
                    R.id.wall -> {
                        parentFragmentManager.commit {
                            replace(R.id.container, WallFeedFragment())
                            addToBackStack(null)
                        }
                        true
                    }
                    R.id.job -> {
                        parentFragmentManager.commit {
                            replace(R.id.container, JobFragment())
                            addToBackStack(null)
                        }
                        true
                    }
                    else -> false
                }

        }, viewLifecycleOwner)
    }


}
