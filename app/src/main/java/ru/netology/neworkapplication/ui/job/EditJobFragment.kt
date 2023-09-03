package ru.netology.neworkapplication.ui.job

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.neworkapplication.databinding.FragmentNewJobBinding
import ru.netology.neworkapplication.util.AndroidUtils.setupJobMenu
import ru.netology.neworkapplication.viewmodel.JobViewModel

@AndroidEntryPoint
class EditJobFragment : Fragment() {

    companion object {
        const val KEY_ID = "id"
        const val KEY_NAME = "name"
        const val KEY_POSITION = "position"
        const val KEY_START = "start"
        const val KEY_FINISH = "finish"

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

        setupJobMenu(this, viewLifecycleOwner, viewModel)
    }

    override fun onDestroyView() {
        fragmentBinding = null
        super.onDestroyView()
    }
}
