package ru.sign.conditional.diplomanework.adapter

import ru.sign.conditional.diplomanework.dto.Event
import ru.sign.conditional.diplomanework.viewmodel.AttachmentViewModel
import ru.sign.conditional.diplomanework.viewmodel.AuthViewModel
import ru.sign.conditional.diplomanework.viewmodel.EventViewModel

class OnEventInteractionListenerImpl(
    private val eventViewModel: EventViewModel,
    private val attachmentViewModel: AttachmentViewModel,
    private val authViewModel: AuthViewModel
) : OnInteractionListenerImpl(authViewModel),
    OnEventInteractionListener {
    override fun onAttend(event: Event) {
        eventViewModel.editParticipantsInEventById(event)
    }

    override fun onEdit(event: Event) {
        eventViewModel.setEditEvent(event)
    }

    override fun onRemove(event: Event) {
        eventViewModel.removeEventById(event)
    }

    override fun onShowAttachment(event: Event) {
        attachmentViewModel.showAttachment(event)
    }

    override fun onShowSingleEvent(event: Event) {
        eventViewModel.getEventById(event.id)
    }
}