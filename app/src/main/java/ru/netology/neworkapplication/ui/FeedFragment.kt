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
import androidx.recyclerview.widget.ConcatAdapter
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

import kotlinx.coroutines.flow.collectLatest
import ru.netology.neworkapplication.adapter.OnInteractionListener
import ru.netology.neworkapplication.adapter.events.OnInteractionListener2
import ru.netology.neworkapplication.adapter.PostsAdapter
import ru.netology.neworkapplication.dto.Post
import ru.netology.neworkapplication.R

import ru.netology.neworkapplication.adapter.PostLoadingStateAdapter
import ru.netology.neworkapplication.adapter.events.EventLoadingStateAdapter
import ru.netology.neworkapplication.adapter.events.EventsAdapter
import ru.netology.neworkapplication.auth.AppAuth
import ru.netology.neworkapplication.databinding.FragmentFeedBinding
import ru.netology.neworkapplication.dto.Event
import ru.netology.neworkapplication.dto.Job
import ru.netology.neworkapplication.ui.event.EditEventFragment
import ru.netology.neworkapplication.ui.event.NewEventFragment
import ru.netology.neworkapplication.ui.job.JobFragment
import ru.netology.neworkapplication.ui.wall.WallFeedFragment

import ru.netology.neworkapplication.util.TokenManager
import ru.netology.neworkapplication.viewmodel.EventViewModel


import ru.netology.neworkapplication.viewmodel.PostViewModel
import javax.inject.Inject

@AndroidEntryPoint
class FeedFragment : Fragment() {
    private val postViewModel: PostViewModel by activityViewModels()
    private val eventViewModel: EventViewModel by activityViewModels()

    @Inject
    lateinit var tokenManager: TokenManager

    @Inject
    lateinit var auth: AppAuth

    lateinit var postAdapter: PostsAdapter
    lateinit var eventAdapter: EventsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentFeedBinding.inflate(inflater, container, false)

        postAdapter = PostsAdapter(object : OnInteractionListener {

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
                            putString("content", post.content)

                            putInt("id", post.id)
                        }
                    })
                    addToBackStack(null)
                }
            }

        }, tokenManager)
        eventAdapter = EventsAdapter(object : OnInteractionListener2 {

            override fun onEdit(event: Event) {
                eventViewModel.editEvent(event.id)
            }

            override fun onLike(event: Event) {
                eventViewModel.likeEventById(event)
            }

            override fun onRemove(event: Event) {
                eventViewModel.removeEventById(event.id)
            }

            override fun onEditNavigate(event: Event) {
                parentFragmentManager.commit {
                    replace(R.id.container, EditEventFragment().apply {
                        arguments = Bundle().apply {
                            putString("content", event.content)

                            putInt("id", event.id)
                        }
                    })
                    addToBackStack(null)
                }
            }

        }, tokenManager)
        val adapter = ConcatAdapter(
            postAdapter.withLoadStateHeaderAndFooter(
                header = PostLoadingStateAdapter { postAdapter.retry() },
                footer = PostLoadingStateAdapter { postAdapter.retry() }
            ),
            eventAdapter.withLoadStateHeaderAndFooter(
                header = EventLoadingStateAdapter { eventAdapter.retry() },
                footer = EventLoadingStateAdapter { eventAdapter.retry() }
            )
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
        eventViewModel.dataState.observe(viewLifecycleOwner) { state ->
            binding.progress.isVisible = state.loading
            if (state.error) {
                Snackbar.make(binding.root, R.string.error_loading, Snackbar.LENGTH_LONG)
                    .setAction(R.string.retry_loading) { eventViewModel.loadEvents() }
                    .show()
            }
        }
        postViewModel.messageError.observe(viewLifecycleOwner) { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
        eventViewModel.messageError.observe(viewLifecycleOwner) { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }





        lifecycleScope.launchWhenCreated {
            postViewModel.data.collectLatest { pagingData ->
                postAdapter.submitData(pagingData)
            }
        }

        lifecycleScope.launchWhenCreated {
            eventViewModel.data.collectLatest { pagingData ->
                eventAdapter.submitData(pagingData)
            }
        }




        binding.swiperefresh.setOnRefreshListener {
            postAdapter.refresh()
            eventAdapter.refresh()
        }

        binding.fab.setOnClickListener {
            parentFragmentManager.commit {
                replace(R.id.container, NewPostFragment())
                addToBackStack(null)
            }
        }
        binding.fabEvent.setOnClickListener {
            parentFragmentManager.commit {
                replace(R.id.container, NewEventFragment())
                addToBackStack(null)
            }
        }

        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



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
