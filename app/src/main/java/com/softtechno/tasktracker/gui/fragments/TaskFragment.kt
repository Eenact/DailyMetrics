package com.softtechno.tasktracker.gui.fragments

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
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.softtechno.tasktracker.R
import com.softtechno.tasktracker.databinding.FragmentTaskBinding
import com.softtechno.tasktracker.db.TaskEntity
import com.softtechno.tasktracker.enums.EntityType
import com.softtechno.tasktracker.enums.Units
import com.softtechno.tasktracker.managers.TaskCreationManager
import com.softtechno.tasktracker.viewmodels.TaskEntityViewModel
import com.softtechno.tasktracker.viewmodels.TaskEntityViewModelFactory

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

        // Add button clicked
        binding.buttonAdd.setOnClickListener {
            val name: String = binding.taskNameEdittext.editableText.toString()
            val type: EntityType = EntityType.fromString(binding.taskTypeEdittext.text.toString())
            val unit: Units = Units.fromString(binding.taskUnitsEdittext.text.toString())

            if (name.trim().isEmpty()) {
                binding.taskNameInputLayout.error = getString(R.string.error_field_required)
                return@setOnClickListener
            }

            if (mSelectedTask == null) {
                val task = TaskEntity(
                    name, type, unit, mSelectedColor
                )
                mTaskEntityModel?.insert(task)
            } else {
                val task =
                    TaskEntity(name, mSelectedTask!!.type, unit, mSelectedColor, mSelectedTaskId)
                mTaskEntityModel?.update(task, name != mSelectedTask!!.name)
            }

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

    override fun onDestroyView() {
        super.onDestroyView()
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
        binding.buttonAdd.setText(R.string.update)
    }
}