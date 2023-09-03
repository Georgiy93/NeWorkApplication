package ru.netology.neworkapplication.ui.event

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.netology.neworkapplication.R
import ru.netology.neworkapplication.adapter.events.EventLoadingStateAdapter
import ru.netology.neworkapplication.adapter.events.EventsAdapter
import ru.netology.neworkapplication.adapter.events.OnInteractionListenerEvent
import ru.netology.neworkapplication.auth.AppAuth
import ru.netology.neworkapplication.databinding.FragmentEventBinding
import ru.netology.neworkapplication.dto.Event
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

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) {
                launch {
                    eventViewModel.data.collectLatest { pagingData ->
                        eventAdapter.submitData(pagingData)
                    }
                }
            }
        }

        binding.swiperefresh.setOnRefreshListener {
            eventAdapter.refresh()
        }

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) {
                launch {
                    eventViewModel.data.collectLatest { pagingData ->
                        eventAdapter.submitData(pagingData)
                        binding.swiperefresh.isRefreshing = false
                    }
                }
            }
        }



        binding.fab.setOnClickListener {
            parentFragmentManager.commit {
                replace(R.id.container, NewEventFragment())
                addToBackStack(null)
            }
        }

        return binding.root
    }


}
