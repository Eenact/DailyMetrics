package com.softtechno.tasktracker.gui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.softtechno.tasktracker.R
import com.softtechno.tasktracker.adapter.FilterSelectionAdapter
import com.softtechno.tasktracker.db.TaskEntity
import com.softtechno.tasktracker.viewmodels.FilterViewModel

class MainBottomSheetFragment(private var layout: Int, private val entries: Array<TaskEntity>) :
    BottomSheetDialogFragment() {

    // View models
    private val mFilteredTaskIds: FilterViewModel by activityViewModels()

    private lateinit var mMainView: View
    private lateinit var mFilterRecyclerView: RecyclerView
    private lateinit var mFilterSelectionAdapter: FilterSelectionAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        mMainView = inflater.inflate(layout, container, false)

        when (layout) {

            R.layout.dialog_filter_bottom_sheet -> {

                // Cancel button clicked
                mMainView.findViewById<MaterialButton>(R.id.main_bottom_close).setOnClickListener {
                    this.dismiss()
                }

                // Apply button clicked
                mMainView.findViewById<MaterialButton>(R.id.main_bottom_apply).setOnClickListener {
                    mFilteredTaskIds.setFilteredTasks(mFilterSelectionAdapter.getAllFilters())
                    mFilteredTaskIds.notifyFilterChanged()
                    this.dismiss()
                }

                // Select all checkbox clicked
                mMainView.findViewById<MaterialCheckBox>(R.id.main_filter_select_all_check)
                    .setOnCheckedChangeListener { _, checked ->
                        mFilterSelectionAdapter.checkAll(checked)
                    }

                // Setting recycler view
                mFilterRecyclerView = mMainView.findViewById(R.id.main_filter_recycler_view)
                mFilterRecyclerView.layoutManager =
                    StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
                mFilterSelectionAdapter =
                    FilterSelectionAdapter(entries, mFilteredTaskIds.filteredTasks.value ?: mapOf())
            }

        }

        return mMainView

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mFilterRecyclerView.adapter = mFilterSelectionAdapter
    }

    companion object {
        const val TAG = "MainBottomSheet"
    }

}