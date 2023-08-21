package ru.sign.conditional.diplomanework.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import ru.sign.conditional.diplomanework.databinding.CardJobBinding
import ru.sign.conditional.diplomanework.dto.Job

class JobAdapter(
    private val onJobInteractionListener: OnJobInteractionListener
) : ListAdapter<Job, JobViewHolder>(JobCallback()) {
    override fun onBindViewHolder(holder: JobViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobViewHolder =
        JobViewHolder(
            CardJobBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ),
            onJobInteractionListener
        )
}