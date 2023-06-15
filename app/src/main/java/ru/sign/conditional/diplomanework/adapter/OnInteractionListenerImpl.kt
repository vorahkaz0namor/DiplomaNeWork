package ru.sign.conditional.diplomanework.adapter

import ru.sign.conditional.diplomanework.dto.Post
import ru.sign.conditional.diplomanework.viewmodel.AuthViewModel
import ru.sign.conditional.diplomanework.viewmodel.PostViewModel

class OnInteractionListenerImpl(
    private val postViewModel: PostViewModel,
    private val authViewModel: AuthViewModel
) : OnInteractionListener {
    override val authorized: Boolean
        get() = authViewModel.authorized

    override fun checkAuth() {
        authViewModel.checkAuth()
    }

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
        postViewModel.showAttachment(post)
    }

    override fun onShowSinglePost(post: Post) {
        postViewModel.getPostById(post.id)
    }
}