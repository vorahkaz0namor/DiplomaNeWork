package ru.sign.conditional.diplomanework.adapter

import androidx.recyclerview.widget.DiffUtil
import ru.sign.conditional.diplomanework.dto.Job

class JobCallback : DiffUtil.ItemCallback<Job>() {
    override fun areItemsTheSame(oldItem: Job, newItem: Job): Boolean =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: Job, newItem: Job): Boolean =
        oldItem == newItem
}