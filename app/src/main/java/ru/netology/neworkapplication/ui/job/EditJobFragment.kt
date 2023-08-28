package ru.netology.neworkapplication.ui.job

import android.os.Bundle
import android.view.*
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.neworkapplication.R
import ru.netology.neworkapplication.databinding.FragmentNewJobBinding
import ru.netology.neworkapplication.databinding.FragmentNewPostBinding
import ru.netology.neworkapplication.dto.Post
import ru.netology.neworkapplication.ui.NewPostFragment.Companion.textArg
import ru.netology.neworkapplication.util.AndroidUtils
import ru.netology.neworkapplication.util.StringArg
import ru.netology.neworkapplication.viewmodel.JobViewModel
import ru.netology.neworkapplication.viewmodel.PostViewModel

@AndroidEntryPoint
class EditJobFragment : Fragment() {

    companion object {
        const val KEY_ID = "id"
        const val KEY_NAME = "name"
        const val KEY_POSITION = "position"
        const val KEY_START = "start"
        const val KEY_FINISH = "finish"
        var Bundle.textArg: String? by StringArg
    }

    private val viewModel: JobViewModel by activityViewModels()
    private var fragmentBinding: FragmentNewJobBinding? = null
    private var jobId: Long = 0
    private var name: String = ""
    private var position: String = ""
    private var start: String = ""
    private var finish: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentNewJobBinding.inflate(
            inflater,
            container,
            false
        )
        fragmentBinding = binding

        jobId = arguments?.getLong(KEY_ID) ?: 0
        name = arguments?.getString(KEY_NAME) ?: ""
        position = arguments?.getString(KEY_POSITION) ?: ""
        start = arguments?.getString(KEY_START) ?: ""
        finish = arguments?.getString(KEY_FINISH) ?: ""

        binding.name.setText(name)
        binding.name.requestFocus()
        binding.position.setText(position)
        binding.position.requestFocus()
        binding.start.setText(start)
        binding.finish.setText(finish)
        binding.position.requestFocus()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.edit(jobId)

        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_new_post, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
                when (menuItem.itemId) {
                    R.id.save -> {
                        fragmentBinding?.let {
                            viewModel.changeName(it.name.text.toString())
                            viewModel.changePosition(it.position.text.toString())
                            viewModel.changeStart(it.start.text.toString())

                            if (it.finish.text.toString().isEmpty()) {
                                viewModel.changeFinish(null)
                            } else {
                                viewModel.changeFinish(it.finish.text.toString())
                            }
                            if (it.link.text.toString().isEmpty()) {

                                viewModel.changeLink(null)
                            } else {
                                viewModel.changeLink(it.link.text.toString())
                            }
                            viewModel.save()
                            AndroidUtils.hideKeyboard(requireView())
                        }
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

    override fun onDestroyView() {
        fragmentBinding = null
        super.onDestroyView()
    }
}
