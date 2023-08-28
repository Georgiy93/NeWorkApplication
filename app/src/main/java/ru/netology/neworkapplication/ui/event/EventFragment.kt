package ru.netology.neworkapplication.ui.event

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
import ru.netology.neworkapplication.R
import ru.netology.neworkapplication.adapter.PostLoadingStateAdapter
import ru.netology.neworkapplication.adapter.events.EventLoadingStateAdapter
import ru.netology.neworkapplication.adapter.events.EventsAdapter
import ru.netology.neworkapplication.adapter.events.OnInteractionListenerEvent
import ru.netology.neworkapplication.auth.AppAuth
import ru.netology.neworkapplication.auth.NoIdException
import ru.netology.neworkapplication.databinding.FragmentEventBinding
import ru.netology.neworkapplication.dto.Event
import ru.netology.neworkapplication.ui.AuthActivity
import ru.netology.neworkapplication.ui.NewPostFragment
import ru.netology.neworkapplication.ui.job.JobFragment
import ru.netology.neworkapplication.ui.wall.WallFeedFragment

import ru.netology.neworkapplication.viewmodel.EventViewModel
import javax.inject.Inject

@AndroidEntryPoint
class EventFragment : Fragment() {

    companion object {
        const val KEY_CONTENT = "content"
        const val KEY_ID = "id"
    }

    private val eventViewModel: EventViewModel by activityViewModels()

    @Inject
    lateinit var appAuth: AppAuth

    @Inject
    @ApplicationContext
    lateinit var fragmentContext: Context

    private val eventAdapter: EventsAdapter by lazy {
        EventsAdapter(fragmentContext, object : OnInteractionListenerEvent {

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
                            putString(KEY_CONTENT, event.content)
                            putLong(KEY_ID, event.id)
                        }
                    })
                    addToBackStack(null)
                }
            }

        }, appAuth)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentEventBinding.inflate(inflater, container, false)

        val adapter = eventAdapter.withLoadStateHeaderAndFooter(
            header = EventLoadingStateAdapter { eventAdapter.retry() },
            footer = EventLoadingStateAdapter { eventAdapter.retry() }
        )

        binding.list.adapter = adapter
        eventViewModel.dataState.observe(viewLifecycleOwner) { state ->
            binding.progress.isVisible = state.loading
            if (state.error) {
                Snackbar.make(binding.root, R.string.error_loading, Snackbar.LENGTH_LONG)
                    .setAction(R.string.retry_loading) { eventViewModel.loadEvents() }
                    .show()
            }
        }

        eventViewModel.messageError.observe(viewLifecycleOwner) { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }

        lifecycleScope.launchWhenCreated {
            eventViewModel.data.collectLatest { pagingData ->
                eventAdapter.submitData(pagingData)
            }
        }

        binding.swiperefresh.setOnRefreshListener {
            eventAdapter.refresh()
        }

        lifecycleScope.launchWhenCreated {
            eventViewModel.data.collectLatest { pagingData ->
                eventAdapter.submitData(pagingData)
                binding.swiperefresh.isRefreshing = false
            }
        }

        try {
            val currentUserId = appAuth.getId()
            binding.fab.isVisible = (currentUserId == 401L)
        } catch (e: NoIdException) {
            binding.fab.isVisible = false
        }

        binding.fab.setOnClickListener {
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
