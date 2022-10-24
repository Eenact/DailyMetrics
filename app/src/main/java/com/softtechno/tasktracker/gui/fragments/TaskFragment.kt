package com.softtechno.tasktracker.gui.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.softtechno.tasktracker.R
import com.softtechno.tasktracker.databinding.FragmentTaskBinding
import com.softtechno.tasktracker.db.TaskEntity
import com.softtechno.tasktracker.enums.EntityType
import com.softtechno.tasktracker.enums.Units
import com.softtechno.tasktracker.viewmodels.TaskEntityViewModel
import com.softtechno.tasktracker.viewmodels.TaskEntityViewModelFactory
import java.util.*

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class TaskFragment : Fragment() {

    private var _binding: FragmentTaskBinding? = null
    private var mTaskEntityModel: TaskEntityViewModel? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentTaskBinding.inflate(inflater, container, false)

        val mViewModelFactory = TaskEntityViewModelFactory(requireContext())
        mTaskEntityModel = ViewModelProvider(this, mViewModelFactory).get()

        fillUnits()
        fillEntities()

        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.taskNameEdittext.addTextChangedListener {
            binding.taskNameInputLayout.error = null
        }

        binding.buttonAdd.setOnClickListener {
            val name: String = binding.taskNameEdittext.editableText.toString()
            val type: EntityType = EntityType.fromString(binding.taskTypeEdittext.text.toString())
            val unit: Units = Units.fromString(binding.taskUnitsEdittext.text.toString())
            val rnd = Random()

            if (name.trim().isEmpty()) {
                binding.taskNameInputLayout.error = getString(R.string.error_field_required)
                return@setOnClickListener
            }

            val task = TaskEntity(name, type, unit, Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256)))
            mTaskEntityModel?.insert(task)

        }

        mTaskEntityModel?.insertSuccessful?.observe(viewLifecycleOwner) { successful ->
            if (successful) {
                findNavController().navigate(R.id.action_TaskFragment_to_DashboardFragment)
            } else {
                binding.taskNameInputLayout.error = getString(R.string.error_already_exists)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun fillEntities() {

        (binding.taskTypeEdittext as? MaterialAutoCompleteTextView)?.setSimpleItems(EntityType.values().map { it.toString() }.toTypedArray())
        (binding.taskTypeEdittext as? MaterialAutoCompleteTextView)?.setText(EntityType.YesNo.toString(), false)

    }

    private fun fillUnits() {

        (binding.taskUnitsEdittext as? MaterialAutoCompleteTextView)?.setSimpleItems(Units.values().map { it.toString() }.toTypedArray())

    }
}