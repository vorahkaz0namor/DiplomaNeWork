package ru.sign.conditional.diplomanework.adapter

import ru.sign.conditional.diplomanework.dto.Post
import ru.sign.conditional.diplomanework.viewmodel.AttachmentViewModel
import ru.sign.conditional.diplomanework.viewmodel.AuthViewModel
import ru.sign.conditional.diplomanework.viewmodel.PostViewModel

class OnPostInteractionListenerImpl(
    private val postViewModel: PostViewModel,
    private val attachmentViewModel: AttachmentViewModel,
    private val authViewModel: AuthViewModel
) : OnInteractionListenerImpl(authViewModel),
    OnPostInteractionListener {
    override fun onLike(post: Post) {
        postViewModel.likePostById(post)
    }

    override fun onEdit(post: Post) {
        postViewModel.setEditPost(post)
    }

    override fun onRepeatSave(post: Post) {
        postViewModel.repeatSavePost(post)
    }

    override fun onRemove(post: Post) {
        postViewModel.removePostById(post)
    }

    override fun onShowAttachment(post: Post) {
        attachmentViewModel.showAttachment(post)
    }
}