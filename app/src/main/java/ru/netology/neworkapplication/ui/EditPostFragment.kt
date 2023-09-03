package ru.netology.neworkapplication.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toFile
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.neworkapplication.R
import ru.netology.neworkapplication.databinding.FragmentNewPostBinding
import ru.netology.neworkapplication.util.AndroidUtils.setupPostMenu
import ru.netology.neworkapplication.viewmodel.PostViewModel

@AndroidEntryPoint
class EditPostFragment : Fragment() {

    companion object {
        const val KEY_ID = "id"
        const val KEY_CONTENT = "content"
        const val KEY_JOB = "job"


    }

    private val viewModel: PostViewModel by activityViewModels()
    private var fragmentBinding: FragmentNewPostBinding? = null
    private var postId: Long = 0
    private var postContent: String = ""
    private var authorJob: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentNewPostBinding.inflate(
            inflater,
            container,
            false
        )
        fragmentBinding = binding

        postId = arguments?.getLong(KEY_ID) ?: 0
        postContent = arguments?.getString(KEY_CONTENT) ?: ""
        authorJob = arguments?.getString(KEY_JOB) ?: ""

        binding.edit.setText(postContent)
        binding.edit.requestFocus()

        binding.clear.visibility = View.GONE
        val photoLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                when (it.resultCode) {
                    ImagePicker.RESULT_ERROR -> {
                        Snackbar.make(
                            binding.root,
                            getString(R.string.photo_error),
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                    else -> {
                        val uri = it.data?.data ?: return@registerForActivityResult
                        viewModel.changePhoto(uri.toFile(), uri)
                    }
                }
            }

        binding.pickPhoto.setOnClickListener {
            ImagePicker.with(this)
                .galleryOnly()
                .crop()
                .compress(2048)
                .createIntent(photoLauncher::launch)
        }
        viewModel.media.observe(viewLifecycleOwner) { media ->
            if (media == null) {
                binding.photoContainer.isGone = true
                return@observe
            }
            binding.photoContainer.isVisible = true
            binding.photo.setImageURI(media.uri)
        }
        binding.clear.visibility = View.VISIBLE

        viewModel.edit(postId)
        setupPostMenu(this, viewLifecycleOwner, viewModel)
        binding.clear.setOnClickListener {
            viewModel.clearPhoto()
        }
        return binding.root
    }

    override fun onDestroyView() {
        fragmentBinding = null
        super.onDestroyView()
    }
}

