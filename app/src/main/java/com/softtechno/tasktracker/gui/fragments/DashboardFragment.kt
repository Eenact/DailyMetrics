package com.softtechno.tasktracker.gui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.softtechno.tasktracker.databinding.FragmentDashboardBinding
import com.softtechno.tasktracker.viewmodels.TaskValueViewModel
import com.softtechno.tasktracker.viewmodels.TaskViewViewModelFactory


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val taskValuesModel: TaskValueViewModel by activityViewModels {
        TaskViewViewModelFactory(requireContext())
    }

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)

        setValues()

        return binding.root

    }

    private fun setValues() {

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        taskValuesModel.allValues.observe(viewLifecycleOwner) {
            //Read item values
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}