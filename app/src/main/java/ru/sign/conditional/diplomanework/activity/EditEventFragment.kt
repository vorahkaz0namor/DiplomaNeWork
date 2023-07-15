package ru.sign.conditional.diplomanework.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch
import okhttp3.internal.http.HTTP_OK
import ru.sign.conditional.diplomanework.R
import ru.sign.conditional.diplomanework.databinding.FragmentEditEventBinding
import ru.sign.conditional.diplomanework.dto.AttachmentType
import ru.sign.conditional.diplomanework.dto.DraftCopy
import ru.sign.conditional.diplomanework.dto.Event
import ru.sign.conditional.diplomanework.dto.EventType
import ru.sign.conditional.diplomanework.util.AndroidUtils
import ru.sign.conditional.diplomanework.util.AndroidUtils.createImageLauncher
import ru.sign.conditional.diplomanework.util.AndroidUtils.viewScope
import ru.sign.conditional.diplomanework.util.NeWorkDatetime
import ru.sign.conditional.diplomanework.util.NeWorkHelper.loadImage
import ru.sign.conditional.diplomanework.util.NeWorkHelper.overview
import ru.sign.conditional.diplomanework.util.viewBinding
import ru.sign.conditional.diplomanework.viewmodel.EventViewModel

class EditEventFragment : Fragment(R.layout.fragment_edit_event) {
    private val binding by viewBinding(FragmentEditEventBinding::bind)
    private val eventViewModel: EventViewModel by activityViewModels()
    private val event: Event?
        get() = eventViewModel.edited.value
    private var eventIsBlank: Boolean = false
    private lateinit var imageLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            customNavigateUp(
                DraftCopy(
                    eventId = event?.id ?: 0,
                    eventContent = binding.description.editText?.text.toString()
                )
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        subscribe()
        setupListeners()
    }

    private fun initViews() {
        val draftCopy = eventViewModel.draftCopy
        binding.apply {
            description.editText?.setText(
                if (draftCopy != null) {
                    if (draftCopy.eventId == event?.id)
                        draftCopy.eventContent
                    else
                        event?.content
                } else
                    event?.content
            )
        }
        imageLauncher =
            createImageLauncher(binding.root) { uri, file ->
                eventViewModel.setImage(uri = uri, file = file)
            }
    }

