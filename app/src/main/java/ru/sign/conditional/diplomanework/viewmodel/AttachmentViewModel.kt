package ru.sign.conditional.diplomanework.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ru.sign.conditional.diplomanework.dto.FeedItem
import ru.sign.conditional.diplomanework.dto.Post

class AttachmentViewModel: ViewModel() {
    private val emptyFeedItem: FeedItem = Post(
        id = 0,
        author = "",
        content = "",
        published = ""
    )
    private val _viewAttachment = MutableLiveData(emptyFeedItem)
    val viewAttachment: LiveData<FeedItem>
        get() = _viewAttachment

    fun showAttachment(item: FeedItem) { _viewAttachment.value = item }

    fun clearAttachment() { _viewAttachment.value = emptyFeedItem }
}