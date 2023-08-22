package ru.netology.neworkapplication.ui.job

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController

import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.neworkapplication.R
import ru.netology.neworkapplication.databinding.FragmentNewJobBinding

import ru.netology.neworkapplication.ui.wall.WallFeedFragment

import ru.netology.neworkapplication.util.AndroidUtils
import ru.netology.neworkapplication.util.StringArg
import ru.netology.neworkapplication.viewmodel.JobViewModel
import ru.netology.neworkapplication.viewmodel.PostViewModel
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class NewJobFragment : Fragment() {

    companion object {
        var Bundle.textArg: String? by StringArg
    }

    private val viewModel: JobViewModel by activityViewModels()

    private var fragmentBinding: FragmentNewJobBinding? = null


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

        arguments?.textArg
            ?.let(binding.name::setText)

        arguments?.textArg
            ?.let(binding.position::setText)

        arguments?.textArg
            ?.let(binding.start::setText)

        arguments?.textArg
            ?.let(binding.finish::setText)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val calendar = Calendar.getInstance()


        val startDateSetListener =
            DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                val startCalendar = Calendar.getInstance()
                startCalendar.set(year, monthOfYear, dayOfMonth)
                val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hour, minute ->
                    startCalendar.set(Calendar.HOUR_OF_DAY, hour)
                    startCalendar.set(Calendar.MINUTE, minute)
                    startCalendar.set(Calendar.SECOND, 0)
                    startCalendar.set(Calendar.MILLISECOND, 0)
                    val myFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
                    val sdf = SimpleDateFormat(myFormat, Locale("ru", "RU"))
                    sdf.setTimeZone(TimeZone.getTimeZone("Europe/Moscow"))
                    fragmentBinding?.start?.setText(sdf.format(startCalendar.time))
                }
                TimePickerDialog(
                    requireContext(), timeSetListener,
                    startCalendar.get(Calendar.HOUR_OF_DAY),
                    startCalendar.get(Calendar.MINUTE), true
                ).show()
            }

        val finishDateSetListener =
            DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                val finishCalendar = Calendar.getInstance()
                finishCalendar.set(year, monthOfYear, dayOfMonth)
                val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hour, minute ->
                    finishCalendar.set(Calendar.HOUR_OF_DAY, hour)
                    finishCalendar.set(Calendar.MINUTE, minute)
                    finishCalendar.set(Calendar.SECOND, 0)
                    finishCalendar.set(Calendar.MILLISECOND, 0)
                    val myFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
                    val sdf = SimpleDateFormat(myFormat, Locale("ru", "RU"))
                    sdf.setTimeZone(TimeZone.getTimeZone("Europe/Moscow"))
                    fragmentBinding?.finish?.setText(sdf.format(finishCalendar.time))
                }
                TimePickerDialog(
                    requireContext(), timeSetListener,
                    finishCalendar.get(Calendar.HOUR_OF_DAY),
                    finishCalendar.get(Calendar.MINUTE), true
                ).show()
            }

// Use the separate listeners
        fragmentBinding?.start?.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                DatePickerDialog(
                    requireContext(), startDateSetListener,
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            }
        }

        fragmentBinding?.finish?.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                DatePickerDialog(
                    requireContext(), finishDateSetListener,
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            }
        }

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
                                viewModel.changeFinish(null) // set finish to null if it is empty
                            } else {
                                viewModel.changeFinish(it.finish.text.toString())
                            }
                            if (it.link.text.toString().isEmpty()) {

                                viewModel.changeLink(null) // set finish to null if it is empty
                            } else {
                                viewModel.changeLink(it.link.text.toString())
                            }
                            viewModel.save()
                            AndroidUtils.hideKeyboard(requireView())
                        }

                        parentFragmentManager.popBackStack()
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
