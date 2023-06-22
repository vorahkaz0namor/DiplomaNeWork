package ru.sign.conditional.diplomanework.activity

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import ru.sign.conditional.diplomanework.R
import ru.sign.conditional.diplomanework.databinding.FragmentAttachmentBinding
import ru.sign.conditional.diplomanework.dto.AttachmentType
import ru.sign.conditional.diplomanework.dto.Post
import ru.sign.conditional.diplomanework.util.NeWorkHelper.loadImage
import ru.sign.conditional.diplomanework.util.viewBinding
import ru.sign.conditional.diplomanework.viewmodel.PostViewModel

class AttachmentFragment : Fragment(R.layout.fragment_attachment) {
    private val postViewModel: PostViewModel by activityViewModels()
    private val binding by viewBinding(FragmentAttachmentBinding::bind)
    private val post: Post
        get() = postViewModel.viewAttachment.value!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            customNavigateUp()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        setupListeners()
    }

    private fun initViews() {
        binding.apply {
            val attachmentValidation = post.attachment?.type != AttachmentType.AUDIO
            attachmentPreview.isVisible = attachmentValidation
            attachmentPreview.loadImage(
                url = post.attachment!!.url,
                type = post.attachment!!.type.name
            )
            playButton.isVisible = post.attachment?.type == AttachmentType.VIDEO
        }
    }

    private fun setupListeners() {
        binding.playButton.setOnClickListener {

        }
    }

    private fun customNavigateUp() {
        postViewModel.clearAttachment()
        findNavController().navigateUp()
    }
}