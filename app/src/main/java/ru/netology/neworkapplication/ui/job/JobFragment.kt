package ru.netology.neworkapplication.ui.job

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import ru.netology.neworkapplication.R
import ru.netology.neworkapplication.adapter.job.JobAdapter
import ru.netology.neworkapplication.adapter.job.JobLoadingStateAdapter
import ru.netology.neworkapplication.adapter.job.OnInteractionListener
import ru.netology.neworkapplication.auth.AppAuth

import ru.netology.neworkapplication.databinding.FragmentJobBinding
import ru.netology.neworkapplication.dto.Job
import ru.netology.neworkapplication.ui.AuthActivity
import ru.netology.neworkapplication.ui.FeedFragment
import ru.netology.neworkapplication.ui.event.EventFragment
import ru.netology.neworkapplication.ui.wall.WallFeedFragment

import ru.netology.neworkapplication.viewmodel.JobViewModel
import javax.inject.Inject

@AndroidEntryPoint
class JobFragment : Fragment() {
    private val viewModel: JobViewModel by viewModels()

    companion object {
        const val KEY_NAME = "name"
        const val KEY_POSITION = "position"
        const val KEY_START = "start"
        const val KEY_FINISH = "finish"
        const val KEY_ID = "id"
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentJobBinding.inflate(inflater, container, false)

        val adapter = JobAdapter(object : OnInteractionListener {

            override fun onEditJob(job: Job) {
                viewModel.edit(job.id)
            }

            override fun onRemoveJob(job: Job) {
                viewModel.removeJobById(job.id)
            }

            override fun onEditNavigateJob(job: Job) {
                parentFragmentManager.commit {
                    replace(
                        R.id.container, EditJobFragment().apply {
                            arguments = Bundle().apply {
                                putString(KEY_NAME, job.name)
                                putString(KEY_POSITION, job.position)
                                putString(KEY_START, job.start)
                                putString(KEY_FINISH, job.finish)
                                putLong(KEY_ID, job.id)
                            }
                        }
                    )
                    addToBackStack(null)
                }
            }

        })
        binding.list.adapter = adapter
        lifecycleScope.launchWhenStarted {
            viewModel.jobs.collect { jobs ->
                adapter.submitList(jobs)
            }
        }
        viewModel.messageError.observe(
            viewLifecycleOwner,
            Observer { message -> Toast.makeText(context, message, Toast.LENGTH_SHORT).show() })
        binding.list.adapter = adapter


        viewModel.dataState.observe(viewLifecycleOwner) { state ->
            binding.progress.isVisible = state.loading

            if (state.error) {
                Snackbar.make(binding.root, R.string.error_loading, Snackbar.LENGTH_LONG)
                    .setAction(R.string.retry_loading) { viewModel.loadJobs() }
                    .show()
            }
        }

        binding.swiperefresh.setOnRefreshListener {
            viewModel.loadJobs()

            binding.swiperefresh.isRefreshing = false

        }


binding.back.visibility = View.GONE
        binding.back.setOnClickListener {
            parentFragmentManager.commit {
                replace(R.id.container, FeedFragment())
                addToBackStack(null)
            }
        }

        binding.fab.setOnClickListener {
            parentFragmentManager.commit {
                replace(R.id.container, NewJobFragment())
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