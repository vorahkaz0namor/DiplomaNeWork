package ru.sign.conditional.diplomanework.activity

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import ru.sign.conditional.diplomanework.R
import ru.sign.conditional.diplomanework.databinding.FragmentAttachmentBinding
import ru.sign.conditional.diplomanework.dto.Attachment
import ru.sign.conditional.diplomanework.dto.AttachmentType
import ru.sign.conditional.diplomanework.observer.ExoMediaLifecycleObserver
import ru.sign.conditional.diplomanework.util.NeWorkHelper.loadImage
import ru.sign.conditional.diplomanework.util.viewBinding
import ru.sign.conditional.diplomanework.viewmodel.AttachmentViewModel

class AttachmentFragment : Fragment(R.layout.fragment_attachment) {
    private val attachmentViewModel: AttachmentViewModel by activityViewModels()
    private val binding by viewBinding(FragmentAttachmentBinding::bind)
    private val attachment: Attachment?
        get() = attachmentViewModel.viewAttachment.value?.attachment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            attachmentViewModel.clearAttachment()
        }
        if (attachment != null) {
            if (attachment!!.type != AttachmentType.IMAGE)
                ExoMediaLifecycleObserver(
                    context = requireContext(),
                    attachmentUrl = attachment!!.url
                ) { player ->
                    binding.exoplayerView.player = player
                }
                    .also { observer ->
                        lifecycle.addObserver(observer)
                    }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            attachmentPreview.isVisible = attachment?.type == AttachmentType.IMAGE
            attachmentPreview.loadImage(
                url = attachment!!.url,
                type = attachment!!.type.name
            )
            exoplayerView.isVisible = attachment?.type != AttachmentType.IMAGE
        }
    }
}