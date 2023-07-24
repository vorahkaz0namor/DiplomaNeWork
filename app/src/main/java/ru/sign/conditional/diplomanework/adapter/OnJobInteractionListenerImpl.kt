package ru.sign.conditional.diplomanework.adapter

import ru.sign.conditional.diplomanework.dto.Job
import ru.sign.conditional.diplomanework.viewmodel.JobViewModel

class OnJobInteractionListenerImpl(
    private val jobViewModel: JobViewModel
) : OnJobInteractionListener {
    override fun onEdit(job: Job) {
        jobViewModel.setEditJob(job)
    }

    override fun onRemove(job: Job) {
        jobViewModel.removeJobById(job)
    }
}