package com.eenact.dailymetrics.gui.fragments

import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.eenact.dailymetrics.R
import com.eenact.dailymetrics.databinding.FragmentEntryBinding
import com.eenact.dailymetrics.db.TaskEntity
import com.eenact.dailymetrics.db.TaskValue
import com.eenact.dailymetrics.enums.EntityType
import com.eenact.dailymetrics.enums.Units
import com.eenact.dailymetrics.utils.DateTimeUtils
import com.eenact.dailymetrics.viewmodels.TaskEntityViewModel
import com.eenact.dailymetrics.viewmodels.TaskEntityViewModelFactory
import com.eenact.dailymetrics.viewmodels.TaskValueViewModel
import com.eenact.dailymetrics.viewmodels.TaskValueViewModelFactory
import java.util.*


class TaskEntryDialogFragment : DialogFragment() {

    companion object {
        private const val DEBUG: Boolean = false
        private const val TAG: String = "TaskEntryDialogFragment"
    }

    private var _binding: FragmentEntryBinding? = null
    private val mTaskValuesModel: TaskValueViewModel by activityViewModels {
        TaskValueViewModelFactory(requireActivity())
    }
    private val mTaskEntityModel: TaskEntityViewModel by activityViewModels {
        TaskEntityViewModelFactory(requireActivity())
    }

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentEntryBinding.inflate(inflater, container, false)

