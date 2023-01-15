package com.softtechno.tasktracker.gui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.softtechno.tasktracker.R
import com.softtechno.tasktracker.adapter.ExportListAdapter
import com.softtechno.tasktracker.adapter.FilterSelectionAdapter
import com.softtechno.tasktracker.db.TaskEntity
import com.softtechno.tasktracker.db.TaskValue
import com.softtechno.tasktracker.enums.ExportTypes
import com.softtechno.tasktracker.managers.DataExportManager
import com.softtechno.tasktracker.viewmodels.FilterViewModel

class MainBottomSheetFragment(
    private var layout: Int,
    private val entries: Array<TaskEntity>?,
    private val values: Map<TaskEntity, TaskValue>?
) : BottomSheetDialogFragment() {

    // View models
    private val mFilteredTaskIds: FilterViewModel by activityViewModels()

    private lateinit var mDataExportManager: DataExportManager

    // UI Views
    private lateinit var mMainView: View
    private lateinit var mCheckBox: MaterialCheckBox

    // Filtered Recycler view
    private var mFilterRecyclerView: RecyclerView? = null
    private lateinit var mFilterSelectionAdapter: FilterSelectionAdapter

    // Export Recycler view
    private var mExportRecyclerView: RecyclerView? = null
    private lateinit var mExportListAdapter: ExportListAdapter

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
                mCheckBox = mMainView.findViewById(R.id.main_filter_select_all_check)
                mCheckBox.setOnCheckedChangeListener { _, checked ->
                    mFilterSelectionAdapter.checkAll(checked)
                }

                // Setting recycler view
                mFilterRecyclerView = mMainView.findViewById(R.id.main_filter_recycler_view)
                mFilterRecyclerView?.layoutManager =
                    StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
                mFilterSelectionAdapter = FilterSelectionAdapter(
                    entries ?: arrayOf(), mFilteredTaskIds.filteredTasks.value ?: mapOf()
                )
            }

            R.layout.dialog_export_bottom_sheet -> {

                // Setting filter check box
                mCheckBox = mMainView.findViewById(R.id.main_export_filtered_check)

                // setting recycler view
                mExportRecyclerView = mMainView.findViewById(R.id.main_export_recycler_view)
                val linearLayoutManager =
                    LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
                mExportRecyclerView?.layoutManager = linearLayoutManager
                mExportListAdapter =
                    ExportListAdapter(ExportListAdapter.OnClickListener { exportType ->

                        val fileName = exportData()
                        openContentActivity(exportType, fileName)

                    })
                mExportRecyclerView?.addItemDecoration(
                    DividerItemDecoration(
                        requireContext(), linearLayoutManager.orientation
                    )
                )

                mDataExportManager = DataExportManager(requireActivity())
                lifecycle.addObserver(mDataExportManager)
            }

        }

        return mMainView

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (mFilterRecyclerView != null) {
            mFilterRecyclerView?.adapter = mFilterSelectionAdapter
        } else if (mExportRecyclerView != null) {
            mExportRecyclerView?.adapter = mExportListAdapter
        }
    }

    companion object {
        const val TAG = "MainBottomSheet"
    }

    private fun exportData(): String {

        val filters = mFilteredTaskIds.filteredTasks.value
        val content: Map<TaskEntity, TaskValue> =
            if (mCheckBox.isChecked) values?.filter { filters?.get(it.key.id) == true } ?: mapOf()
            else values ?: mapOf()

        return mDataExportManager.createCsvFile(requireContext(), content, mCheckBox.isChecked)
    }

    private fun openContentActivity(type: ExportTypes, fileName: String) {

        when (type) {

            ExportTypes.ShareData -> {
                mDataExportManager.shareExportedFile(requireContext(), fileName)
            }

            ExportTypes.ExportData -> {
                mDataExportManager.saveFile(fileName)
            }

        }

        dismiss()
    }

}

