package ru.netology.neworkapplication.ui.job

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.neworkapplication.databinding.FragmentNewJobBinding
import ru.netology.neworkapplication.util.AndroidUtils.setupJobMenu
import ru.netology.neworkapplication.util.StringArg
import ru.netology.neworkapplication.viewmodel.JobViewModel
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
                    val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
                    sdf.timeZone = TimeZone.getDefault()
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
                    val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
                    sdf.timeZone = TimeZone.getDefault()
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

        setupJobMenu(this, viewLifecycleOwner, viewModel)

    }

    override fun onDestroyView() {
        fragmentBinding = null
        super.onDestroyView()
    }
}
