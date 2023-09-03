package ru.netology.neworkapplication.ui


import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
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
import ru.netology.neworkapplication.adapter.OnInteractionListener
import ru.netology.neworkapplication.adapter.PostLoadingStateAdapter
import ru.netology.neworkapplication.adapter.PostsAdapter
import ru.netology.neworkapplication.auth.AppAuth
import ru.netology.neworkapplication.databinding.FragmentFeedBinding
import ru.netology.neworkapplication.dto.Post
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
    lateinit var sharedPreferences: SharedPreferences

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

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) {
                launch {
                    postViewModel.data.collectLatest { pagingData ->
                        postAdapter.submitData(pagingData)
                    }
                }
            }
        }

        binding.swiperefresh.setOnRefreshListener {
            postAdapter.refresh()
        }

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) {
                launch {
                    postViewModel.data.collectLatest { pagingData ->
                        postAdapter.submitData(pagingData)
                        binding.swiperefresh.isRefreshing = false
                    }
                }
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

    private fun isDialogShown(): Boolean {
        return sharedPreferences.getBoolean("DIALOG_SHOWN", false)
    }

    private fun setDialogShown() {
        val editor = sharedPreferences.edit()
        editor.putBoolean("DIALOG_SHOWN", true)
        editor.apply()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!isDialogShown()) {
            AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.main_title))
                .setMessage(getString(R.string.main_description))
                .setPositiveButton(getString(R.string.understand)) { _, _ ->
                    setDialogShown()
                }
                .show()
        }


    }
}
