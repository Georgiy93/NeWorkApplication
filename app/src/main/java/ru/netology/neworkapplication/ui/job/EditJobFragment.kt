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
        var Bundle.textArg: String? by StringArg
    }

    private val viewModel: JobViewModel by activityViewModels()
    private var fragmentBinding: FragmentNewJobBinding? = null
    private var jobId: Int = 0
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

        jobId = arguments?.getInt("id") ?: 0
        name = arguments?.getString("name") ?: ""
        position = arguments?.getString("position") ?: ""
        start = arguments?.getString("start") ?: ""
        finish = arguments?.getString("finish") ?: ""

        binding.name.setText(name)

        binding.name.requestFocus()
        binding.position.setText(position)

        binding.position.requestFocus()
        binding.start.setText(start)

        binding.finish.requestFocus()
        binding.finish.setText(finish)

        binding.position.requestFocus()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.edit(jobId)  // Load the post you want to edit when the fragment is created

        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_new_post, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
                when (menuItem.itemId) {
                    R.id.save -> {
                        fragmentBinding?.let {
                            viewModel.changeContent(it.name.text.toString())
                            viewModel.changeContent(it.position.text.toString())
                            viewModel.changeContent(it.start.text.toString())
                            viewModel.changeContent(it.finish.text.toString())
                            viewModel.save()  // Save the changes when the save button is clicked
                            AndroidUtils.hideKeyboard(requireView())
                            //requireActivity().supportFragmentManager.popBackStack()
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