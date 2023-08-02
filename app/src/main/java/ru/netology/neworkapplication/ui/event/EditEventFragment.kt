package ru.netology.neworkapplication.ui.event

import android.os.Bundle
import android.view.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toFile
import androidx.core.view.MenuProvider
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.neworkapplication.R
import ru.netology.neworkapplication.databinding.FragmentNewEventBinding
import ru.netology.neworkapplication.ui.FeedFragment
import ru.netology.neworkapplication.util.AndroidUtils
import ru.netology.neworkapplication.util.StringArg
import ru.netology.neworkapplication.viewmodel.EventViewModel


@AndroidEntryPoint
class EditEventFragment : Fragment() {

    companion object {
        var Bundle.textArg: String? by StringArg
    }

    private val viewModel: EventViewModel by activityViewModels()


    private var fragmentBinding: FragmentNewEventBinding? = null
    private var eventId: Int = 0
    private var eventContent: String = ""
    private var authorJob: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentNewEventBinding.inflate(
            inflater,
            container,
            false
        )
        fragmentBinding = binding

        eventId = arguments?.getInt("id") ?: 0
        eventContent = arguments?.getString("content") ?: ""
        authorJob = arguments?.getString("job") ?: ""

        binding.edit.setText(eventContent)
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

        viewModel.editEvent(eventId)
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_new_post, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
                when (menuItem.itemId) {
                    R.id.save -> {
                        fragmentBinding?.let {

                            viewModel.changeContent(it.edit.text.toString())
                            viewModel.saveEvent()
                            AndroidUtils.hideKeyboard(requireView())
                        }
                        parentFragmentManager.commit {
                            replace(R.id.container, FeedFragment())
                            addToBackStack(null)
                        }
                        true
                    }
                    else -> false
                }

        }, viewLifecycleOwner)
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