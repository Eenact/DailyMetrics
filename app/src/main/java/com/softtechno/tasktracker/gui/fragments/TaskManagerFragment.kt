package com.softtechno.tasktracker.gui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.softtechno.tasktracker.R
import com.softtechno.tasktracker.adapter.TaskManagerAdapter
import com.softtechno.tasktracker.databinding.FragmentTaskManagerBinding
import com.softtechno.tasktracker.viewmodels.TaskEntityViewModel
import com.softtechno.tasktracker.viewmodels.TaskEntityViewModelFactory

class TaskManagerFragment : Fragment() {

    // This property is only valid between onCreateView and
    // onDestroyView.
    private var _binding: FragmentTaskManagerBinding? = null
    private val binding get() = _binding!!

    private var mTaskEntityModel: TaskEntityViewModel? = null
    private var mTaskManagerAdapter: TaskManagerAdapter = TaskManagerAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentTaskManagerBinding.inflate(inflater, container, false)

        val viewModelFactory = TaskEntityViewModelFactory(requireContext())
        mTaskEntityModel = ViewModelProvider(this, viewModelFactory).get()

        mTaskEntityModel!!.allEntries.observe(viewLifecycleOwner) {
            mTaskManagerAdapter.updateItems(it)
            mTaskManagerAdapter.notifyDataSetChanged()
        }

        binding.buttonInsertTask.setOnClickListener {
//            val taskEntryDialogFragment = TaskEntryDialogFragment()
//            if (isLargeDisplay) {
//                taskEntryDialogFragment.show(supportFragmentManager, "TaskEntryDialog")
//            }
            findNavController().navigate(R.id.action_TaskManagerFragment_to_TaskFragment)
            activity?.actionBar?.setHomeAsUpIndicator(R.drawable.ic_close)
            (activity as AppCompatActivity).supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_close)
        }

        val linearLayoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.recyclerListTasks.layoutManager = linearLayoutManager
        binding.recyclerListTasks.adapter = mTaskManagerAdapter
        binding.recyclerListTasks.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                linearLayoutManager.orientation
            )
        )

        return binding.root
    }


}