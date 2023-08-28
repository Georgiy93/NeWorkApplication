package ru.netology.neworkapplication.ui


import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ConcatAdapter
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.qualifiers.ApplicationContext

import kotlinx.coroutines.flow.collectLatest
import ru.netology.neworkapplication.adapter.OnInteractionListener
import ru.netology.neworkapplication.adapter.events.OnInteractionListenerEvent
import ru.netology.neworkapplication.adapter.PostsAdapter
import ru.netology.neworkapplication.dto.Post
import ru.netology.neworkapplication.R

import ru.netology.neworkapplication.adapter.PostLoadingStateAdapter
import ru.netology.neworkapplication.adapter.events.EventLoadingStateAdapter
import ru.netology.neworkapplication.adapter.events.EventsAdapter
import ru.netology.neworkapplication.auth.AppAuth

import ru.netology.neworkapplication.databinding.FragmentFeedBinding
import ru.netology.neworkapplication.dto.Event
import ru.netology.neworkapplication.ui.event.EditEventFragment
import ru.netology.neworkapplication.ui.event.EventFragment
import ru.netology.neworkapplication.ui.event.NewEventFragment
import ru.netology.neworkapplication.ui.job.JobFragment
import ru.netology.neworkapplication.ui.wall.WallFeedFragment


import ru.netology.neworkapplication.viewmodel.EventViewModel


import ru.netology.neworkapplication.viewmodel.PostViewModel
import javax.inject.Inject

@AndroidEntryPoint
class FeedFragment : Fragment() {

    companion object {
        const val KEY_CONTENT = "content"
        const val KEY_ID = "id"
    }

    private val postViewModel: PostViewModel by activityViewModels()

    @Inject
    lateinit var auth: AppAuth

    @ApplicationContext
    @Inject
    lateinit var fragmentContext: Context

    private val postAdapter: PostsAdapter by lazy {
        PostsAdapter(object : OnInteractionListener {

            override fun onEdit(post: Post) {
                postViewModel.edit(post.id)
            }

            override fun onLike(post: Post) {
                postViewModel.likeById(post)
            }

            override fun onRemove(post: Post) {
                postViewModel.removeById(post.id)
            }

            override fun onEditNavigate(post: Post) {
                parentFragmentManager.commit {
                    replace(R.id.container, EditPostFragment().apply {
                        arguments = Bundle().apply {
                            putString(KEY_CONTENT, post.content)
                            putLong(KEY_ID, post.id)
                        }
                    })
                    addToBackStack(null)
                }
            }

        }, auth, fragmentContext)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentFeedBinding.inflate(inflater, container, false)

        val adapter = postAdapter.withLoadStateHeaderAndFooter(
            header = PostLoadingStateAdapter { postAdapter.retry() },
            footer = PostLoadingStateAdapter { postAdapter.retry() }
        )

        binding.list.adapter = adapter
        postViewModel.dataState.observe(viewLifecycleOwner) { state ->
            binding.progress.isVisible = state.loading
            if (state.error) {
                Snackbar.make(binding.root, R.string.error_loading, Snackbar.LENGTH_LONG)
                    .setAction(R.string.retry_loading) { postViewModel.loadPosts() }
                    .show()
            }
        }

        postViewModel.messageError.observe(viewLifecycleOwner) { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }

        lifecycleScope.launchWhenCreated {
            postViewModel.data.collectLatest { pagingData ->
                postAdapter.submitData(pagingData)
            }
        }

        binding.swiperefresh.setOnRefreshListener {
            postAdapter.refresh()
        }

        lifecycleScope.launchWhenCreated {
            postViewModel.data.collectLatest { pagingData ->
                postAdapter.submitData(pagingData)
                binding.swiperefresh.isRefreshing = false
            }
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

        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.main_title))
            .setMessage(getString(R.string.main_description))
            .setPositiveButton(getString(R.string.understand), null)
            .show()

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
                    R.id.event -> {
                        parentFragmentManager.commit {
                            replace(R.id.container, EventFragment())
                            addToBackStack(null)
                        }
                        true
                    }
                    else -> false
                }

        }, viewLifecycleOwner)
    }
}