        mTaskEntityModel.allEntries.observe(viewLifecycleOwner) { taskEntities ->
            (binding.entryNameEdittext as? MaterialAutoCompleteTextView)?.setSimpleItems(
                taskEntities.map { it.name }.toTypedArray()
            )
        }

        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.AppTheme_FullScreenDialog)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {

            initViews()

            toolbar.setTitle(R.string.task_entry_fragment_label)
            toolbar.setBackgroundColor(
                ResourcesCompat.getColor(
                    resources,
                    R.color.device_theme_color,
                    null
                )
            )
            toolbar.setNavigationIcon(R.drawable.ic_close)
            toolbar.setNavigationOnClickListener { dismiss() }
            toolbar.inflateMenu(R.menu.menu_task_entry)
            toolbar.setOnMenuItemClickListener { menuItem ->
                return@setOnMenuItemClickListener when (menuItem.itemId) {
                    R.id.action_save -> {
                        val value: Long? =
                            if (mTaskEntityModel.selectedItem.value?.type == EntityType.Number) {
                                binding.entryNumberEdittext.text.toString().toLongOrNull()
                            } else {
                                mTaskValuesModel.taskEnteredValue.value
                            }

                        if (DEBUG) {
                            Log.d(
                                TAG,
                                "save = Time: ${mTaskValuesModel.taskEntryDateTime.value}, task_id: ${mTaskEntityModel.selectedItem.value}, Value: $value"
                            )
                        }

                        mTaskValuesModel.insert(
                            TaskValue(
                                mTaskValuesModel.taskEntryDateTime.value
                                    ?: return@setOnMenuItemClickListener invalidValue(
                                        binding.entryEventTitleTextview,
                                        R.string.error_invalid_date_time
                                    ),
                                mTaskEntityModel.selectedItem.value?.id
                                    ?: return@setOnMenuItemClickListener invalidValue(
                                        binding.entryNameEdittext,
                                        R.string.error_invalid_field_name
                                    ),
                                value
                                    ?: return@setOnMenuItemClickListener when (mTaskEntityModel.selectedItem.value?.type) {
                                        EntityType.Number -> invalidValue(binding.entryNumberInputLayout)
                                        else -> invalidValue(binding.entryYesnoInputLayout)
                                    },
                                binding.entryCommentEdittext.text.toString()
                            )
                        )

                        dismiss()
                        findNavController().navigate(R.id.action_TaskEntryFragment_to_DashboardFragment)
                        true
                    }
                    else -> false
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun invalidValue(view: View, message: Int): Boolean {
        Snackbar.make(view, message, Snackbar.LENGTH_SHORT)
            .setAnchorView(binding.root)
            .show()
        return false
    }

    private fun invalidValue(inputLayout: TextInputLayout): Boolean {
        inputLayout.error = getString(R.string.error_field_required)
        return false
    }

    private fun showItems(view: View, selectedTask: TaskEntity?) {

        binding.entryEventDateInputLayout.isEnabled = true
        binding.entryEventTimeInputLayout.isEnabled = true
        binding.entryCommentInputLayout.isEnabled = true
        binding.entryToFromLayout.visibility = View.GONE
        binding.entryYesnoInputLayout.isEnabled = true

        when (mTaskEntityModel.selectedItem.value?.type) {
            EntityType.YesNo -> {
                binding.entryYesnoInputLayout.visibility = View.VISIBLE

                binding.entryNumberInputLayout.visibility = View.GONE
            }
            EntityType.Number -> {
                binding.entryNumberInputLayout.visibility = View.VISIBLE
                binding.entryNumberInputLayout.hint =
                    if (selectedTask != null && selectedTask.unit != Units.Other) selectedTask.unit.toString() else getString(
                        R.string.entry_number_title
                    )

                binding.entryYesnoInputLayout.visibility = View.GONE
            }
            else -> {
                Snackbar.make(view, "Unknown type found.", Snackbar.LENGTH_SHORT)
                    .setAnchorView(binding.root)
                    .show()
            }
        }

    }

    private fun initViews() {

        // Name selection listener
        binding.entryNameEdittext.setOnItemClickListener { _, view, i, _ ->

            val selectedTask = mTaskEntityModel.setSelectedItem(i)
            showItems(view, selectedTask)

        }

        binding.entryYesnoEdittext.setOnItemClickListener { _, _, i, _ ->

            binding.entryYesnoInputLayout.error = null
            mTaskValuesModel.taskEnteredValue.value = when (i) {
                0 -> 1
                1 -> 0
                else -> null
            }

            if (DEBUG) {
                Log.d(TAG, "YesNo Item selected: $i")
            }
        }
        binding.entryNumberEdittext.setOnKeyListener { _, _, _ ->
            binding.entryNumberInputLayout.error = null
            false
        }

        val calendar = Calendar.getInstance()
        val currentDate = DateTimeUtils.DateFormatter.format(calendar.time)
        val currentTime = DateTimeUtils.TimeFormatter.format(calendar.time)

        mTaskValuesModel.taskEntryDateTime.value = calendar.timeInMillis

        // From date
        binding.entryFromDateEdittext.setText(currentDate)
        binding.entryFromDateEdittext.setOnClickListener {
            dateRangePicker()
        }

        // To date
        binding.entryToDateEdittext.setText(currentDate)
        binding.entryToDateEdittext.setOnClickListener {
            dateRangePicker()
        }

        // Event date
        binding.entryEventDateEdittext.setText(currentDate)
        binding.entryEventDateEdittext.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(R.string.entry_date_picker_dialog_title)
                .setInputMode(MaterialDatePicker.INPUT_MODE_CALENDAR)
                .setSelection(mTaskValuesModel.taskEntryDateTime.value)
                .setCalendarConstraints(
                    CalendarConstraints.Builder()
                        .setValidator(DateValidatorPointBackward.now())
                        .build()
                )
                .build()
            datePicker.addOnPositiveButtonClickListener {

                val setTime = Calendar.getInstance()
                setTime.timeInMillis = mTaskValuesModel.taskEntryDateTime.value!!

                val dateNow = Calendar.getInstance()
                dateNow.timeInMillis = it
                dateNow.set(Calendar.HOUR_OF_DAY, setTime.get(Calendar.HOUR_OF_DAY))
                dateNow.set(Calendar.MINUTE, setTime.get(Calendar.MINUTE))
                dateNow.set(Calendar.SECOND, 0)
                dateNow.set(Calendar.MILLISECOND, 0)

                mTaskValuesModel.taskEntryDateTime.value = dateNow.timeInMillis

                binding.entryEventDateEdittext.setText(DateTimeUtils.DateFormatter.format(dateNow.time))

                if (DEBUG) {
                    Log.d(TAG, "Picked Date: $it")
                }
            }
            datePicker.show(requireActivity().supportFragmentManager, "event_date")
        }

        // Event time
        val is24Hour = DateFormat.is24HourFormat(requireContext())
        binding.entryEventTimeEdittext.setText(currentTime)
        binding.entryEventTimeEdittext.setOnClickListener {

            val savedTimeCalendar = Calendar.getInstance()
            savedTimeCalendar.timeInMillis = mTaskValuesModel.taskEntryDateTime.value!!

            val timePicker = MaterialTimePicker.Builder()
                .setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK)
                .setMinute(savedTimeCalendar.get(Calendar.MINUTE))
                .setTitleText(R.string.entry_time_picker_dialog_title)
                .setTimeFormat(if (is24Hour) TimeFormat.CLOCK_24H else TimeFormat.CLOCK_12H)
                .setHour(
                    if (is24Hour) savedTimeCalendar.get(Calendar.HOUR_OF_DAY) else savedTimeCalendar.get(
                        Calendar.HOUR
                    )
                )
                .build()
            timePicker.addOnPositiveButtonClickListener {

                val timeNow = Calendar.getInstance()
                timeNow.timeInMillis = mTaskValuesModel.taskEntryDateTime.value!!
                timeNow.set(Calendar.HOUR_OF_DAY, timePicker.hour)
                timeNow.set(Calendar.MINUTE, timePicker.minute)
                timeNow.set(Calendar.SECOND, 0)
                timeNow.set(Calendar.MILLISECOND, 0)

                mTaskValuesModel.taskEntryDateTime.value = timeNow.timeInMillis
                binding.entryEventTimeEdittext.setText(DateTimeUtils.TimeFormatter.format(timeNow.time))

                if (DEBUG) {
                    Log.d(
                        TAG,
                        "Picked Time: ${timeNow.timeInMillis}, Hour: ${timePicker.hour}, Min: ${timePicker.minute}"
                    )
                }
            }
            timePicker.show(requireActivity().supportFragmentManager, "event_time")
        }

    }

    private fun dateRangePicker() {
        val datePicker = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText(R.string.entry_date_picker_dialog_title)
            .setInputMode(MaterialDatePicker.INPUT_MODE_CALENDAR)
            .build()
        datePicker.addOnPositiveButtonClickListener {
            if (DEBUG) {
                Log.d(TAG, "Picked Date: ${it.first} - ${it.second}")
            }
        }
        datePicker.show(requireActivity().supportFragmentManager, "to_from_date_picker")
    }
}