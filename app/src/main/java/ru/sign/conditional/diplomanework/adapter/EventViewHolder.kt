package ru.sign.conditional.diplomanework.adapter

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.util.Log
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import ru.sign.conditional.diplomanework.R
import ru.sign.conditional.diplomanework.databinding.CardEventBinding
import ru.sign.conditional.diplomanework.dto.AttachmentType
import ru.sign.conditional.diplomanework.dto.Event
import ru.sign.conditional.diplomanework.dto.EventType
import ru.sign.conditional.diplomanework.dto.Payload
import ru.sign.conditional.diplomanework.util.NeWorkHelper.datetimeCustomRepresentation
import ru.sign.conditional.diplomanework.util.NeWorkHelper.itemsCount
import ru.sign.conditional.diplomanework.util.NeWorkHelper.loadImage

class EventViewHolder(
    private val binding: CardEventBinding,
    private val onEventInteractionListener: OnEventInteractionListener
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(event: Event) {
        fillingCardEvent(event)
        setupListeners(event)
    }

    fun bind(payload: Payload, event: Event?) {
        fillingCardEvent(payload)
        if (event != null)
            setupListenersGivenPayload(event)
    }

    private fun fillingCardEvent(event: Event) {
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
            content.text = event.content
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

    private fun setupListeners(event: Event) {
        binding.apply {
            setCustomOnClickListener(viewEvent) {
                onEventInteractionListener.onShowSingleEvent(event)
            }
            setCustomOnClickListener(attendIn) {
                attendAnimation()
                onEventInteractionListener.onAttend(event)
            }
            setCustomOnClickListener(eventAttachment, mediaType) {
                onEventInteractionListener.onShowAttachment(event)
            }
            menu.setOnClickListener { view ->
                PopupMenu(view.context, view).apply {
                    inflate(R.menu.event_options)
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.edit_event -> {
                                onEventInteractionListener.onEdit(event)
                                true
                            }
                            R.id.remove_event -> {
                                onEventInteractionListener.onRemove(event)
                                true
                            }
                            else -> false
                        }
                    }
                }.show()
            }
        }
    }

    private fun setupListenersGivenPayload(event: Event) {
        binding.apply {
            setCustomOnClickListener(attendIn) {
                attendAnimation()
                onEventInteractionListener.onAttend(event)
            }
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

    private fun attendAnimation() {
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
    }
}