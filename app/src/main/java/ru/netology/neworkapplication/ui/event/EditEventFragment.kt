package ru.netology.neworkapplication.ui.event

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.ListView
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
import ru.netology.neworkapplication.databinding.FragmentNewEventBinding
import ru.netology.neworkapplication.dto.EventType
import ru.netology.neworkapplication.util.AndroidUtils
import ru.netology.neworkapplication.viewmodel.EventViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone


@AndroidEntryPoint
class EditEventFragment : Fragment() {

    companion object {
        const val KEY_ID = "id"
        const val KEY_CONTENT = "content"
        const val KEY_LINK = "link"
        const val KEY_TYPE = "type"
        const val KEY_DATE = "date"

        fun newInstance(
            id: Int,
            content: String,
            link: String,
            type: String,
            date: String
        ): EditEventFragment {
            val fragment = EditEventFragment()

            val args = Bundle().apply {
                putInt(KEY_ID, id)
                putString(KEY_CONTENT, content)
                putString(KEY_LINK, link)
                putString(KEY_TYPE, type)
                putString(KEY_DATE, date)
            }

            fragment.arguments = args
            return fragment
        }
    }

    private val viewModel: EventViewModel by activityViewModels()
    private var participantsAdapter: ArrayAdapter<String>? = null
    private val participantsList = mutableListOf<String>()
    private var fragmentBinding: FragmentNewEventBinding? = null
    private var eventId: Long = 0
    private var eventContent: String = ""
    private var eventType: String = ""
    private var eventLink: String = ""
    private var dateTime: String = ""
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

        eventId = arguments?.getLong(KEY_ID) ?: 0
        eventContent = arguments?.getString(KEY_CONTENT) ?: ""
        eventLink = arguments?.getString(KEY_LINK) ?: ""
        eventType = arguments?.getString(KEY_TYPE) ?: ""
        dateTime = arguments?.getString(KEY_DATE) ?: ""
        binding.edit.setText(eventContent)
        binding.link.setText(eventLink)
        binding.type.text = eventType
        binding.datetime.setText(dateTime)

        fragmentBinding?.type?.setOnClickListener { _ ->
            // list of event types
            val eventTypes = arrayOf(EventType.OFFLINE, EventType.ONLINE)
            val eventTypeNames = eventTypes.map { it.toString() }.toTypedArray()
            // create a dialog
            AlertDialog.Builder(requireContext())
                .setTitle(R.string.event_type)
                .setSingleChoiceItems(eventTypeNames, -1) { dialog, which ->
                    val eventType = eventTypes[which]
                    fragmentBinding?.type?.text = eventType.toString()
                    dialog.dismiss()
                }
                .setNegativeButton(R.string.cancel) { dialog, _ ->
                    dialog.cancel()
                }
                .show()
        }


        viewModel.editedEvent.observe(viewLifecycleOwner) { event ->
            fragmentBinding?.edit?.setText(event.content)
            fragmentBinding?.link?.setText(event.link)
            fragmentBinding?.type?.text = event.type
            fragmentBinding?.datetime?.setText(event.datetime)

            val participantsNames = viewModel.getParticipantNamesForEvent(event.id)
            participantsList.clear()
            participantsList.addAll(participantsNames)
            participantsAdapter?.notifyDataSetChanged()
        }

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
                    val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
                    sdf.timeZone = TimeZone.getDefault()
                    fragmentBinding?.datetime?.setText(sdf.format(startCalendar.time))
                }
                TimePickerDialog(
                    requireContext(), timeSetListener,
                    startCalendar.get(Calendar.HOUR_OF_DAY),
                    startCalendar.get(Calendar.MINUTE), true
                ).show()
            }


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
        AndroidUtils.setupEventMenu(this, viewLifecycleOwner, viewModel)
        binding.clear.setOnClickListener {
            viewModel.clearPhoto()
        }
        participantsAdapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, participantsList)
        val listView = ListView(requireContext())
        listView.adapter = participantsAdapter

        (binding.scrollView.getChildAt(0) as LinearLayout).addView(listView)

        binding.addParticipantButton.setOnClickListener {
            val participantLogin = binding.participantEditText.text.toString().trim()
            if (participantLogin.isNotEmpty()) {
                viewModel.addParticipant(participantLogin)
                participantsList.add(participantLogin)
                participantsAdapter?.notifyDataSetChanged()
            } else {
                Snackbar.make(
                    binding.root,
                    getString(R.string.participant_login),
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }
        binding.participantEditText.text?.clear()

        listView.setOnItemLongClickListener { _, _, position, _ ->
            val participant = participantsList[position]
            AlertDialog.Builder(requireContext())
                .setMessage(getString(R.string.remove_participant_confirmation, participant))
                .setPositiveButton(R.string.yes) { _, _ ->
                    viewModel.removeParticipantByName(participant)
                    participantsList.removeAt(position)
                    participantsAdapter?.notifyDataSetChanged()
                }
                .setNegativeButton(R.string.no, null)
                .show()
            true
        }

        return binding.root
    }

    override fun onDestroyView() {
        fragmentBinding = null
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        AlertDialog.Builder(requireContext())
            .setTitle(
                getString(R.string.event_instruction_title)
            )
            .setMessage(getString(R.string.event_creation_instructions))
            .setPositiveButton(R.string.understand, null)
            .show()
    }
}