    private fun subscribe() {
        eventViewModel.apply {
            media.observe(viewLifecycleOwner) { model ->
                val attachment = event?.attachment
                val attachmentValidation =
                    attachment != null &&
                            attachment.type == AttachmentType.IMAGE
                binding.addCoverEvent.isVisible = model == null && attachment == null
                binding.clearCover.isVisible = !binding.addCoverEvent.isVisible
                binding.coverEvent.apply {
                    isVisible = model != null || attachmentValidation
                    when {
                        model != null -> setImageURI(model.uri)
                        attachmentValidation -> {
                            loadImage(
                                url = attachment!!.url,
                                type = attachment.type.name
                            )
                        }
                    }
                }
                eventBlankValidation()
            }
            edited.observe(viewLifecycleOwner) {
                binding.onlineChooser.isChecked = event?.type == EventType.ONLINE
                binding.offlineChooser.isChecked = event?.type == EventType.OFFLINE
            }
            datetimeForLayout.observe(viewLifecycleOwner) { datetime ->
                binding.datetimeInput.apply {
                    setDatetimeToView(startYear, datetime?.year)
                    setDatetimeToView(startMonth, datetime?.month)
                    setDatetimeToView(startDay, datetime?.day)
                    setDatetimeToView(startHour, datetime?.hour)
                    setDatetimeToView(startMinute, datetime?.minute)
                }
                eventBlankValidation()
            }
            datetimeIsntValid.observe(viewLifecycleOwner) {
                binding.apply {
                    if (!eventIsBlank && !it) {
                        AndroidUtils.hideKeyboard(root)
                        editEventGroup.isVisible = false
                        progressBarView.progressBar.isVisible = true
                        eventViewModel.saveEvent(description.editText?.text.toString())
                    } else {
                        Snackbar.make(
                            root,
                            if (eventIsBlank)
                                R.string.empty_event_card
                            else
                                R.string.incorrect_datetime,
                            Snackbar.LENGTH_LONG
                        )
                            .setTextMaxLines(3)
                            .setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE)
                            .show()
                    }
                }
            }
            eventOccurrence.observe(viewLifecycleOwner) { code ->
                event?.let { event ->
                    binding.apply {
                            Toast.makeText(
                                context,
                                root.context.getString(
                                    if (code == HTTP_OK) {
                                        if (event.id == 0)
                                            R.string.new_event_was_created
                                        else
                                            R.string.the_event_was_saved
                                    } else {
                                        /* resId = */ R.string.error_saving_event
                                    },
                                    /* ...formatArgs = */ overview(code)
                                ),
                                Toast.LENGTH_LONG
                            ).show()
                    }
                }
                customNavigateUp(null)
            }
        }
    }

    private fun setupListeners() {
        binding.apply {
            requireActivity().addMenuProvider(
                /* provider = */ object : MenuProvider {
                    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                        menuInflater.inflate(R.menu.edit_item_menu, menu)
                    }

                    override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
                        when (menuItem.itemId) {
                            R.id.publish -> {
                                eventViewModel.datetimeValidationBeforeSaveEvent()
                                true
                            }
                            else -> false
                        }
                },
                /* owner = */ viewLifecycleOwner
            )
            addCoverEvent.setOnClickListener {
                ImagePicker.with(this@EditEventFragment)
                    .galleryOnly()
                    .crop()
                    .compress(2048)
                    .createIntent(imageLauncher::launch)
            }
            clearCover.setOnClickListener {
                eventViewModel.clearImage()
            }
            onlineChooser.setOnClickListener {
                eventViewModel.setType(EventType.ONLINE)
            }
            offlineChooser.setOnClickListener {
                eventViewModel.setType(EventType.OFFLINE)
            }
            datetimeInput.apply {
                setCustomTextChangedListener(
                    startYear,
                    startMonth,
                    startDay,
                    startHour,
                    startMinute
                )
            }
            description.editText?.addTextChangedListener {
                eventBlankValidation()
            }
        }
    }

    private fun setCustomTextChangedListener(vararg view: TextInputLayout) {
        view.map {
            viewScope.launch {
                it.editText?.addTextChangedListener {
                    binding.datetimeInput.apply {
                        eventViewModel.setDatetime(
                            NeWorkDatetime(
                                year = startYear.editText?.text ?: "",
                                month = startMonth.editText?.text ?: "",
                                day = startDay.editText?.text ?: "",
                                hour = startHour.editText?.text ?: "",
                                minute = startMinute.editText?.text ?: ""
                            )
                        )
                    }
                }
            }
        }
    }

    private fun setDatetimeToView(view: TextInputLayout, text: CharSequence?) {
        view.editText?.let { content ->
            if (!content.text.contentEquals(text))
                content.setText(text)
        }
    }

    private fun eventBlankValidation() {
        viewScope.launch {
            eventIsBlank =
                binding.description.editText?.text.isNullOrBlank()
                    .or(binding.datetimeInput.startYear.editText?.text.isNullOrBlank())
                    .or(binding.datetimeInput.startMonth.editText?.text.isNullOrBlank())
                    .or(binding.datetimeInput.startDay.editText?.text.isNullOrBlank())
                    .or(binding.datetimeInput.startHour.editText?.text.isNullOrBlank())
                    .or(binding.datetimeInput.startMinute.editText?.text.isNullOrBlank())
                    .or(
                        (eventViewModel.media.value == null)
                            .and(event?.attachment == null)
                    )
        }
    }

    private fun customNavigateUp(draftCopy: DraftCopy?) {
        eventViewModel.apply {
            saveDraftCopy(draftCopy)
            clearEditEvent()
            clearDatetime()
            clearImage()
        }
        findNavController().navigateUp()
    }
}