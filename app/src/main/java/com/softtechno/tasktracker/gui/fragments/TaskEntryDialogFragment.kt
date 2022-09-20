package com.softtechno.tasktracker.gui.fragments

import android.os.Bundle
import android.view.*
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.softtechno.tasktracker.R
import com.softtechno.tasktracker.databinding.FragmentEntryBinding
import com.softtechno.tasktracker.db.TaskEntity
import com.softtechno.tasktracker.viewmodels.TaskEntityViewModel
import com.softtechno.tasktracker.viewmodels.TaskEntityViewModelFactory
import com.softtechno.tasktracker.viewmodels.TaskValueViewModel
import com.softtechno.tasktracker.viewmodels.TaskViewViewModelFactory


class TaskEntryDialogFragment : DialogFragment() {

    private var selectedTask: TaskEntity? = null
    private var _binding: FragmentEntryBinding? = null
    private val taskValuesModel: TaskValueViewModel by activityViewModels {
        TaskViewViewModelFactory(requireContext())
    }
    private val taskEntityModel: TaskEntityViewModel by activityViewModels {
        TaskEntityViewModelFactory(requireContext())
    }

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentEntryBinding.inflate(inflater, container, false)

        

        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.AppTheme_FullScreenDialog)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val menuHost: MenuHost = requireActivity()

        menuHost.addMenuProvider(object : MenuProvider {
            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when(menuItem.itemId) {
                    R.id.action_save -> {
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
}