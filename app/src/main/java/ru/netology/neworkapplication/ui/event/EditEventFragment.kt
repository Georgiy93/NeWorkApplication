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
import ru.netology.neworkapplication.ui.FeedFragment
import ru.netology.neworkapplication.util.AndroidUtils
import ru.netology.neworkapplication.util.StringArg
import ru.netology.neworkapplication.viewmodel.EventViewModel
import java.text.SimpleDateFormat
import java.util.*


@AndroidEntryPoint
class EditEventFragment : Fragment() {

    companion object {
        var Bundle.textArg: String? by StringArg


        fun newInstance(
            id: Int,
            content: String,
            link: String,
            type: String,
            date: String
        ): EditEventFragment {
            val fragment = EditEventFragment()


            val args = Bundle().apply {
                putInt("id", id)
                putString("content", content)
                putString("link", link)
                putString("type", type)
                putString("date", date)
            }


            fragment.arguments = args
            return fragment
        }
    }

    private val viewModel: EventViewModel by activityViewModels()
    private var participantsAdapter: ArrayAdapter<String>? = null
    private val participantsList = mutableListOf<String>()
    private var fragmentBinding: FragmentNewEventBinding? = null
    private var eventId: Int = 0
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

        eventId = arguments?.getInt("id") ?: 0
        eventContent = arguments?.getString("content") ?: ""
        eventLink = arguments?.getString("link") ?: ""
        eventType = arguments?.getString("type") ?: ""
        dateTime = arguments?.getString("date") ?: ""
        binding.edit.setText(eventContent)
        binding.link.setText(eventLink)
        binding.type.setText(eventType)
        binding.datetime.setText(dateTime)

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


        viewModel.editedEvent.observe(viewLifecycleOwner) { event ->
            fragmentBinding?.edit?.setText(event.content)
            fragmentBinding?.link?.setText(event.link)
            fragmentBinding?.type?.setText(event.type)
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
                    val sdf = SimpleDateFormat(myFormat, Locale("ru", "RU"))
                    sdf.setTimeZone(TimeZone.getTimeZone("Europe/Moscow"))
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
                            viewModel.changeContent(it.edit.text.toString())


                            if (it.link.text.toString().isEmpty()) {

                                viewModel.changeLink(null) // set finish to null if it is empty
                            } else {
                                viewModel.changeLink(it.link.text.toString())
                            }
                            viewModel.saveEvent()
                            AndroidUtils.hideKeyboard(requireView())
                        }
                        parentFragmentManager.commit {
                            replace(R.id.container, EventFragment())
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
                    "Please enter a participant login",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }
        binding.participantEditText.text?.clear()

        listView.setOnItemLongClickListener { _, _, position, _ ->
            val participant = participantsList[position]
            AlertDialog.Builder(requireContext())
                .setMessage("Do you want to remove $participant?")
                .setPositiveButton("Yes") { _, _ ->
                    viewModel.removeParticipantByName(participant)
                    participantsList.removeAt(position)
                    participantsAdapter?.notifyDataSetChanged()
                }
                .setNegativeButton("No", null)
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
            .setTitle("Как пользоваться")
            .setMessage(
                "Здесь вы можете создать новое событие. " +
                        "В 1ой строке введите дату события. Далее нажмите и выберите тип события. " +
                        "Далее напишите описание события. " + "При необходимости добавте участников, ссылку и картинку." +
                        " Для удаления участника зажмите его имя или логин, после подтвердите удаление."
            )
            .setPositiveButton("Понятно", null)
            .show()
    }
}
