package ru.sign.conditional.diplomanework.activity

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.addCallback
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.collectLatest
import okhttp3.internal.http.HTTP_OK
import ru.sign.conditional.diplomanework.R
import ru.sign.conditional.diplomanework.adapter.EventViewHolder
import ru.sign.conditional.diplomanework.adapter.OnEventInteractionListenerImpl
import ru.sign.conditional.diplomanework.databinding.FragmentSingleEventBinding
import ru.sign.conditional.diplomanework.dto.Event
import ru.sign.conditional.diplomanework.dto.Payload
import ru.sign.conditional.diplomanework.util.AndroidUtils.viewScopeWithRepeat
import ru.sign.conditional.diplomanework.util.NeWorkHelper.overview
import ru.sign.conditional.diplomanework.util.viewBinding
import ru.sign.conditional.diplomanework.viewmodel.AttachmentViewModel
import ru.sign.conditional.diplomanework.viewmodel.AuthViewModel
import ru.sign.conditional.diplomanework.viewmodel.EventViewModel

class SingleEventFragment : Fragment(R.layout.fragment_single_event) {
    private val binding by viewBinding(FragmentSingleEventBinding::bind)
    private val eventViewModel: EventViewModel by activityViewModels()
    private val attachmentViewModel: AttachmentViewModel by activityViewModels()
    private val authViewModel: AuthViewModel by activityViewModels()
    private lateinit var bindEvent: (Event?, Event) -> Unit

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            findNavController().navigateUp()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        subscribe()
    }

    private fun init() {
        viewScopeWithRepeat {
            bindEvent = { oldItem: Event?, newItem: Event ->
                if (oldItem != newItem) {
                    EventViewHolder(
                        binding.singleEvent,
                        OnEventInteractionListenerImpl(
                            eventViewModel = eventViewModel,
                            attachmentViewModel = attachmentViewModel,
                            authViewModel = authViewModel
                        )
                    ).let { holder ->
                        oldItem?.let {
                            val payload = Payload(
                                id = newItem.id,
                                participantsIds = newItem.participantsIds.takeIf { it != oldItem.participantsIds },
                                participatedByMe = newItem.participatedByMe.takeIf { it != oldItem.participatedByMe }
                            )
                            holder.bind(
                                payload = payload,
                                event = oldItem.copy(
                                    participantsIds = payload.participantsIds ?: oldItem.participantsIds,
                                    participatedByMe = payload.participatedByMe ?: oldItem.participatedByMe
                                )
                            )
                        } ?: holder.bindSingleEvent(newItem)
                    }
                }
            }
        }
    }

    private fun subscribe() {
        eventViewModel.apply {
            viewScopeWithRepeat {
                var oldItem: Event? = null
                singleEvent.collectLatest { event ->
                    event?.let { newItem ->
                        bindEvent(oldItem, newItem)
                        oldItem = newItem
                    } ?: findNavController().navigateUp()
                }
            }
            eventOccurrence.observe(viewLifecycleOwner) { code ->
                if (code != HTTP_OK) {
                    binding.apply {
                        scrollView.isVisible = false
                        progressBarView.progressBar.isVisible = true
                    }
                    Snackbar.make(
                        binding.root,
                        overview(code),
                        Snackbar.LENGTH_INDEFINITE
                    )
                        .setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE)
                        .setAction(android.R.string.ok) {
                            findNavController().navigateUp()
                        }
                        .show()
                }
            }
            edited.observe(viewLifecycleOwner) { event ->
                if (event.id != 0)
                    findNavController().navigate(
                        R.id.action_singleEventFragment_to_editEventFragment
                    )
            }
        }
        attachmentViewModel.viewAttachment.observe(viewLifecycleOwner) { item ->
            if (item.id != 0)
                findNavController().navigate(
                    R.id.action_singleEventFragment_to_attachmentFragment
                )
        }
    }
}