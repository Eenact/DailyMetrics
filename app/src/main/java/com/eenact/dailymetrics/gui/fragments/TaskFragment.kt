package com.eenact.dailymetrics.gui.fragments

import android.app.TimePickerDialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.eenact.dailymetrics.R
import com.eenact.dailymetrics.databinding.FragmentTaskBinding
import com.eenact.dailymetrics.db.TaskEntity
import com.eenact.dailymetrics.enums.EntityType
import com.eenact.dailymetrics.enums.Units
import com.eenact.dailymetrics.gui.MainActivity
import com.eenact.dailymetrics.managers.TaskCreationManager
import com.eenact.dailymetrics.utils.NotificationScheduler
import com.eenact.dailymetrics.viewmodels.TaskEntityViewModel
import com.eenact.dailymetrics.viewmodels.TaskEntityViewModelFactory
import java.util.*

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class TaskFragment : Fragment() {

    private var mSelectedTaskId: Long = -1
    private var mSelectedTask: TaskEntity? = null

    private var _binding: FragmentTaskBinding? = null
    private var mTaskEntityModel: TaskEntityViewModel? = null

    private var mColorPickerDialog: View? = null
    private var mSelectedColor: Int = Color.RED
    private var mSelectedTime: String? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        _binding = FragmentTaskBinding.inflate(inflater, container, false)

        val mViewModelFactory = TaskEntityViewModelFactory(requireContext())
        mTaskEntityModel = ViewModelProvider(this, mViewModelFactory).get()

        TaskCreationManager.fillUnits(binding.taskUnitsEdittext as MaterialAutoCompleteTextView)
        TaskCreationManager.fillTypes(binding.taskTypeEdittext as MaterialAutoCompleteTextView)

        mSelectedTaskId = arguments?.getLong("task_id", -1) ?: -1

        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? MainActivity)?.permissionResultListener = { isGranted ->
            if (!isGranted) {
                binding.notificationSwitch.isChecked = false
            }
        }

        if (mSelectedTaskId != -1L) {
            mTaskEntityModel?.allEntries?.observe(viewLifecycleOwner) { entries ->
                val selectedItem = entries.firstOrNull { it.id == mSelectedTaskId }
                if (selectedItem != null) {
                    setUpdateItems(selectedItem)
                }
            }
        }

        binding.taskNameEdittext.addTextChangedListener {
            binding.taskNameInputLayout.error = null
        }

        binding.taskTypeEdittext.setOnItemClickListener { _, _, i, _ ->
            binding.taskUnitsInputLayout.isEnabled = (i != 0)
        }

        binding.notificationSwitch.setOnCheckedChangeListener { _, isChecked ->
            binding.notificationSettingsContainer.visibility = if (isChecked) View.VISIBLE else View.GONE
            if (isChecked) {
                (activity as MainActivity).askNotificationPermission()
            }
            else if (mSelectedTaskId != -1L) {
                NotificationScheduler.cancelNotification(requireContext(), mSelectedTaskId)
            }
        }

        binding.notificationTimeButton.setOnClickListener {
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            TimePickerDialog(requireContext(), { _, h, m ->
                mSelectedTime = String.format("%02d:%02d", h, m)
                binding.notificationTimeButton.text = "Time: $mSelectedTime"
            }, hour, minute, true).show()
        }

        // Add button clicked
        binding.buttonAdd.setOnClickListener {
            val name: String = binding.taskNameEdittext.editableText.toString()
            val type: EntityType = EntityType.fromString(binding.taskTypeEdittext.text.toString())
            val unit: Units = Units.fromString(binding.taskUnitsEdittext.text.toString())
            val isNotifEnabled = binding.notificationSwitch.isChecked
            val notifDays = getSelectedDays()

            if (name.trim().isEmpty()) {
                binding.taskNameInputLayout.error = getString(R.string.error_field_required)
                return@setOnClickListener
            }

            if (isNotifEnabled && mSelectedTime == null) {
                // Show error or pick default
                mSelectedTime = "09:00"
            }

            val task = if (mSelectedTask == null) {
                TaskEntity(
                    name, type, unit, mSelectedColor, isNotifEnabled, mSelectedTime, notifDays
                )
            } else {
                TaskEntity(
                    name, mSelectedTask!!.type, unit, mSelectedColor, isNotifEnabled, mSelectedTime, notifDays, mSelectedTaskId
                )
            }

            if (mSelectedTask == null) {
                mTaskEntityModel?.insert(task)
            } else {
                mTaskEntityModel?.update(task, name != mSelectedTask!!.name)
            }

            // Schedule notification
            NotificationScheduler.scheduleNotification(requireContext(), task)
        }

        // Observer of insert result
        mTaskEntityModel?.insertSuccessful?.observe(viewLifecycleOwner) { successful ->
            if (successful) {
                findNavController().navigate(R.id.action_TaskFragment_to_TaskManagerFragment)
            } else {
                binding.taskNameInputLayout.error = getString(R.string.error_already_exists)
            }
        }

        // Setting Color button
        binding.taskColorInputButton.setOnClickListener {

            mColorPickerDialog = TaskCreationManager.colorPickerDialog(
                requireContext(),
                mSelectedColor
            ) { selectedColor ->
                mSelectedColor = selectedColor
                setSelectedIconColor()
            }
        }

        mSelectedColor = ResourcesCompat.getColor(resources, R.color.legend_red, null)
        setSelectedIconColor()
    }

    private fun getSelectedDays(): String {
        val days = mutableListOf<Int>()
        if (binding.chipMon.isChecked) days.add(Calendar.MONDAY)
        if (binding.chipTue.isChecked) days.add(Calendar.TUESDAY)
        if (binding.chipWed.isChecked) days.add(Calendar.WEDNESDAY)
        if (binding.chipThu.isChecked) days.add(Calendar.THURSDAY)
        if (binding.chipFri.isChecked) days.add(Calendar.FRIDAY)
        if (binding.chipSat.isChecked) days.add(Calendar.SATURDAY)
        if (binding.chipSun.isChecked) days.add(Calendar.SUNDAY)
        return days.joinToString(",")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (activity as? MainActivity)?.permissionResultListener = null
        _binding = null
    }

    private fun setSelectedIconColor() {
        (binding.taskColorInputButton as MaterialButton).iconTint =
            ColorStateList.valueOf(mSelectedColor)
    }

    private fun setUpdateItems(selectedItem: TaskEntity) {
        mSelectedTask = selectedItem
        binding.taskNameEdittext.setText(selectedItem.name)
        binding.taskTypeEdittext.setText(selectedItem.type.toString())
        binding.taskTypeInputLayout.isEnabled = false
        if (selectedItem.type == EntityType.Number) {
            binding.taskUnitsInputLayout.isEnabled = true
            (binding.taskUnitsEdittext as? MaterialAutoCompleteTextView)?.setText(
                selectedItem.unit.toString(),
                false
            )
        } else {
            binding.taskUnitsInputLayout.isEnabled = false
        }
        mSelectedColor = selectedItem.color
        (binding.taskColorInputButton as MaterialButton).iconTint =
            ColorStateList.valueOf(selectedItem.color)
        
        binding.notificationSwitch.isChecked = selectedItem.isNotificationEnabled
        mSelectedTime = selectedItem.notificationTime
        if (mSelectedTime != null) {
            binding.notificationTimeButton.text = "Time: $mSelectedTime"
        }
        
        selectedItem.notificationDays?.split(",")?.forEach {
            when (it.toIntOrNull()) {
                Calendar.MONDAY -> binding.chipMon.isChecked = true
                Calendar.TUESDAY -> binding.chipTue.isChecked = true
                Calendar.WEDNESDAY -> binding.chipWed.isChecked = true
                Calendar.THURSDAY -> binding.chipThu.isChecked = true
                Calendar.FRIDAY -> binding.chipFri.isChecked = true
                Calendar.SATURDAY -> binding.chipSat.isChecked = true
                Calendar.SUNDAY -> binding.chipSun.isChecked = true
            }
        }

        binding.buttonAdd.setText(R.string.update)
    }
}
