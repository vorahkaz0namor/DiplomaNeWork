package ru.sign.conditional.diplomanework.activity

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch
import okhttp3.internal.http.HTTP_OK
import ru.sign.conditional.diplomanework.R
import ru.sign.conditional.diplomanework.databinding.FragmentEditJobBinding
import ru.sign.conditional.diplomanework.dto.Job
import ru.sign.conditional.diplomanework.util.AndroidUtils
import ru.sign.conditional.diplomanework.util.AndroidUtils.viewScope
import ru.sign.conditional.diplomanework.util.NeWorkDatetime
import ru.sign.conditional.diplomanework.util.NeWorkHelper.overview
import ru.sign.conditional.diplomanework.util.NeWorkHelper.setDatetimeToView
import ru.sign.conditional.diplomanework.util.viewBinding
import ru.sign.conditional.diplomanework.viewmodel.JobViewModel

class EditJobFragment : Fragment(R.layout.fragment_edit_job) {
    private val binding by viewBinding(FragmentEditJobBinding::bind)
    private val jobViewModel: JobViewModel by activityViewModels()
    private val job: Job?
        get() = jobViewModel.edited.value
    private var jobIsBlank: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            customNavigateUp()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        subscribe()
        setupListeners()
    }

    private fun initViews() {
        binding.apply {
            position.editText?.setText(job?.position)
            companyName.editText?.setText(job?.name)
            job?.link?.let { linkField.editText?.setText(it) }
        }
    }

    private fun subscribe() {
        jobViewModel.apply {
            startForLayout.observe(viewLifecycleOwner) { jobStart ->
                binding.jobDurationInput.apply {
                    startYear.setDatetimeToView(jobStart?.year)
                    startMonth.setDatetimeToView(jobStart?.month)
                }
                jobBlankValidation()
            }
            finishForLayout.observe(viewLifecycleOwner) { jobFinish ->
                jobFinish?.let {
                    binding.jobDurationInput.apply {
                        finishYear.setDatetimeToView(jobFinish.year)
                        finishMonth.setDatetimeToView(jobFinish.month)
                    }
                }
            }
            datesIsntValid.observe(viewLifecycleOwner) {
                binding.apply {
                    AndroidUtils.hideKeyboard(binding.root)
                    if (!jobIsBlank && !it) {
                        editJobGroup.isVisible = false
                        progressBarView.progressBar.isVisible = true
                        jobViewModel.saveJob(
                            name = companyName.editText?.text.toString(),
                            position = position.editText?.text.toString(),
                            link = linkField.editText?.text.let {
                                if (!it.isNullOrBlank())
                                    it.trim().toString()
                                else
                                    null
                            }
                        )
                    } else {
                        Snackbar.make(
                            root,
                            if (jobIsBlank)
                                R.string.empty_fields
                            else
                                R.string.incorrect_date,
                            Snackbar.LENGTH_LONG
                        )
                            .setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE)
                            .show()
                    }
                }
            }
            jobEvent.observe(viewLifecycleOwner) { code ->
                job?.let { job ->
                    binding.apply {
                        Toast.makeText(
                            context,
                            root.context.getString(
                                if (code == HTTP_OK) {
                                    if (job.id == 0)
                                        R.string.job_was_added
                                    else
                                        R.string.job_was_saved
                                } else {
                                    /* resId = */ R.string.error_saving_job
                                },
                                /* ...formatArgs = */ overview(code)
                            ),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
                customNavigateUp()
            }
        }
    }

    private fun setupListeners() {
        requireActivity().addMenuProvider(
            /* provider = */ object : MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(R.menu.edit_item_menu, menu)
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
                    when (menuItem.itemId) {
                        R.id.publish -> {
                            jobViewModel.datesValidationBeforeSaveJob()
                            true
                        }
                        else -> false
                    }
            },
            /* owner = */ viewLifecycleOwner
        )
        binding.apply {
            jobDurationInput.apply {
                setCustomTextChangeListener(
                    action = { jobViewModel.setStart(
                            NeWorkDatetime(
                                year = startYear.editText?.text ?: "",
                                month = startMonth.editText?.text ?: ""
                            )
                    )},
                    view = arrayOf(startYear, startMonth)
                )
                setCustomTextChangeListener(
                    action = { jobViewModel.setFinish(
                        NeWorkDatetime(
                            year = finishYear.editText?.text ?: "",
                            month = finishMonth.editText?.text ?: ""
                        )
                    )},
                    view = arrayOf(finishYear, finishMonth)
                )
            }
            setCustomTextChangeListener(
                action = { jobBlankValidation() },
                view = arrayOf(position, companyName)
            )
        }
    }

    private fun setCustomTextChangeListener(action: () -> Unit, vararg view: TextInputLayout) {
        view.map {
            viewScope.launch {
                it.editText?.addTextChangedListener { action() }
            }
        }
    }

    private fun jobBlankValidation() {
        viewScope.launch {
            jobIsBlank =
                binding.position.editText?.text.isNullOrBlank()
                    .or(binding.companyName.editText?.text.isNullOrBlank())
                    .or(binding.jobDurationInput.startYear.editText?.text.isNullOrBlank())
                    .or(binding.jobDurationInput.startMonth.editText?.text.isNullOrBlank())
        }
    }

    private fun customNavigateUp() {
        jobViewModel.apply {
            clearEditJob()
            clearDates()
        }
        findNavController().navigateUp()
    }
}