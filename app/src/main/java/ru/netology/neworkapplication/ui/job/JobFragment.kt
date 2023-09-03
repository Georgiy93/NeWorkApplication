package ru.netology.neworkapplication.ui.job

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.netology.neworkapplication.R
import ru.netology.neworkapplication.adapter.job.JobAdapter
import ru.netology.neworkapplication.adapter.job.OnInteractionListener
import ru.netology.neworkapplication.auth.AppAuth
import ru.netology.neworkapplication.databinding.FragmentJobBinding
import ru.netology.neworkapplication.dto.Job
import ru.netology.neworkapplication.ui.FeedFragment
import ru.netology.neworkapplication.viewmodel.JobViewModel
import javax.inject.Inject

@AndroidEntryPoint
class JobFragment : Fragment() {
    private val viewModel: JobViewModel by viewModels()

    @Inject
    lateinit var auth: AppAuth

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
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.jobs.collect { jobs ->
                        adapter.submitList(jobs)
                    }
                }
            }
        }
        viewModel.messageError.observe(
            viewLifecycleOwner
        ) { message -> Toast.makeText(context, message, Toast.LENGTH_SHORT).show() }
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
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.jobs.collect { jobs ->
                        adapter.submitList(jobs)


                        binding.emptyText.visibility =
                            if (jobs.isEmpty()) View.VISIBLE else View.GONE
                        binding.list.visibility = if (jobs.isEmpty()) View.GONE else View.VISIBLE
                    }
                }
            }
        }

        return binding.root

    }




}