package ru.netology.neworkapplication.ui.wall

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.LoadState
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.netology.neworkapplication.R
import ru.netology.neworkapplication.adapter.wall.WallAdapter
import ru.netology.neworkapplication.adapter.wall.WallLoadingStateAdapter
import ru.netology.neworkapplication.auth.AppAuth
import ru.netology.neworkapplication.databinding.FragmentWallBinding
import ru.netology.neworkapplication.viewmodel.WallViewModel
import javax.inject.Inject

@AndroidEntryPoint
class WallFeedFragment : Fragment() {
    private val viewModel: WallViewModel by activityViewModels()


    @Inject
    lateinit var auth: AppAuth

    @ApplicationContext
    @Inject
    lateinit var fragmentContext: Context
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentWallBinding.inflate(inflater, container, false)

        val adapter = WallAdapter(fragmentContext, auth)
        viewModel.messageError.observe(
            viewLifecycleOwner
        ) { message -> Toast.makeText(context, message, Toast.LENGTH_SHORT).show() }

        binding.list.adapter =
            adapter.withLoadStateHeaderAndFooter(header = WallLoadingStateAdapter {
                adapter.retry()
            }, footer = WallLoadingStateAdapter { adapter.retry() })

        viewModel.dataState.observe(viewLifecycleOwner) { state ->
            binding.progress.isVisible = state.loading


            if (state.error) {
                Snackbar.make(binding.root, R.string.error_loading, Snackbar.LENGTH_LONG)
                    .setAction(R.string.retry_loading) { viewModel.loadPosts() }
                    .show()
            }
        }
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) {
                launch {
                    viewModel.data.collectLatest { adapter.submitData(it) }
                }
            }
        }


        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) {
                launch {
                    adapter.loadStateFlow.collectLatest {
                        binding.swiperefresh.isRefreshing =
                            it.refresh is LoadState.Loading &&
                                    it.append !is LoadState.Loading &&
                                    it.prepend !is LoadState.Loading
                    }
                }
            }
        }


        binding.swiperefresh.setOnRefreshListener {
            adapter.refresh()
        }






        return binding.root

    }


}