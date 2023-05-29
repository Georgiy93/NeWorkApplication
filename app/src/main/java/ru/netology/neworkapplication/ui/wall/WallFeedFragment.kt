package ru.netology.neworkapplication.ui.wall

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Toast
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
import ru.netology.neworkapplication.R
import ru.netology.neworkapplication.adapter.OnInteractionListener
import ru.netology.neworkapplication.adapter.PostLoadingStateAdapter
import ru.netology.neworkapplication.adapter.PostsAdapter
import ru.netology.neworkapplication.adapter.wall.WallAdapter
import ru.netology.neworkapplication.auth.AppAuth
import ru.netology.neworkapplication.databinding.FragmentFeedBinding
import ru.netology.neworkapplication.databinding.FragmentWallBinding
import ru.netology.neworkapplication.dto.Post
import ru.netology.neworkapplication.ui.AuthActivity
import ru.netology.neworkapplication.ui.EditPostFragment
import ru.netology.neworkapplication.ui.NewPostFragment
import ru.netology.neworkapplication.util.TokenManager
import ru.netology.neworkapplication.viewmodel.PostViewModel
import javax.inject.Inject

@AndroidEntryPoint
class WallFeedFragment : Fragment() {
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
        val binding = FragmentWallBinding.inflate(inflater, container, false)

        val adapter = WallAdapter(tokenManager)
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

                    else -> false
                }

        }, viewLifecycleOwner)
    }
}