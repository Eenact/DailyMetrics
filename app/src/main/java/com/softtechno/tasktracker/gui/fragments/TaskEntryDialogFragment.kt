package com.softtechno.tasktracker.gui.fragments

import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.view.*
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
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
import com.softtechno.tasktracker.R
import com.softtechno.tasktracker.databinding.FragmentEntryBinding
import com.softtechno.tasktracker.db.TaskValue
import com.softtechno.tasktracker.enums.EntityType
import com.softtechno.tasktracker.viewmodels.TaskEntityViewModel
import com.softtechno.tasktracker.viewmodels.TaskEntityViewModelFactory
import com.softtechno.tasktracker.viewmodels.TaskValueViewModel
import com.softtechno.tasktracker.viewmodels.TaskViewViewModelFactory
import java.text.SimpleDateFormat
import java.util.*


class TaskEntryDialogFragment : DialogFragment() {

    private val DEBUG: Boolean = false
    private val TAG: String = TaskEntryDialogFragment::class.java.name

    private var _binding: FragmentEntryBinding? = null
    private val mTaskValuesModel: TaskValueViewModel by activityViewModels {
        TaskViewViewModelFactory(requireContext())
    }
    private val mTaskEntityModel: TaskEntityViewModel by activityViewModels {
        TaskEntityViewModelFactory(requireContext())
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

        val menuHost: MenuHost = requireActivity()

        initViews()

        menuHost.addMenuProvider(object : MenuProvider {
            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
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
                                mTaskValuesModel.taskEntryDateTime.value ?: return invalidValue(
                                    binding.entryEventTitleTextview,
                                    R.string.error_invalid_date_time
                                ),
                                mTaskEntityModel.selectedItem.value?.id ?: return invalidValue(
                                    binding.entryNameEdittext,
                                    R.string.error_invalid_field_name
                                ),
                                value ?: return when (mTaskEntityModel.selectedItem.value?.type) {
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

            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menu.clear()
                menuInflater.inflate(R.menu.menu_task_entry, menu)
            }
        }, viewLifecycleOwner)

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

    private fun showItems(view: View) {

        when (mTaskEntityModel.selectedItem.value?.type) {
            EntityType.YesNo -> {
                binding.entryYesnoInputLayout.visibility = View.VISIBLE
                binding.entryDateLayout.visibility = View.VISIBLE

                binding.entryNumberInputLayout.visibility = View.GONE
                binding.entryToFromLayout.visibility = View.GONE
            }
            EntityType.Number -> {
                binding.entryNumberInputLayout.visibility = View.VISIBLE
                binding.entryDateLayout.visibility = View.VISIBLE

                binding.entryYesnoInputLayout.visibility = View.GONE
                binding.entryToFromLayout.visibility = View.GONE
            }
            /*EntityType.StartEndDate -> {
                binding.entryToFromLayout.visibility = View.VISIBLE

                binding.entryDateLayout.visibility = View.GONE
                binding.entryYesnoInputLayout.visibility = View.GONE
                binding.entryNumberInputLayout.visibility = View.GONE
            }*/
            else -> {
                Snackbar.make(view, "Unknown type found.", Snackbar.LENGTH_SHORT)
                    .setAnchorView(binding.root)
                    .show()
            }
        }
        binding.entryCommentInputLayout.visibility = View.VISIBLE

    }

    private fun initViews() {

        // Name selection listener
        binding.entryNameEdittext.setOnItemClickListener { _, view, i, _ ->

            mTaskEntityModel.setSelectedItem(i)
            showItems(view)

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

        val dateFormatter = SimpleDateFormat.getDateInstance()
        val currentDate = dateFormatter.format(calendar.time)

        val timeFormatter = SimpleDateFormat.getTimeInstance()
        val currentTime = timeFormatter.format(calendar.time)

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

                binding.entryEventDateEdittext.setText(dateFormatter.format(dateNow.time))

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
                binding.entryEventTimeEdittext.setText(timeFormatter.format(timeNow.time))

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