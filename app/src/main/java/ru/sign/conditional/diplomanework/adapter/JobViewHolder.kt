package ru.sign.conditional.diplomanework.adapter

import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import ru.sign.conditional.diplomanework.R
import ru.sign.conditional.diplomanework.databinding.CardJobBinding
import ru.sign.conditional.diplomanework.dto.Job
import ru.sign.conditional.diplomanework.util.NeWorkHelper.jobDatetimeCustomRepresentation
import ru.sign.conditional.diplomanework.util.NeWorkHelper.jobDurationCount
import ru.sign.conditional.diplomanework.util.NeWorkHelper.setFeedItemMenu

class JobViewHolder(
    private val binding: CardJobBinding,
    private val onJobInteractionListener: OnJobInteractionListener
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(job: Job) {
        fillingCardJob(job)
        setupListeners(job)
    }
    
    private fun fillingCardJob(job: Job) {
        binding.apply { 
            positionName.text = job.position
            companyName.text = job.name
            workStart.text = jobDatetimeCustomRepresentation(job.start)
            job.finish?.let {
                workFinish.text = jobDatetimeCustomRepresentation(it)
            } ?: workFinish.setText(R.string.work_until_present)
            workDuration.text = workDuration.jobDurationCount(
                start = job.start,
                finish = job.finish
            )
            link.isVisible = job.link != null
            link.text = job.link
        }
    }
    
    private fun setupListeners(job: Job) {
        binding.menu.setOnClickListener { view ->
            view.setFeedItemMenu(
                actionEdit = { onJobInteractionListener.onEdit(job) },
                actionRemove = { onJobInteractionListener.onRemove(job) }
            )
        }
    }
}