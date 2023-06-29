package ru.sign.conditional.diplomanework.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toFile
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.snackbar.Snackbar
import okhttp3.internal.http.HTTP_OK
import ru.sign.conditional.diplomanework.R
import ru.sign.conditional.diplomanework.databinding.FragmentEditPostBinding
import ru.sign.conditional.diplomanework.dto.AttachmentType
import ru.sign.conditional.diplomanework.dto.DraftCopy
import ru.sign.conditional.diplomanework.dto.Post
import ru.sign.conditional.diplomanework.util.AndroidUtils
import ru.sign.conditional.diplomanework.util.NeWorkHelper.loadImage
import ru.sign.conditional.diplomanework.util.NeWorkHelper.overview
import ru.sign.conditional.diplomanework.util.viewBinding
import ru.sign.conditional.diplomanework.viewmodel.PostViewModel

class EditPostFragment : Fragment(R.layout.fragment_edit_post) {
    private val binding by viewBinding(FragmentEditPostBinding::bind)
    private val postViewModel: PostViewModel by activityViewModels()
    private val post: Post?
        get() = postViewModel.edited.value
    private lateinit var imageLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            customNavigateUp(
                DraftCopy(
                    postId = post?.id ?: 0,
                    postContent = binding.postContent.text.toString()
                )
            )
        }
    }

    override fun onStop() {
        AndroidUtils.hideKeyboard(binding.postContent)
        super.onStop()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        subscribe()
        setupListeners()
    }

    private fun initViews() {
        val draftCopy = postViewModel.draftCopy
        binding.postContent.setText(
            if (draftCopy != null) {
                if (draftCopy.postId == post?.id)
                    draftCopy.postContent
                else
                    post?.content
            } else
                post?.content
        )
        imageLauncher =
            registerForActivityResult(
                ActivityResultContracts.StartActivityForResult()
            ) {
                if (it.resultCode == ImagePicker.RESULT_ERROR)
                    Snackbar.make(
                        binding.root,
                        getString(R.string.error_loading),
                        Snackbar.LENGTH_LONG
                    )
                        .setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE)
                        .show()
                else {
                    val uri = it.data?.data
                        ?: return@registerForActivityResult
                    postViewModel.setImage(
                        uri = uri,
                        file = uri.toFile()
                    )
                }
            }
    }

    private fun subscribe() {
        postViewModel.apply {
            media.observe(viewLifecycleOwner) {
                val attachment = post?.attachment
                val attachmentValidation =
                    attachment != null &&
                            attachment.type != AttachmentType.AUDIO
                Log.d("LOGGING THE MEDIA MODEL",
                    "isPresent = ${it != null}\n" +
                            "uri = ${it?.uri}\n" +
                            "file length = ${it?.file?.length()}")
                Log.d("LOGGING THE ATTACHMENT",
                    "isPresent = ${attachment != null}\n" +
                            "url = ${attachment?.url}\n" +
                            "type = ${attachment?.type}")
                binding.previewContainer.isVisible = it != null || attachmentValidation
                binding.imagePreview.apply {
                    if (it != null) {
                        Log.d("MEDIA MODEL NOT NULL!", it.file.name)
                        setImageURI(it.uri)
                    }
                    else {
                        if (attachmentValidation) {
                            Log.d("ATTACHMENT NOT NULL?", attachment?.type.toString())
                            loadImage(
                                url = attachment!!.url,
                                type = attachment.type.name
                            )
                        }
                    }
                }
            }
            edited.observe(viewLifecycleOwner) {
                val link = it.link
                binding.addLink.isChecked = link != null
                binding.linkPreview.isVisible = link != null
                binding.linkPreview.editText?.setText(link)
            }
            postEvent.observe(viewLifecycleOwner) { code ->
                post?.let { post ->
                    binding.apply {
                        val newContent = postContent.text.toString()
                        if (code == HTTP_OK)
                            if (newContent != post.content)
                                Toast.makeText(
                                    context,
                                    root.context.getString(
                                        if (post.id == 0)
                                            R.string.new_post_was_created
                                        else
                                            R.string.the_post_was_edited
                                    ),
                                    Toast.LENGTH_LONG
                                ).show()
                            else
                                Toast.makeText(
                                    context,
                                    root.context.getString(
                                        if (post.idFromServer == 0)
                                            R.string.error_saving
                                        else
                                            R.string.error_editing,
                                        overview(code)
                                    ),
                                    Toast.LENGTH_LONG
                                ).show()
                    }
                    customNavigateUp(null)
                }
            }
        }
    }

    private fun setupListeners() {
        binding.apply {
            requireActivity().addMenuProvider(
                /* provider = */ object : MenuProvider {
                    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                        menuInflater.inflate(R.menu.edit_post_menu, menu)
                    }

                    override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
                        when (menuItem.itemId) {
                            R.id.publish_post -> {
                                if (postContent.text.isNullOrBlank()) {
                                    Snackbar.make(
                                        root,
                                        R.string.empty_content,
                                        Snackbar.LENGTH_LONG
                                    )
                                        .setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE)
                                        .show()
                                } else {
                                    AndroidUtils.hideKeyboard(postContent)
                                    editPostGroup.isVisible = false
                                    progressBarView.progressBar.isVisible = true
                                    postViewModel.savePost(
                                        text = postContent.text.toString(),
                                        link = linkPreview.editText?.text.toString()
                                    )
                                }
                                true
                            }
                            else -> false
                        }
                },
                /* owner = */ viewLifecycleOwner
            )
            imageFromGallery.setOnClickListener {
                ImagePicker.with(this@EditPostFragment)
                    .galleryOnly()
                    .crop()
                    .compress(2048)
                    .createIntent(imageLauncher::launch)
            }
            imageFromCamera.setOnClickListener {
                ImagePicker.with(this@EditPostFragment)
                    .cameraOnly()
                    .crop()
                    .compress(2048)
                    .createIntent(imageLauncher::launch)
            }
            deletePreview.setOnClickListener {
                postViewModel.clearImage()
            }
            addLink.setOnClickListener {
                if (post?.link != null)
                    postViewModel.clearLink()
                else
                    postViewModel.addLink()
            }
        }
    }

    private fun customNavigateUp(draftCopy: DraftCopy?) {
        postViewModel.apply {
            saveDraftCopy(draftCopy)
            clearEditPost()
            clearImage()
        }
        findNavController().navigateUp()
    }
}