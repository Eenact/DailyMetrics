package com.softtechno.tasktracker.gui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.softtechno.tasktracker.R
import com.softtechno.tasktracker.adapter.TaskManagerAdapter
import com.softtechno.tasktracker.databinding.FragmentTaskManagerBinding
import com.softtechno.tasktracker.db.TaskEntity
import com.softtechno.tasktracker.gui.MainActivity
import com.softtechno.tasktracker.utils.RecyclerViewUtils
import com.softtechno.tasktracker.viewmodels.TaskEntityViewModel
import com.softtechno.tasktracker.viewmodels.TaskEntityViewModelFactory


class TaskManagerFragment : Fragment() {

    // This property is only valid between onCreateView and
    // onDestroyView.
    private var _binding: FragmentTaskManagerBinding? = null
    private val binding get() = _binding!!

    private val mTaskEntityModel: TaskEntityViewModel by activityViewModels {
        TaskEntityViewModelFactory(requireActivity())
    }
    private var mTaskManagerAdapter: TaskManagerAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentTaskManagerBinding.inflate(inflater, container, false)

        mTaskEntityModel.allEntries.observe(viewLifecycleOwner) {
            binding.taskManagerNoItemsTextview.visibility =
                if (it.isNullOrEmpty()) View.VISIBLE else View.GONE
            mTaskManagerAdapter?.updateItems(it)
        }

        mTaskManagerAdapter = TaskManagerAdapter(TaskManagerAdapter.OnClickListener { task ->
            val bundle = Bundle()
            bundle.putLong("task_id", task.id)
            findNavController().navigate(R.id.action_TaskManagerFragment_to_TaskFragment, bundle)
        })
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
        setListGestures()

        return binding.root
    }

    override fun onResume() {
        super.onResume()

        (activity as? MainActivity).let {
            it?.didClickAddButton = {
                findNavController().navigate(R.id.action_TaskManagerFragment_to_TaskFragment)
            }
        }
    }

    override fun onPause() {
        super.onPause()

        (activity as? MainActivity).let {
            it?.didClickAddButton = null
        }
    }

    private fun setListGestures() {
        val itemTouch = RecyclerViewUtils.swipeToDelete(resources) { viewHolder ->
            // this method is called when we swipe our item to right direction.
            // on below line we are getting the item at a particular position.
            val deletedTask: TaskEntity =
                mTaskEntityModel.allEntries.value?.get(viewHolder.adapterPosition)
                    ?: return@swipeToDelete

            // below line is to get the position
            // of the item at that position.
            val position = viewHolder.adapterPosition

            // below line is to notify our item is removed from adapter.
            mTaskManagerAdapter?.notifyItemRemoved(position)

            mTaskEntityModel.delete(deletedTask)

            // below line is to display our snackbar
            Snackbar.make(
                binding.recyclerListTasks,
                getString(R.string.item_deleted, deletedTask.name),
                Snackbar.LENGTH_LONG
            )
                .show()
        }
        itemTouch.attachToRecyclerView(binding.recyclerListTasks)
    }

}