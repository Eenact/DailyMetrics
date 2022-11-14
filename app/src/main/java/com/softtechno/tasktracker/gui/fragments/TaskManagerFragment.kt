package com.softtechno.tasktracker.gui.fragments

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
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
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.softtechno.tasktracker.R
import com.softtechno.tasktracker.adapter.TaskManagerAdapter
import com.softtechno.tasktracker.databinding.FragmentTaskManagerBinding
import com.softtechno.tasktracker.db.TaskEntity
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
            binding.taskManagerNoItemsTextview.visibility =
                if (it.isNullOrEmpty()) View.VISIBLE else View.GONE
            mTaskManagerAdapter.updateItems(it)
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
        setListGestures()

        return binding.root
    }

    private fun setListGestures() {
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            // we want to cache these and not allocate anything repeatedly in the onChildDraw method
            private var background: Drawable? = null
            private var deleteText: String? = null
            private var paint: Paint? = null
            private var leftMargin: Float = -1f
            private var initiated = false

            private fun init() {
                deleteText = getString(R.string.delete)
                background = ColorDrawable(Color.RED)
                paint = Paint()
                paint?.color = Color.WHITE
                paint?.textSize =
                    resources.getDimension(R.dimen.task_manager_delete_hint) * resources.displayMetrics.density
                leftMargin =
                    resources.getDimension(R.dimen.task_manager_delete_margin) * resources.displayMetrics.density
                initiated = true
            }

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                // this method is called
                // when the item is moved.
                return false
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                val itemView = viewHolder.itemView

                // not sure why, but this method get's called for viewholder that are already swiped away
                if (viewHolder.adapterPosition == -1) {
                    // not interested in those
                    return
                }
                if (!initiated) {
                    init()
                }

                // draw red background
                background?.setBounds(
                    itemView.right + dX.toInt(),
                    itemView.top,
                    itemView.right,
                    itemView.bottom
                )
                background?.draw(c)

                // draw delete
                val textBox = Rect(
                    itemView.right + dX.toInt(),
                    itemView.top,
                    itemView.right - leftMargin.toInt(),
                    itemView.bottom
                )
                val numOfChars =
                    paint?.breakText(deleteText.toString(), true, textBox.width().toFloat(), null)
                val start = (deleteText.toString().length - numOfChars!!) / 2;
                val bounds = Rect()
                paint?.getTextBounds(deleteText.toString(), 0, deleteText.toString().length, bounds)
                val y: Float =
                    itemView.bottom - (itemView.bottom - itemView.top) / 2f + (bounds.height() / 2f)
                c.drawText(
                    deleteText.toString(),
                    start,
                    start + numOfChars,
                    textBox.exactCenterX(),
                    y,
                    paint!!
                )


                super.onChildDraw(
                    c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                // this method is called when we swipe our item to right direction.
                // on below line we are getting the item at a particular position.
                val deletedTask: TaskEntity =
                    mTaskEntityModel?.allEntries?.value?.get(viewHolder.adapterPosition) ?: return

                // below line is to get the position
                // of the item at that position.
                val position = viewHolder.adapterPosition

                // below line is to notify our item is removed from adapter.
                mTaskManagerAdapter.notifyItemRemoved(position)

                mTaskEntityModel?.delete(deletedTask) ?: return

                // below line is to display our snackbar
                Snackbar.make(
                    binding.recyclerListTasks,
                    getString(R.string.task_deleted, deletedTask.name),
                    Snackbar.LENGTH_LONG
                )
                    .show()
            }
            // at last we are adding this
            // to our recycler view.
        }).attachToRecyclerView(binding.recyclerListTasks)
    }

}