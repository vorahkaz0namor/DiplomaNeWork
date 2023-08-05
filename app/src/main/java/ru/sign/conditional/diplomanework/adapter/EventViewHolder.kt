package ru.sign.conditional.diplomanework.adapter

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.util.Log
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import ru.sign.conditional.diplomanework.R
import ru.sign.conditional.diplomanework.databinding.CardEventBinding
import ru.sign.conditional.diplomanework.dto.AttachmentType
import ru.sign.conditional.diplomanework.dto.Event
import ru.sign.conditional.diplomanework.dto.EventType
import ru.sign.conditional.diplomanework.dto.Payload
import ru.sign.conditional.diplomanework.util.NeWorkHelper.datetimeCustomRepresentation
import ru.sign.conditional.diplomanework.util.NeWorkHelper.getEventNameFromContent
import ru.sign.conditional.diplomanework.util.NeWorkHelper.itemsCount
import ru.sign.conditional.diplomanework.util.NeWorkHelper.loadImage
import ru.sign.conditional.diplomanework.util.NeWorkHelper.setFeedItemMenu

class EventViewHolder(
    private val binding: CardEventBinding,
    private val onEventInteractionListener: OnEventInteractionListener
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(event: Event) {
        fillingCardEvent(event)
        setupListeners(
            event = event,
            actionByViewEvent = {
                onEventInteractionListener.onShowSingleEvent(event)
            }
        )
    }

    fun bind(payload: Payload, event: Event?) {
        fillingCardEvent(payload)
        if (event != null)
            setupAttendInListener(event)
    }

    fun bindSingleEvent(event: Event) {
        fillingSingleCardEvent(event)
        setupListeners(
            event = event,
            actionByViewEvent = {
                if (binding.menu.hasOnClickListeners())
                    binding.menu.performClick()
            }
        )
    }

    private fun commonFillingCardEvent(event: Event) {
        binding.apply {
            val attachment = event.attachment
            if (attachment != null) {
                val imageValidation = attachment.type == AttachmentType.IMAGE
                eventAttachment.isVisible = imageValidation
                if (imageValidation) {
                    eventAttachment.loadImage(
                        url = attachment.url,
                        type = attachment.type.name
                    )
                }
                mediaType.isVisible = !imageValidation
                if (attachment.type == AttachmentType.VIDEO)
                    mediaType.setImageResource(R.drawable.ic_video_attachment_48)
                if (attachment.type == AttachmentType.AUDIO)
                    mediaType.setImageResource(R.drawable.ic_audio_attachment_48)
            } else {
                eventAttachment.isVisible = false
                mediaType.isVisible = false
            }
            datetime.text = datetimeCustomRepresentation(event.datetime)
            eventType.apply {
                text = event.type.name
                setIconTintResource(
                    if (event.type == EventType.ONLINE)
                        R.color.green
                    else
                        R.color.red
                )
            }
            author.text = event.author
            participants.apply {
                text = context.getString(
                    R.string.participants_count,
                    itemsCount(event.participantsIds.size)
                )
            }
            attendIn.isChecked = event.participatedByMe
            menu.isVisible = event.ownedByMe
        }
    }

    private fun fillingCardEvent(event: Event) {
        commonFillingCardEvent(event)
        binding.eventName.text = getEventNameFromContent(event.content)
    }

    private fun fillingCardEvent(payload: Payload) {
        payload.apply {
            participatedByMe?.let {
                binding.attendIn.isChecked = it
            }
            participantsIds?.let {
                binding.participants.apply {
                    text = context.getString(
                        R.string.participants_count,
                        itemsCount(it.size)
                    )
                }
            }
        }
    }

    private fun fillingSingleCardEvent(event: Event) {
        commonFillingCardEvent(event)
        binding.apply {
            eventName.isVisible = false
            eventContent.isVisible = true
            eventContent.text = event.content
            viewEvent.isVisible = event.ownedByMe
            viewEvent.setText(R.string.event_menu_title)
        }
    }

    private fun setupListeners(
        event: Event,
        actionByViewEvent: () -> Unit
    ) {
        setupAttendInListener(event)
        binding.apply {
            setCustomOnClickListener(eventAttachment, mediaType) {
                onEventInteractionListener.onShowAttachment(event)
            }
            setCustomOnClickListener(viewEvent) { actionByViewEvent() }
            menu.setOnClickListener { view ->
                view.setFeedItemMenu(
                    actionEdit = { onEventInteractionListener.onEdit(event) },
                    actionRemove = { onEventInteractionListener.onRemove(event) }
                )
            }
        }
    }

    private fun setupAttendInListener(event: Event) {
        setCustomOnClickListener(binding.attendIn) {
            ObjectAnimator.ofPropertyValuesHolder(
                binding.attendIn,
                PropertyValuesHolder.ofFloat(View.SCALE_X, 1.2F, 1F),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, 1.2F, 1F)
            )
                .apply {
                    duration = 300
                    interpolator = LinearInterpolator()
                }
                .start()
            onEventInteractionListener.onAttend(event)
        }
    }

    private fun setCustomOnClickListener(vararg view: View, action: () -> Unit) {
        view.map {
            onEventInteractionListener.apply {
                it.setOnClickListener {
                    checkAuth()
                    if (authorized)
                        action()
                    else
                        if (it == binding.attendIn)
                            binding.attendIn.isChecked = false
                }
            }
        }
    }
}