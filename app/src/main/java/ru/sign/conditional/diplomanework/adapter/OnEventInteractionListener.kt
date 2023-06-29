package ru.sign.conditional.diplomanework.adapter

import ru.sign.conditional.diplomanework.dto.Event

interface OnEventInteractionListener : OnInteractionListener {
    fun onAttend(event: Event)
    fun onEdit(event: Event)
    fun onRepeatSave(event: Event)
    fun onRemove(event: Event)
    fun onShowAttachment(event: Event)
    fun onShowSingleEvent(event: Event)
}