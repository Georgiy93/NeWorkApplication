package ru.netology.neworkapplication.ui.event

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.ListView
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
import ru.netology.neworkapplication.dto.EventType
import ru.netology.neworkapplication.ui.job.JobFragment
import ru.netology.neworkapplication.util.AndroidUtils
import ru.netology.neworkapplication.util.StringArg
import ru.netology.neworkapplication.viewmodel.EventViewModel
import java.text.SimpleDateFormat
import java.util.*


@AndroidEntryPoint
class NewEventFragment : Fragment() {

    companion object {
        var Bundle.textArg: String? by StringArg
    }

    private val viewModel: EventViewModel by activityViewModels()

    private var fragmentBinding: FragmentNewEventBinding? = null
    private val participantsList = mutableListOf<String>()

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

        arguments?.textArg?.let { textArg ->
            binding.datetime.setText(textArg)
            binding.link.setText(textArg)
            binding.edit.setText(textArg)

        }
        fragmentBinding?.type?.setOnClickListener { view ->
            // list of event types
            val eventTypes = arrayOf(EventType.OFFLINE, EventType.ONLINE)
            val eventTypeNames = eventTypes.map { it.toString() }.toTypedArray()
            // create a dialog
            AlertDialog.Builder(requireContext())
                .setTitle("Event Type")
                .setSingleChoiceItems(eventTypeNames, -1) { dialog, which ->
                    val eventType = eventTypes[which]
                    fragmentBinding?.type?.setText(eventType.toString())
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.cancel()
                }
                .show()
        }
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

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val calendar = Calendar.getInstance()


        val dateTimeSetListener =
            DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                val startCalendar = Calendar.getInstance()
                startCalendar.set(year, monthOfYear, dayOfMonth)
                val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hour, minute ->
                    startCalendar.set(Calendar.HOUR_OF_DAY, hour)
                    startCalendar.set(Calendar.MINUTE, minute)
                    startCalendar.set(Calendar.SECOND, 0)
                    startCalendar.set(Calendar.MILLISECOND, 0)
                    val myFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
                    val sdf = SimpleDateFormat(myFormat, Locale.US)
                    sdf.setTimeZone(TimeZone.getTimeZone("UTC"))
                    fragmentBinding?.datetime?.setText(sdf.format(startCalendar.time))
                }
                TimePickerDialog(
                    requireContext(), timeSetListener,
                    startCalendar.get(Calendar.HOUR_OF_DAY),
                    startCalendar.get(Calendar.MINUTE), true
                ).show()
            }


// Use the separate listeners
        fragmentBinding?.datetime?.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                DatePickerDialog(
                    requireContext(), dateTimeSetListener,
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            }
        }
        val participantsAdapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, participantsList)
        fragmentBinding?.participantsRecyclerView?.let {
            it.visibility = View.GONE // Hide RecyclerView as we will use ListView
        }
        val listView = ListView(requireContext())
        listView.adapter = participantsAdapter
        (fragmentBinding?.scrollView?.getChildAt(0) as LinearLayout).addView(listView)

        fragmentBinding?.addParticipantButton?.setOnClickListener {
            val participantName = fragmentBinding?.participantEditText?.text.toString().trim()
            if (participantName.isNotEmpty()) {
                participantsList.add(participantName)
                participantsAdapter.notifyDataSetChanged()
                fragmentBinding?.participantEditText?.text?.clear()
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
                            viewModel.changeDatetime(it.datetime.text.toString())
                            viewModel.changeType(it.type.text.toString())


//                            if (it.datetime.text.toString().isEmpty()) {
//                                viewModel.changeDatetime(null) // set finish to null if it is empty
//                            } else {
//                                viewModel.changeDatetime(it.finish.text.toString())
//                            }
                            if (it.link.text.toString().isEmpty()) {

                                viewModel.changeLink("") // set finish to null if it is empty
                            } else {
                                viewModel.changeLink(it.link.text.toString())
                            }
                            viewModel.saveEvent()
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