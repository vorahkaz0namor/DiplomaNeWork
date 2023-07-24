package ru.sign.conditional.diplomanework.adapter

import ru.sign.conditional.diplomanework.dto.Job

interface OnJobInteractionListener {
    fun onEdit(job: Job)
    fun onRemove(job: Job)
}